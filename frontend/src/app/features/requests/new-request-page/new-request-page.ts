import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiErrorMessageService } from '../../../core/services/api-error-message.service';
import { PortingRequestApiService } from '../../../core/services/porting-request-api.service';

@Component({
  selector: 'app-new-request-page',
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    ReactiveFormsModule,
  ],
  templateUrl: './new-request-page.html',
  styleUrl: './new-request-page.scss',
})
export class NewRequestPage {
  private readonly api = inject(PortingRequestApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly errorMessages = inject(ApiErrorMessageService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly requestForm = new FormGroup({
    phoneNumber: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^01\d{9}$/)],
    }),
    nationalId: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{14}$/)],
    }),
  });
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected submitRequest(): void {
    if (this.requestForm.invalid || this.loading()) {
      this.requestForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.api
      .create(this.requestForm.getRawValue())
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (request) => {
          this.snackBar.open('Porting request created.', 'Dismiss', { duration: 4000 });
          void this.router.navigate(['/requests', request.id], {
            queryParams: { view: 'recipient' },
          });
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.errorMessages.messageFor(error, 'The porting request could not be created.'),
          );
        },
      });
  }
}
