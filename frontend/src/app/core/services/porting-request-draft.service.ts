import { Injectable } from '@angular/core';

export interface PortingRequestDraft {
  phoneNumber: string;
  nationalId: string;
}

@Injectable({ providedIn: 'root' })
export class PortingRequestDraftService {
  private draft: PortingRequestDraft | null = null;

  store(draft: PortingRequestDraft): void {
    this.draft = draft;
  }

  consume(): PortingRequestDraft | null {
    const current = this.draft;
    this.draft = null;
    return current;
  }
}
