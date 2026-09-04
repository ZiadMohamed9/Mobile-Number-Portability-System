import { Component, computed, input } from '@angular/core';
import { PortingRequestStatus } from '../../core/models/porting-request.model';

const STATUS_LABELS: Readonly<Record<PortingRequestStatus, string>> = {
  PENDING: 'Pending',
  ACCEPTED: 'Accepted',
  REJECTED: 'Rejected',
  CANCELLED_TIMEOUT: 'Timed out',
};

@Component({
  selector: 'app-status-badge',
  templateUrl: './status-badge.html',
  styleUrl: './status-badge.scss',
})
export class StatusBadge {
  readonly status = input.required<PortingRequestStatus>();
  protected readonly label = computed(() => STATUS_LABELS[this.status()]);
  protected readonly modifierClass = computed(() => `status-badge--${this.status().toLowerCase()}`);
}
