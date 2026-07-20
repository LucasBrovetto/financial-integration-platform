import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTransaction, getTransaction } from './transactions'

describe('transaction API client', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('creates transactions through the development API prefix', async () => {
    const response = { id: 'transaction-id', status: 'PENDING' }
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(response), { status: 201 }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(createTransaction({ terminalId: 'POS-UR-001', amount: 15.5, type: 'SALE' }))
      .resolves.toEqual(response)
    expect(fetchMock).toHaveBeenCalledWith('/api/transactions', expect.objectContaining({ method: 'POST' }))
  })

  it('encodes the transaction identifier used for lookup', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response('{}', { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    await getTransaction('id with spaces')

    expect(fetchMock).toHaveBeenCalledWith('/api/transactions/id%20with%20spaces', expect.any(Object))
  })

  it('exposes the backend error contract', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      status: 404,
      error: 'NOT_FOUND',
      message: 'Transaction not found',
    }), { status: 404 }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(getTransaction('missing')).rejects.toMatchObject({
      status: 404,
      code: 'NOT_FOUND',
    })
  })
})
