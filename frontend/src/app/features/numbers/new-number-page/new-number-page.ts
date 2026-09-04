import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import {
  MobileNumberResponse,
  OperatorOption,
  ServiceStatus,
} from '../../../core/models/mobile-number.model';
import { ApiErrorMessageService } from '../../../core/services/api-error-message.service';
import { MobileNumberApiService } from '../../../core/services/mobile-number-api.service';
import { PortingRequestDraftService } from '../../../core/services/porting-request-draft.service';

@Component({
  selector: 'app-new-number-page',
  imports: [
    MatButtonModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    ReactiveFormsModule,
  ],
  templateUrl: './new-number-page.html',
  styleUrl: './new-number-page.scss',
})
export class NewNumberPage implements OnInit {
  private readonly api = inject(MobileNumberApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly errorMessages = inject(ApiErrorMessageService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly draftService = inject(PortingRequestDraftService);

  protected readonly operators = signal<OperatorOption[]>([]);
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly success = signal<MobileNumberResponse | null>(null);
  protected readonly maxDate = new Date();

  protected readonly serviceStatuses: { value: ServiceStatus; label: string }[] = [
    { value: 'ACTIVE', label: 'Active' },
    { value: 'SUSPENDED', label: 'Suspended' },
    { value: 'DISCONNECTED', label: 'Disconnected' },
  ];

  protected readonly numberForm = new FormGroup(
    {
      operatorCode: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      phoneNumber: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(/^01\d{9}$/)],
      }),
      nationalId: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(/^\d{14}$/)],
      }),
      fullName: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.maxLength(150)],
      }),
      serviceStatus: new FormControl<ServiceStatus>('ACTIVE', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      currentOperatorSince: new FormControl<Date | null>(null, {
        validators: [Validators.required],
      }),
    },
    { validators: [this.prefixMatchValidator.bind(this)] },
  );

  ngOnInit(): void {
    this.api
      .getOperators()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (ops) => this.operators.set(ops),
      });
  }

  protected submitNumber(): void {
    if (this.numberForm.invalid || this.loading()) {
      this.numberForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const raw = this.numberForm.getRawValue();
    const dateValue = raw.currentOperatorSince!;
    const year = dateValue.getFullYear();
    const month = String(dateValue.getMonth() + 1).padStart(2, '0');
    const day = String(dateValue.getDate()).padStart(2, '0');

    this.api
      .create({
        phoneNumber: raw.phoneNumber,
        nationalId: raw.nationalId,
        fullName: raw.fullName,
        serviceStatus: raw.serviceStatus,
        currentOperatorSince: `${year}-${month}-${day}`,
        operatorCode: raw.operatorCode,
      })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (response) => {
          this.success.set(response);
          this.snackBar.open('Mobile number created.', 'Dismiss', { duration: 4000 });
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.errorMessages.messageFor(error, 'The mobile number could not be created.'),
          );
        },
      });
  }

  protected createAnother(): void {
    this.success.set(null);
    this.errorMessage.set(null);
    this.numberForm.reset({ serviceStatus: 'ACTIVE' });
  }

  protected startPortingRequest(): void {
    const result = this.success();
    if (!result) return;

    this.draftService.store({
      phoneNumber: result.phoneNumber,
      nationalId: this.numberForm.getRawValue().nationalId,
    });
    void this.router.navigate(['/requests/new']);
  }

  protected get prefixError(): boolean {
    return this.numberForm.hasError('prefixMismatch');
  }

  private prefixMatchValidator(group: AbstractControl): ValidationErrors | null {
    const formGroup = group as FormGroup;
    const operatorCode = formGroup.get('operatorCode')?.value;
    const phoneNumber = formGroup.get('phoneNumber')?.value;

    if (!operatorCode || !phoneNumber || phoneNumber.length < 4) {
      return null;
    }

    const selectedOperator = this.operators().find((op) => op.code === operatorCode);
    if (!selectedOperator) {
      return null;
    }

    const phonePrefix = phoneNumber.substring(0, 3);
    if (phonePrefix !== selectedOperator.numberPrefix) {
      return { prefixMismatch: true };
    }

    return null;
  }
}
