export interface ApiError {
  error: string;
  message: string;
}

export function isApiError(candidateValue: unknown): candidateValue is ApiError {
  if (typeof candidateValue !== 'object' || candidateValue === null) {
    return false;
  }

  const candidate = candidateValue as Partial<ApiError>;
  return typeof candidate.error === 'string' && typeof candidate.message === 'string';
}
