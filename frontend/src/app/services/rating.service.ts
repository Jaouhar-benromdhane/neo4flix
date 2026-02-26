import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Rating, CreateRatingRequest } from '../models/rating.model';

interface ApiResponse<T> { success: boolean; message: string; data: T; }

@Injectable({ providedIn: 'root' })
export class RatingService {
  private readonly base = `${environment.apiUrl}/ratings`;

  constructor(private http: HttpClient) {}

  rateMovie(data: CreateRatingRequest): Observable<Rating> {
    return this.http.post<ApiResponse<Rating>>(this.base, data).pipe(map(r => r.data));
  }

  getMovieRatings(movieId: string): Observable<Rating[]> {
    return this.http.get<ApiResponse<Rating[]>>(`${this.base}/movie/${movieId}`).pipe(map(r => r.data));
  }

  getMyRatings(): Observable<Rating[]> {
    return this.http.get<ApiResponse<Rating[]>>(`${this.base}/user/me`).pipe(map(r => r.data));
  }

  deleteRating(ratingId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${ratingId}`);
  }
}
