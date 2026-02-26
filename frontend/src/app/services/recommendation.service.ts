import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Recommendation } from '../models/recommendation.model';

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  private readonly base = `${environment.apiUrl}/recommendations`;

  constructor(private http: HttpClient) {}

  getMyRecommendations(): Observable<Recommendation[]> {
    return this.http.get<Recommendation[]>(`${this.base}/me`);
  }

  getPopular(): Observable<Recommendation[]> {
    return this.http.get<Recommendation[]>(`${this.base}/popular`);
  }

  getSimilar(movieId: string): Observable<Recommendation[]> {
    return this.http.get<Recommendation[]>(`${this.base}/movie/${movieId}/similar`);
  }
}
