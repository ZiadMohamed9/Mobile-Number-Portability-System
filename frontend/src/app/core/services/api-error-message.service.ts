import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { isApiError } from '../models/api-error.model';

const ERROR_MESSAGES: Readonly<Record<string, string>> = {
  UNKNOWN_ORGANIZATION: 'We could not find that organization code.',
  MOBILE_NUMBER_NOT_FOUND: 'This mobile number was not found.',
  PORTING_REQUEST_NOT_FOUND:
    'This request is unavailable or no longer visible to your organization.',
  NATIONAL_ID_MISMATCH: 'The national ID does not match the subscriber for this number.',
  NUMBER_NOT_ACTIVE: 'This number is not currently active and cannot be ported.',
  SAME_OPERATOR: 'The number already belongs to this operator.',
  MINIMUM_TENURE_NOT_MET: 'The number has not been with its current operator long enough.',
  PENDING_REQUEST_EXISTS: 'A pending porting request already exists for this number.',
  NOT_DONOR: 'Only the donor operator can decide this request.',
  DONOR_NO_LONGER_CURRENT: 'The donor operator is no longer the current operator for this number.',
  REQUEST_NOT_PENDING: 'This request has already been resolved.',
  VALIDATION_ERROR: 'Please check the entered information and try again.',
  MALFORMED_REQUEST: 'The request could not be sent. Please check the entered information.',
  INVALID_PARAMETER: 'The selected request view is invalid.',
};

@Injectable({ providedIn: 'root' })
export class ApiErrorMessageService {
  messageFor(error: unknown, fallback: string): string {
    if (!(error instanceof HttpErrorResponse)) {
      return fallback;
    }

    if (error.status === 0) {
      return 'The service is unavailable. Check your connection and try again.';
    }

    if (isApiError(error.error)) {
      return ERROR_MESSAGES[error.error.error] ?? error.error.message ?? fallback;
    }

    return fallback;
  }
}
