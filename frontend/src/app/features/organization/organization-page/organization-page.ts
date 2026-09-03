import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiErrorMessageService } from '../../../core/services/api-error-message.service';
import { PortingRequestApiService } from '../../../core/services/porting-request-api.service';
import { OperatorSessionService } from '../../../core/session/operator-session.service';

@Component({
  selector: 'app-organization-page',
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
  ],
  templateUrl: './organization-page.html',
  styleUrl: './organization-page.scss',
})
export class OrganizationPage {
  private readonly api = inject(PortingRequestApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly errorMessages = inject(ApiErrorMessageService);
  private readonly router = inject(Router);
  private readonly session = inject(OperatorSessionService);

  protected readonly organizationCode = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(20)],
  });
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected validateOrganization(): void {
    if (this.organizationCode.invalid || this.loading()) {
      this.organizationCode.markAsTouched();
      return;
    }

    const code = this.organizationCode.value.trim();
    this.loading.set(true);
    this.errorMessage.set(null);

    this.api
      .validateOrganization(code)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: () => {
          this.session.setOrganization(code);
          void this.router.navigate(['/requests']);
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.errorMessages.messageFor(error, 'We could not validate this organization.'),
          );
        },
      });
  }
}
