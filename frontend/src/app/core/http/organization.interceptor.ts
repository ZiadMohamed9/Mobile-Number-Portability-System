import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { OperatorSessionService } from '../session/operator-session.service';

export const organizationInterceptor: HttpInterceptorFn = (request, next) => {
  const organizationCode = inject(OperatorSessionService).organizationCode();

  if (!organizationCode || request.headers.has('Organization')) {
    return next(request);
  }

  return next(request.clone({ setHeaders: { Organization: organizationCode } }));
};
