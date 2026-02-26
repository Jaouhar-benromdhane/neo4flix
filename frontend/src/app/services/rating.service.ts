import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Rating, CreateRatingRequest } from '../models/rating.model';

@Injectable({ providedIn: 'root' })
export class RatingService {
  private readonly base = `${environment.apiUrl}/ratings`;

  constructor(private http: HttpClient) {}

  rateMovie(data: CreateRatingRequest): Observable<Rating> {
    return this.http.post<Rating>(this.base, data);
  }

  getMovieRatings(movieId: string): Observable<Rating[]> {
    return this.http.get<Rating[]>(`${this.base}/movie/${movieId}`);
  }

  getMyRatings(): Observable<Rating[]> {
    return this.http.get<Rating[]>(`${this.base}/user/me`);
  }

  deleteRating(ratingId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${ratingId}`);
  }
}
