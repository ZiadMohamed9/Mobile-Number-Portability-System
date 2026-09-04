import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PortingRequestApiService } from '../services/porting-request-api.service';
import { OperatorSessionService } from '../session/operator-session.service';
import { organizationInterceptor } from './organization.interceptor';

describe('organizationInterceptor', () => {
  let api: PortingRequestApiService;
  let http: HttpTestingController;
  let session: OperatorSessionService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([organizationInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    api = TestBed.inject(PortingRequestApiService);
    http = TestBed.inject(HttpTestingController);
    session = TestBed.inject(OperatorSessionService);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('adds the active organization to normal API requests', () => {
    session.setOrganization('OP-B');
    api.getById(42).subscribe();

    const request = http.expectOne('/api/porting-requests/42');
    expect(request.request.headers.get('Organization')).toBe('OP-B');
    request.flush({});
  });

  it('preserves an explicitly supplied organization during validation', () => {
    session.setOrganization('OP-B');
    api.validateOrganization('OP-C').subscribe();

    const request = http.expectOne('/api/porting-requests?view=RECIPIENT&page=0&size=1');
    expect(request.request.headers.get('Organization')).toBe('OP-C');
    request.flush({});
  });
});
