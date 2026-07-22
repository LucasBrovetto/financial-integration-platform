import type {
  ApiErrorPayload,
  CreateTransactionRequest,
  Transaction,
} from '../types/transaction'

export class TransactionApiError extends Error {
  readonly status: number
  readonly code: string
  readonly details?: Record<string, string>

  constructor(payload: ApiErrorPayload) {
    super(payload.message)
    this.name = 'TransactionApiError'
    this.status = payload.status
    this.code = payload.error
    this.details = payload.details
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init?.body ? { 'Content-Type': 'application/json' } : {}),
      ...init?.headers,
    },
  })

  if (!response.ok) {
    const fallback: ApiErrorPayload = {
      status: response.status,
      error: 'REQUEST_FAILED',
      message: 'The operation could not be completed.',
    }
    const payload = await response.json().catch(() => fallback)
    throw new TransactionApiError(payload as ApiErrorPayload)
  }

  return response.json() as Promise<T>
}

export function createTransaction(payload: CreateTransactionRequest) {
  return request<Transaction>('/transactions', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getTransaction(transactionId: string) {
  return request<Transaction>(`/transactions/${encodeURIComponent(transactionId)}`)
}
