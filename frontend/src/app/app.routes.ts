import { Routes } from '@angular/router';
import { operatorSessionGuard } from './core/guards/operator-session.guard';

export const routes: Routes = [
  {
    path: 'organization',
    loadComponent: () =>
      import('./features/organization/organization-page/organization-page').then(
        (component) => component.OrganizationPage,
      ),
    title: 'Organization | MNP Console',
  },
  {
    path: '',
    canActivate: [operatorSessionGuard],
    loadComponent: () =>
      import('./layout/app-shell/app-shell').then((component) => component.AppShell),
    children: [
      {
        path: 'numbers/new',
        loadComponent: () =>
          import('./features/numbers/new-number-page/new-number-page').then(
            (component) => component.NewNumberPage,
          ),
        title: 'Add number | MNP Console',
      },
      {
        path: 'requests/new',
        loadComponent: () =>
          import('./features/requests/new-request-page/new-request-page').then(
            (component) => component.NewRequestPage,
          ),
        title: 'New request | MNP Console',
      },
      {
        path: 'requests/:id',
        loadComponent: () =>
          import('./features/requests/request-detail-page/request-detail-page').then(
            (component) => component.RequestDetailPage,
          ),
        title: 'Request details | MNP Console',
      },
      {
        path: 'requests',
        loadComponent: () =>
          import('./features/requests/request-list-page/request-list-page').then(
            (component) => component.RequestListPage,
          ),
        title: 'Requests | MNP Console',
      },
      { path: '', pathMatch: 'full', redirectTo: 'requests' },
    ],
  },
  { path: '**', redirectTo: '' },
];
