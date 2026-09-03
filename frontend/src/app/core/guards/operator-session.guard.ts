import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { OperatorSessionService } from '../session/operator-session.service';

export const operatorSessionGuard: CanActivateFn = () => {
  const session = inject(OperatorSessionService);
  const router = inject(Router);

  return session.hasOrganization() ? true : router.createUrlTree(['/organization']);
};
