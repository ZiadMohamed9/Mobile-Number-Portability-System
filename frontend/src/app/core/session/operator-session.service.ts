import { Injectable, computed, signal } from '@angular/core';

const ORGANIZATION_STORAGE_KEY = 'mnp.organizationCode';

@Injectable({ providedIn: 'root' })
export class OperatorSessionService {
  private readonly organizationCodeState = signal<string | null>(this.readStoredCode());

  readonly organizationCode = this.organizationCodeState.asReadonly();
  readonly hasOrganization = computed(() => this.organizationCodeState() !== null);

  setOrganization(code: string): void {
    const normalizedCode = code.trim().toUpperCase();
    localStorage.setItem(ORGANIZATION_STORAGE_KEY, normalizedCode);
    this.organizationCodeState.set(normalizedCode);
  }

  clearOrganization(): void {
    localStorage.removeItem(ORGANIZATION_STORAGE_KEY);
    this.organizationCodeState.set(null);
  }

  private readStoredCode(): string | null {
    const storedCode = localStorage.getItem(ORGANIZATION_STORAGE_KEY)?.trim();
    return storedCode ? storedCode.toUpperCase() : null;
  }
}
