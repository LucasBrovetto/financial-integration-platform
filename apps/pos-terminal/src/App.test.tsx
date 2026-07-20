import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import { createTransaction, getTransaction } from './services/transactions'

vi.mock('./services/transactions', async (importOriginal) => {
  const original = await importOriginal<typeof import('./services/transactions')>()
  return {
    ...original,
    createTransaction: vi.fn(),
    getTransaction: vi.fn(),
  }
})

const transaction = {
  id: '6f9619ff-8b86-d011-b42d-00cf4fc964ff',
  terminalId: 'POS-UR-001',
  amount: 5,
  type: 'SALE' as const,
  status: 'PENDING' as const,
  createdAt: '2026-07-20T10:30:00',
  updatedAt: '2026-07-20T10:30:00',
}

describe('POS terminal', () => {
  beforeEach(() => {
    vi.mocked(createTransaction).mockReset()
    vi.mocked(getTransaction).mockReset()
  })

  it('enters an amount using the payment keypad', async () => {
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: '5' }))
    await user.click(screen.getByRole('button', { name: '0' }))
    await user.click(screen.getByRole('button', { name: '0' }))

    expect(screen.getByText(/5\.00/)).toBeInTheDocument()
  })

  it('creates a transaction and displays its receipt', async () => {
    vi.mocked(createTransaction).mockResolvedValue(transaction)
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: '5' }))
    await user.click(screen.getByRole('button', { name: '0' }))
    await user.click(screen.getByRole('button', { name: '0' }))
    await user.click(screen.getByRole('button', { name: 'Process card' }))

    expect(createTransaction).toHaveBeenCalledWith({
      terminalId: 'POS-UR-001',
      amount: 5,
      type: 'SALE',
    })
    expect(await screen.findByRole('heading', { name: 'Pending' })).toBeInTheDocument()
    expect(screen.getAllByText(transaction.id).length).toBeGreaterThan(0)
  })

  it('retrieves a transaction by its identifier', async () => {
    vi.mocked(getTransaction).mockResolvedValue(transaction)
    const user = userEvent.setup()
    render(<App />)

    await user.type(screen.getByLabelText('Transaction ID'), transaction.id)
    await user.click(screen.getByRole('button', { name: 'Search' }))

    expect(getTransaction).toHaveBeenCalledWith(transaction.id)
    expect(await screen.findByRole('heading', { name: 'Pending' })).toBeInTheDocument()
  })
})
