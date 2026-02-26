import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Recommendation } from '../models/recommendation.model';

interface ApiResponse<T> { success: boolean; message: string; data: T; }

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  private readonly base = `${environment.apiUrl}/recommendations`;

  constructor(private http: HttpClient) {}

  getMyRecommendations(): Observable<Recommendation[]> {
    return this.http.get<ApiResponse<Recommendation[]>>(`${this.base}/me`).pipe(map(r => r.data));
  }

  getPopular(): Observable<Recommendation[]> {
    return this.http.get<ApiResponse<Recommendation[]>>(`${this.base}/popular`).pipe(map(r => r.data));
  }

  getSimilar(movieId: string): Observable<Recommendation[]> {
    return this.http.get<ApiResponse<Recommendation[]>>(`${this.base}/movie/${movieId}/similar`).pipe(map(r => r.data));
  }
}
