import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreateMobileNumber,
  MobileNumberResponse,
  OperatorOption,
} from '../models/mobile-number.model';

@Injectable({ providedIn: 'root' })
export class MobileNumberApiService {
  private readonly http = inject(HttpClient);

  create(request: CreateMobileNumber): Observable<MobileNumberResponse> {
    return this.http.post<MobileNumberResponse>('/api/mobile-numbers', request);
  }

  getOperators(): Observable<OperatorOption[]> {
    return this.http.get<OperatorOption[]>('/api/operators');
  }
}
