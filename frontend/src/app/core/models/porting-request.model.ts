export type PortingRequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED_TIMEOUT';

export type RejectionReason =
  'IDENTITY_MISMATCH' | 'OUTSTANDING_BALANCE' | 'ACCOUNT_RESTRICTION' | 'FRAUD_SUSPECTED';

export type PortingRequestView = 'ACCEPTED' | 'RECIPIENT' | 'DONOR';
export type RequestViewRoute = 'accepted' | 'recipient' | 'donor';

export interface PortingRequest {
  id: number;
  phoneNumber: string;
  donorOperatorCode: string;
  recipientOperatorCode: string;
  status: PortingRequestStatus;
  requestedAt: string;
  expiresAt: string;
  resolvedAt: string | null;
  rejectionReason: RejectionReason | null;
}

export interface CreatePortingRequest {
  phoneNumber: string;
  nationalId: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  size: number;
  number: number;
}
