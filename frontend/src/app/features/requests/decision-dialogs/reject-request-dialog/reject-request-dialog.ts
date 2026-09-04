import { Component, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { RejectionReason } from '../../../../core/models/porting-request.model';

interface RejectionOption {
  reason: RejectionReason;
  label: string;
}

@Component({
  selector: 'app-reject-request-dialog',
  imports: [MatButtonModule, MatFormFieldModule, MatSelectModule, ReactiveFormsModule],
  templateUrl: './reject-request-dialog.html',
  styleUrl: './reject-request-dialog.scss',
})
export class RejectRequestDialog {
  private readonly dialogRef = inject(MatDialogRef<RejectRequestDialog, RejectionReason | null>);

  protected readonly reasons: readonly RejectionOption[] = [
    { reason: 'IDENTITY_MISMATCH', label: 'Identity mismatch' },
    { reason: 'OUTSTANDING_BALANCE', label: 'Outstanding balance' },
    { reason: 'ACCOUNT_RESTRICTION', label: 'Account restriction' },
    { reason: 'FRAUD_SUSPECTED', label: 'Fraud suspected' },
  ];
  protected readonly rejectionReason = new FormControl<RejectionReason | null>(
    null,
    Validators.required,
  );

  protected cancel(): void {
    this.dialogRef.close(null);
  }

  protected confirm(): void {
    if (this.rejectionReason.invalid) {
      this.rejectionReason.markAsTouched();
      return;
    }

    this.dialogRef.close(this.rejectionReason.value);
  }
}
