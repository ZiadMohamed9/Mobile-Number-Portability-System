import { DatePipe } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { filter, finalize, switchMap } from 'rxjs';
import {
  PortingRequest,
  RejectionReason,
  RequestViewRoute,
} from '../../../core/models/porting-request.model';
import { ApiErrorMessageService } from '../../../core/services/api-error-message.service';
import { PortingRequestApiService } from '../../../core/services/porting-request-api.service';
import { OperatorSessionService } from '../../../core/session/operator-session.service';
import { StatusBadge } from '../../../shared/status-badge/status-badge';
import { AcceptRequestDialog } from '../decision-dialogs/accept-request-dialog/accept-request-dialog';
import { RejectRequestDialog } from '../decision-dialogs/reject-request-dialog/reject-request-dialog';

const REJECTION_LABELS: Readonly<Record<RejectionReason, string>> = {
  IDENTITY_MISMATCH: 'Identity mismatch',
  OUTSTANDING_BALANCE: 'Outstanding balance',
  ACCOUNT_RESTRICTION: 'Account restriction',
  FRAUD_SUSPECTED: 'Fraud suspected',
};

@Component({
  selector: 'app-request-detail-page',
  imports: [
    DatePipe,
    MatButtonModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    RouterLink,
    StatusBadge,
  ],
  templateUrl: './request-detail-page.html',
  styleUrl: './request-detail-page.scss',
})
export class RequestDetailPage {
  private readonly api = inject(PortingRequestApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialog = inject(MatDialog);
  private readonly errorMessages = inject(ApiErrorMessageService);
  private readonly route = inject(ActivatedRoute);
  private readonly session = inject(OperatorSessionService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly request = signal<PortingRequest | null>(null);
  protected readonly loading = signal(true);
  protected readonly actionLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);
  protected readonly canDecide = computed(() => {
    const request = this.request();
    const activeOrg = this.session.organizationCode();
    return (
      request?.status === 'PENDING' &&
      !!activeOrg &&
      request.donorOperatorCode.toUpperCase() === activeOrg.toUpperCase()
    );
  });
  protected readonly backView = computed<RequestViewRoute>(() => {
    const viewValue = this.route.snapshot.queryParamMap.get('view');
    if (viewValue === 'recipient' || viewValue === 'donor' || viewValue === 'accepted') {
      return viewValue;
    }
    return this.canDecide() ? 'donor' : 'accepted';
  });

  constructor() {
    this.loadRequest();
  }

  protected rejectionLabel(reason: RejectionReason | null): string {
    return reason ? REJECTION_LABELS[reason] : 'Not provided';
  }

  protected reloadRequest(): void {
    this.loadRequest();
  }

  protected openAcceptDialog(): void {
    this.dialog
      .open(AcceptRequestDialog, { autoFocus: 'dialog', restoreFocus: true })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => {
          this.beginAction();
          return this.api
            .accept(this.request()!.id)
            .pipe(finalize(() => this.actionLoading.set(false)));
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (request) => this.completeAction(request, 'Request accepted.'),
        error: (error: unknown) => this.failAction(error, 'The request could not be accepted.'),
      });
  }

  protected openRejectDialog(): void {
    this.dialog
      .open(RejectRequestDialog, { autoFocus: 'dialog', restoreFocus: true })
      .afterClosed()
      .pipe(
        filter((reason): reason is RejectionReason => reason !== null && reason !== undefined),
        switchMap((reason) => {
          this.beginAction();
          return this.api
            .reject(this.request()!.id, reason)
            .pipe(finalize(() => this.actionLoading.set(false)));
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (request) => this.completeAction(request, 'Request rejected.'),
        error: (error: unknown) => this.failAction(error, 'The request could not be rejected.'),
      });
  }

  private loadRequest(): void {
    const requestId = this.readRequestId();
    if (requestId === null) {
      this.showInvalidRequest();
      return;
    }

    this.beginLoad();
    this.api
      .getById(requestId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (request) => this.request.set(request),
        error: (error: unknown) => this.showLoadFailure(error),
      });
  }

  private readRequestId(): number | null {
    const requestId = Number(this.route.snapshot.paramMap.get('id'));
    return Number.isSafeInteger(requestId) && requestId > 0 ? requestId : null;
  }

  private beginLoad(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
  }

  private showInvalidRequest(): void {
    this.loading.set(false);
    this.errorMessage.set('This request number is invalid.');
  }

  private showLoadFailure(error: unknown): void {
    this.errorMessage.set(
      this.errorMessages.messageFor(error, 'The request details could not be loaded.'),
    );
  }

  private beginAction(): void {
    this.actionLoading.set(true);
    this.actionError.set(null);
  }

  private completeAction(request: PortingRequest, message: string): void {
    this.request.set(request);
    this.snackBar.open(message, 'Dismiss', { duration: 4000 });
  }

  private failAction(error: unknown, fallback: string): void {
    this.actionError.set(this.errorMessages.messageFor(error, fallback));
  }
}
