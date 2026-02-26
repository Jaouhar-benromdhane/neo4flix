import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Movie, CreateMovieRequest } from '../models/movie.model';

@Injectable({ providedIn: 'root' })
export class MovieService {
  private readonly base = `${environment.apiUrl}/movies`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Movie[]> {
    return this.http.get<Movie[]>(this.base);
  }

  getById(id: string): Observable<Movie> {
    return this.http.get<Movie>(`${this.base}/${id}`);
  }

  getTopRated(): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.base}/top-rated`);
  }

  search(query: string): Observable<Movie[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<Movie[]>(`${this.base}/search`, { params });
  }

  create(data: CreateMovieRequest): Observable<Movie> {
    return this.http.post<Movie>(this.base, data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
