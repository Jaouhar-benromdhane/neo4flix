import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Movie } from '../models/movie.model';

interface ApiResponse<T> { success: boolean; message: string; data: T; }

@Injectable({ providedIn: 'root' })
export class WatchlistService {
  private base = `${environment.apiUrl}/users/me`;

  constructor(private http: HttpClient) {}

  // ─── WATCHED ────────────────────────────────────────────────────
  markWatched(movieId: string): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.base}/watched/${movieId}`, {}).pipe(map(() => undefined));
  }
  removeWatched(movieId: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/watched/${movieId}`).pipe(map(() => undefined));
  }
  getWatched(): Observable<Movie[]> {
    return this.http.get<ApiResponse<Movie[]>>(`${this.base}/watched`).pipe(map(r => r.data));
  }

  // ─── SAVED ──────────────────────────────────────────────────────
  saveMovie(movieId: string): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.base}/saved/${movieId}`, {}).pipe(map(() => undefined));
  }
  unsaveMovie(movieId: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/saved/${movieId}`).pipe(map(() => undefined));
  }
  getSaved(): Observable<Movie[]> {
    return this.http.get<ApiResponse<Movie[]>>(`${this.base}/saved`).pipe(map(r => r.data));
  }
}
