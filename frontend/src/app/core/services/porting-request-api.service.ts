import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreatePortingRequest,
  PageResponse,
  PortingRequest,
  PortingRequestView,
  RejectionReason,
} from '../models/porting-request.model';

const API_PATH = '/api/porting-requests';

@Injectable({ providedIn: 'root' })
export class PortingRequestApiService {
  private readonly http = inject(HttpClient);

  validateOrganization(code: string): Observable<PageResponse<PortingRequest>> {
    return this.http.get<PageResponse<PortingRequest>>(API_PATH, {
      headers: new HttpHeaders({ Organization: code.trim() }),
      params: new HttpParams().set('view', 'RECIPIENT').set('page', 0).set('size', 1),
    });
  }

  list(
    view: PortingRequestView,
    page: number,
    size: number,
  ): Observable<PageResponse<PortingRequest>> {
    const params = new HttpParams().set('view', view).set('page', page).set('size', size);
    return this.http.get<PageResponse<PortingRequest>>(API_PATH, { params });
  }

  getById(id: number): Observable<PortingRequest> {
    return this.http.get<PortingRequest>(`${API_PATH}/${id}`);
  }

  create(request: CreatePortingRequest): Observable<PortingRequest> {
    return this.http.post<PortingRequest>(API_PATH, request);
  }

  accept(id: number): Observable<PortingRequest> {
    return this.http.post<PortingRequest>(`${API_PATH}/${id}/accept`, null);
  }

  reject(id: number, rejectionReason: RejectionReason): Observable<PortingRequest> {
    return this.http.post<PortingRequest>(`${API_PATH}/${id}/reject`, { rejectionReason });
  }
}
