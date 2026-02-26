import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

interface ApiResponse<T> { success: boolean; message: string; data: T; }

export interface TwoFactorSetup { secret: string; qrCodeBase64: string; }
export interface TwoFactorStatus { twoFactorEnabled: boolean; }

@Injectable({ providedIn: 'root' })
export class TwoFactorService {
  private base = `${environment.apiUrl}/auth/2fa`;

  constructor(private http: HttpClient) {}

  setup(): Observable<TwoFactorSetup> {
    return this.http.post<ApiResponse<TwoFactorSetup>>(`${this.base}/setup`, {}).pipe(map(r => r.data));
  }

  enable(code: string): Observable<string> {
    return this.http.post<ApiResponse<string>>(`${this.base}/enable`, { code }).pipe(map(r => r.message));
  }

  disable(code: string): Observable<string> {
    return this.http.post<ApiResponse<string>>(`${this.base}/disable`, { code }).pipe(map(r => r.message));
  }

  getStatus(): Observable<TwoFactorStatus> {
    return this.http.get<ApiResponse<TwoFactorStatus>>(`${this.base}/status`).pipe(map(r => r.data));
  }
}
