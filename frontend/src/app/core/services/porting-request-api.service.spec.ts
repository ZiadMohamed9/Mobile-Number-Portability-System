import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PageResponse, PortingRequest } from '../models/porting-request.model';
import { PortingRequestApiService } from './porting-request-api.service';

const EMPTY_PAGE: PageResponse<PortingRequest> = {
  content: [],
  totalElements: 0,
  size: 20,
  number: 2,
};

describe('PortingRequestApiService', () => {
  let api: PortingRequestApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(PortingRequestApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('requests the selected server-side view and page', () => {
    let loadedPage: PageResponse<PortingRequest> | undefined;
    api.list('DONOR', 2, 20).subscribe((page) => (loadedPage = page));

    const request = http.expectOne(
      (candidate) =>
        candidate.url === '/api/porting-requests' && candidate.params.get('view') === 'DONOR',
    );
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('20');

    request.flush(EMPTY_PAGE);
    expect(loadedPage).toEqual(EMPTY_PAGE);
  });

  it('validates an organization with its candidate header before a session exists', () => {
    api.validateOrganization('  OP-A  ').subscribe();

    const request = http.expectOne('/api/porting-requests?view=RECIPIENT&page=0&size=1');
    expect(request.request.headers.get('Organization')).toBe('OP-A');
    request.flush({ ...EMPTY_PAGE, number: 0, size: 1 });
  });
});
