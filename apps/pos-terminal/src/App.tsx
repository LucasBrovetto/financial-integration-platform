import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'
import {
  createTransaction,
  getTransaction,
  TransactionApiError,
} from './services/transactions'
import type {
  Transaction,
  TransactionStatus,
  TransactionType,
} from './types/transaction'

const RECENT_TRANSACTIONS_KEY = 'pos-terminal:recent-transactions'
const KEYPAD = ['1', '2', '3', '4', '5', '6', '7', '8', '9', 'clear', '0', 'backspace'] as const

const operationLabels: Record<TransactionType, string> = {
  SALE: 'Sale',
  REFUND: 'Refund',
  REVERSAL: 'Reversal',
}

const statusLabels: Record<TransactionStatus, string> = {
  PENDING: 'Pending',
  APPROVED: 'Approved',
  DECLINED: 'Declined',
  REFUNDED: 'Refunded',
  REVERSED: 'Reversed',
  PENDING_RETRY: 'Retry pending',
}

function readRecentTransactions(): Transaction[] {
  try {
    return JSON.parse(localStorage.getItem(RECENT_TRANSACTIONS_KEY) ?? '[]') as Transaction[]
  } catch {
    return []
  }
}

function App() {
  const [terminalId, setTerminalId] = useState('POS-UR-001')
  const [operation, setOperation] = useState<TransactionType>('SALE')
  const [amountDigits, setAmountDigits] = useState('')
  const [transaction, setTransaction] = useState<Transaction | null>(null)
  const [recentTransactions, setRecentTransactions] = useState<Transaction[]>(readRecentTransactions)
  const [lookupId, setLookupId] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSearching, setIsSearching] = useState(false)
  const [error, setError] = useState('')

  const amount = useMemo(() => Number(amountDigits || '0') / 100, [amountDigits])
  const formattedAmount = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'UYU',
  }).format(amount)

  function updateRecentTransactions(nextTransaction: Transaction) {
    const next = [
      nextTransaction,
      ...recentTransactions.filter((item) => item.id !== nextTransaction.id),
    ].slice(0, 5)
    setRecentTransactions(next)
    localStorage.setItem(RECENT_TRANSACTIONS_KEY, JSON.stringify(next))
  }

  function handleKey(key: (typeof KEYPAD)[number]) {
    setError('')
    if (key === 'clear') {
      setAmountDigits('')
      return
    }
    if (key === 'backspace') {
      setAmountDigits((current) => current.slice(0, -1))
      return
    }
    setAmountDigits((current) => `${current}${key}`.replace(/^0+/, '').slice(0, 9))
  }

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    setError('')

    if (!terminalId.trim()) {
      setError('Enter the terminal identifier.')
      return
    }
    if (amount <= 0) {
      setError('Enter an amount greater than zero.')
      return
    }

    setIsSubmitting(true)
    try {
      const created = await createTransaction({
        terminalId: terminalId.trim(),
        amount,
        type: operation,
      })
      setTransaction(created)
      setLookupId(created.id)
      setAmountDigits('')
      updateRecentTransactions(created)
    } catch (caughtError) {
      setError(toDisplayError(caughtError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleLookup(event: FormEvent) {
    event.preventDefault()
    setError('')
    if (!lookupId.trim()) {
      setError('Enter a transaction identifier.')
      return
    }

    setIsSearching(true)
    try {
      const found = await getTransaction(lookupId.trim())
      setTransaction(found)
      updateRecentTransactions(found)
    } catch (caughtError) {
      setError(toDisplayError(caughtError))
    } finally {
      setIsSearching(false)
    }
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div className="brand-mark" aria-hidden="true">F</div>
        <div>
          <p className="eyebrow">Financial Integration Platform</p>
          <h1>Payment terminal</h1>
        </div>
        <div className="connection-status"><span /> API local</div>
      </header>

      <section className="workspace">
        <form className="terminal" onSubmit={handleCreate}>
          <div className="terminal-head">
            <label>
              Terminal
              <input
                value={terminalId}
                onChange={(event) => setTerminalId(event.target.value)}
                maxLength={40}
                aria-label="Terminal identifier"
              />
            </label>
            <div className="signal" aria-label="Secure connection">
              <i /><i /><i />
            </div>
          </div>

          <div className="screen">
            <div className="screen-label">
              <span>{operationLabels[operation]}</span>
              <span>UYU</span>
            </div>
            <output className="amount" aria-live="polite">{formattedAmount}</output>
            <p>Enter the amount</p>
          </div>

          <div className="operation-tabs" aria-label="Operation type">
            {(Object.keys(operationLabels) as TransactionType[]).map((type) => (
              <button
                type="button"
                key={type}
                className={operation === type ? 'active' : ''}
                onClick={() => setOperation(type)}
              >
                {operationLabels[type]}
              </button>
            ))}
          </div>

          <div className="keypad" aria-label="Amount keypad">
            {KEYPAD.map((key) => (
              <button type="button" key={key} onClick={() => handleKey(key)}>
                {key === 'clear' ? 'C' : key === 'backspace' ? '⌫' : key}
              </button>
            ))}
          </div>

          <button className="pay-button" type="submit" disabled={isSubmitting}>
            <span className="contactless" aria-hidden="true">)))</span>
            {isSubmitting ? 'Processing…' : 'Process card'}
          </button>
          <p className="secure-note">Encrypted transaction · No card data is stored</p>
        </form>

        <aside className="details-panel">
          <section className="lookup-card">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Tracking</p>
                <h2>Find a transaction</h2>
              </div>
              <span className="search-icon" aria-hidden="true">⌕</span>
            </div>
            <form onSubmit={handleLookup} className="lookup-form">
              <label htmlFor="transaction-id">Transaction ID</label>
              <div>
                <input
                  id="transaction-id"
                  value={lookupId}
                  onChange={(event) => setLookupId(event.target.value)}
                  placeholder="Transaction UUID"
                />
                <button type="submit" disabled={isSearching}>
                  {isSearching ? 'Searching…' : 'Search'}
                </button>
              </div>
            </form>
          </section>

          {error && <div className="error-banner" role="alert">{error}</div>}

          {transaction ? (
            <TransactionReceipt transaction={transaction} />
          ) : (
            <section className="empty-receipt">
              <div className="card-chip" aria-hidden="true"><span /><span /><span /></div>
              <h2>Ready for payment</h2>
              <p>Process a card or find a transaction to view its receipt.</p>
            </section>
          )}

          <section className="recent-card">
            <div className="section-heading">
              <div>
                <p className="eyebrow">This device</p>
                <h2>Recent activity</h2>
              </div>
              <span className="recent-count">{recentTransactions.length}</span>
            </div>
            {recentTransactions.length ? (
              <ul>
                {recentTransactions.map((item) => (
                  <li key={item.id}>
                    <button type="button" onClick={() => { setLookupId(item.id); setTransaction(item) }}>
                      <span className="operation-icon">{item.type === 'SALE' ? '↗' : item.type === 'REFUND' ? '↙' : '↺'}</span>
                      <span>
                        <strong>{operationLabels[item.type]}</strong>
                        <small>{shortId(item.id)}</small>
                      </span>
                      <span className="recent-value">
                        <strong>{formatMoney(item.amount)}</strong>
                        <small className={`status-text ${item.status.toLowerCase()}`}>{statusLabels[item.status]}</small>
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="empty-list">Processed transactions will appear here.</p>
            )}
          </section>
        </aside>
      </section>
    </main>
  )
}

function TransactionReceipt({ transaction }: { transaction: Transaction }) {
  return (
    <section className="receipt" aria-live="polite">
      <div className="receipt-status">
        <span className={`status-dot ${transaction.status.toLowerCase()}`} />
        <div>
          <p className="eyebrow">Result</p>
          <h2>{statusLabels[transaction.status]}</h2>
        </div>
        <strong>{formatMoney(transaction.amount)}</strong>
      </div>
      <dl>
        <div><dt>Operation</dt><dd>{operationLabels[transaction.type]}</dd></div>
        <div><dt>Terminal</dt><dd>{transaction.terminalId}</dd></div>
        <div><dt>Date</dt><dd>{formatDate(transaction.createdAt)}</dd></div>
        <div className="receipt-id"><dt>Transaction</dt><dd>{transaction.id}</dd></div>
        {transaction.failureReason && <div><dt>Reason</dt><dd>{transaction.failureReason}</dd></div>}
      </dl>
    </section>
  )
}

function toDisplayError(error: unknown) {
  if (error instanceof TransactionApiError) {
    return error.details ? Object.values(error.details).join(' · ') : error.message
  }
  return 'Could not connect to the processor. Verify that the backend is running.'
}

function formatMoney(amount: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'UYU' }).format(amount)
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('en-US', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
}

function shortId(id: string) {
  return `${id.slice(0, 8)}…${id.slice(-4)}`
}

export default App
