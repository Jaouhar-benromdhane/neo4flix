import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Movie, CreateMovieRequest } from '../models/movie.model';

interface ApiResponse<T> { success: boolean; message: string; data: T; }

@Injectable({ providedIn: 'root' })
export class MovieService {
  private readonly base = `${environment.apiUrl}/movies`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Movie[]> {
    return this.http.get<ApiResponse<Movie[]>>(this.base).pipe(map(r => r.data));
  }

  getById(id: string): Observable<Movie> {
    return this.http.get<ApiResponse<Movie>>(`${this.base}/${id}`).pipe(map(r => r.data));
  }

  getTopRated(): Observable<Movie[]> {
    return this.http.get<ApiResponse<Movie[]>>(`${this.base}/top-rated`).pipe(map(r => r.data));
  }

  search(query: string): Observable<Movie[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<ApiResponse<Movie[]>>(`${this.base}/search`, { params }).pipe(map(r => r.data));
  }

  create(data: CreateMovieRequest): Observable<Movie> {
    return this.http.post<ApiResponse<Movie>>(this.base, data).pipe(map(r => r.data));
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
