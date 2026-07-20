export type TransactionType = 'SALE' | 'REFUND' | 'REVERSAL'

export type TransactionStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'DECLINED'
  | 'REFUNDED'
  | 'REVERSED'
  | 'PENDING_RETRY'

export interface CreateTransactionRequest {
  terminalId: string
  amount: number
  type: TransactionType
}

export interface Transaction {
  id: string
  terminalId: string
  amount: number
  type: TransactionType
  status: TransactionStatus
  createdAt: string
  updatedAt: string
  failureReason?: string
}

export interface ApiErrorPayload {
  status: number
  error: string
  message: string
  details?: Record<string, string>
}

