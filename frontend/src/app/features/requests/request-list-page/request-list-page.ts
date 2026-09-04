import { DatePipe } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  BehaviorSubject,
  Observable,
  catchError,
  combineLatest,
  map,
  of,
  switchMap,
  tap,
} from 'rxjs';
import {
  PageResponse,
  PortingRequest,
  PortingRequestView,
  RequestViewRoute,
} from '../../../core/models/porting-request.model';
import { ApiErrorMessageService } from '../../../core/services/api-error-message.service';
import { PortingRequestApiService } from '../../../core/services/porting-request-api.service';
import { StatusBadge } from '../../../shared/status-badge/status-badge';

interface RequestTab {
  routeValue: RequestViewRoute;
  apiValue: PortingRequestView;
  label: string;
  description: string;
  emptyTitle: string;
  emptyMessage: string;
}

interface RequestQuery {
  tab: RequestTab;
  page: number;
  size: number;
}

interface RequestLoadOutcome {
  page: PageResponse<PortingRequest> | null;
  error: string | null;
}

const DEFAULT_PAGE_SIZE = 10;

const REQUEST_TABS: readonly RequestTab[] = [
  {
    routeValue: 'accepted',
    apiValue: 'ACCEPTED',
    label: 'Accepted',
    description: 'Completed transfers accepted across all operators.',
    emptyTitle: 'No accepted requests yet',
    emptyMessage: 'Accepted requests from the network will appear here.',
  },
  {
    routeValue: 'recipient',
    apiValue: 'RECIPIENT',
    label: 'Outgoing',
    description: 'Requests submitted by your organization as the recipient.',
    emptyTitle: 'No outgoing requests',
    emptyMessage: 'Create a porting request to begin tracking it here.',
  },
  {
    routeValue: 'donor',
    apiValue: 'DONOR',
    label: 'Incoming',
    description: 'Requests sent to your organization as the donor.',
    emptyTitle: 'No incoming requests',
    emptyMessage: 'Requests awaiting or recording your decisions will appear here.',
  },
];

@Component({
  selector: 'app-request-list-page',
  imports: [DatePipe, MatButtonModule, MatPaginatorModule, MatTabsModule, RouterLink, StatusBadge],
  templateUrl: './request-list-page.html',
  styleUrl: './request-list-page.scss',
})
export class RequestListPage {
  private readonly api = inject(PortingRequestApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly errorMessages = inject(ApiErrorMessageService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly refreshRequest = new BehaviorSubject(0);

  protected readonly tabs = REQUEST_TABS;
  protected readonly skeletonRows = [1, 2, 3, 4, 5];
  protected readonly activeTab = signal<RequestTab>(REQUEST_TABS[0]);
  protected readonly requests = signal<PortingRequest[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(DEFAULT_PAGE_SIZE);
  protected readonly totalElements = signal(0);
  protected readonly emptyTitle = computed(() => this.activeTab().emptyTitle);
  protected readonly emptyMessage = computed(() => this.activeTab().emptyMessage);

  constructor() {
    combineLatest([this.route.queryParamMap, this.refreshRequest])
      .pipe(
        map(([params]) =>
          this.toRequestQuery(params.get('view'), params.get('page'), params.get('size')),
        ),
        tap((query) => this.beginLoad(query)),
        switchMap((query) => this.loadPage(query)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((loadOutcome) => this.applyLoadOutcome(loadOutcome.page, loadOutcome.error));
  }

  protected changePage(event: PageEvent): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        view: this.activeTab().routeValue,
        page: event.pageIndex,
        size: event.pageSize,
      },
    });
  }

  protected reloadRequests(): void {
    this.refreshRequest.next(this.refreshRequest.value + 1);
  }

  private beginLoad(query: RequestQuery): void {
    this.activeTab.set(query.tab);
    this.pageIndex.set(query.page);
    this.pageSize.set(query.size);
    this.loading.set(true);
    this.errorMessage.set(null);
  }

  private loadPage(query: RequestQuery): Observable<RequestLoadOutcome> {
    return this.api.list(query.tab.apiValue, query.page, query.size).pipe(
      map((page) => ({ page, error: null })),
      catchError((error: unknown) =>
        of({
          page: null,
          error: this.errorMessages.messageFor(error, 'Requests could not be loaded.'),
        }),
      ),
    );
  }

  private toRequestQuery(
    viewValue: string | null,
    pageValue: string | null,
    sizeValue: string | null,
  ): RequestQuery {
    const tab =
      REQUEST_TABS.find((candidate) => candidate.routeValue === viewValue) ?? REQUEST_TABS[0];
    return {
      tab,
      page: this.toNonNegativeInteger(pageValue, 0),
      size: this.toPositiveInteger(sizeValue, DEFAULT_PAGE_SIZE),
    };
  }

  private toNonNegativeInteger(rawValue: string | null, fallback: number): number {
    const parsed = Number(rawValue);
    return Number.isInteger(parsed) && parsed >= 0 ? parsed : fallback;
  }

  private toPositiveInteger(rawValue: string | null, fallback: number): number {
    const parsed = Number(rawValue);
    return [10, 20, 50].includes(parsed) ? parsed : fallback;
  }

  private applyLoadOutcome(page: PageResponse<PortingRequest> | null, error: string | null): void {
    this.loading.set(false);
    this.errorMessage.set(error);

    if (!page) {
      this.requests.set([]);
      this.totalElements.set(0);
      return;
    }

    this.requests.set(page.content);
    this.totalElements.set(page.totalElements);
    this.pageIndex.set(page.number);
    this.pageSize.set(page.size);
  }
}
