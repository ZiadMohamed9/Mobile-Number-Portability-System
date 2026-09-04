import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { OperatorSessionService } from '../../core/session/operator-session.service';

@Component({
  selector: 'app-shell',
  imports: [MatButtonModule, RouterLink, RouterOutlet],
  templateUrl: './app-shell.html',
  styleUrl: './app-shell.scss',
})
export class AppShell {
  private readonly router = inject(Router);
  private readonly session = inject(OperatorSessionService);

  protected readonly organizationCode = this.session.organizationCode;

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => e.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  protected readonly isAddNumberActive = computed(() =>
    this.currentUrl().startsWith('/numbers/new'),
  );

  protected readonly isNewRequestActive = computed(() =>
    this.currentUrl().startsWith('/requests/new'),
  );

  protected readonly isRequestsActive = computed(
    () =>
      this.currentUrl().startsWith('/requests') &&
      !this.currentUrl().startsWith('/requests/new'),
  );

  protected changeOrganization(): void {
    this.session.clearOrganization();
    void this.router.navigate(['/organization']);
  }
}
