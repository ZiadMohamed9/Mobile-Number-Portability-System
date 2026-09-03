export type ServiceStatus = 'ACTIVE' | 'SUSPENDED' | 'DISCONNECTED';

export interface CreateMobileNumber {
  phoneNumber: string;
  nationalId: string;
  fullName: string;
  serviceStatus: ServiceStatus;
  currentOperatorSince: string;
  operatorCode: string;
}

export interface MobileNumberResponse {
  phoneNumber: string;
  nationalIdLast4: string;
  serviceStatus: string;
  currentOperatorSince: string;
  originOperatorCode: string;
  currentOperatorCode: string;
}

export interface OperatorOption {
  id: number;
  code: string;
  displayName: string;
  numberPrefix: string;
}
