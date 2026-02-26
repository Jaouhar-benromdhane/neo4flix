import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MovieService } from '../../services/movie.service';
import { Movie } from '../../models/movie.model';

@Component({
  selector: 'app-movies',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page">
      <div class="page-header">
        <h1>Catalogue de films</h1>
        <div class="search-bar">
          <input type="text" [(ngModel)]="searchQuery" (input)="onSearch()"
            placeholder="Rechercher un film..." />
        </div>
      </div>

      @if (loading()) {
        <div class="loader">Chargement...</div>
      }

      @if (error()) {
        <div class="error-banner">{{ error() }}</div>
      }

      <div class="movies-grid">
        @for (movie of movies(); track movie.movieId) {
          <a class="movie-card" [routerLink]="['/movies', movie.movieId]">
            <div class="movie-poster">
              @if (movie.posterUrl) {
                <img [src]="movie.posterUrl" [alt]="movie.title" (error)="onImgError($event)" />
              } @else {
                <div class="poster-placeholder">🎬</div>
              }
            </div>
            <div class="movie-info">
              <h3>{{ movie.title }}</h3>
              <p class="movie-meta">
                <span class="year">{{ movie.year }}</span>
                @if (movie.averageRating > 0) {
                  <span class="rating">⭐ {{ movie.averageRating | number:'1.1-1' }}</span>
                }
              </p>
              @if (movie.genres?.length) {
                <div class="genres">
                  @for (g of movie.genres.slice(0, 2); track g.name) {
                    <span class="genre-tag">{{ g.name }}</span>
                  }
                </div>
              }
            </div>
          </a>
        }
      </div>

      @if (!loading() && movies().length === 0 && !error()) {
        <div class="empty-state">
          <p>Aucun film trouvé.</p>
        </div>
      }
    </div>
  `,
  styles: [`
    .page { max-width: 1200px; margin: 0 auto; padding: 2rem; }
    .page-header {
      display: flex; align-items: center; justify-content: space-between;
      margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;
    }
    h1 { color: #fff; font-size: 1.8rem; margin: 0; }
    .search-bar input {
      padding: 0.6rem 1rem; background: #1a1a1a; border: 1px solid #333;
      border-radius: 4px; color: #fff; font-size: 0.95rem; width: 280px;
    }
    .search-bar input:focus { outline: none; border-color: #e50914; }
    .movies-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 1.5rem;
    }
    .movie-card {
      background: #1a1a1a; border: 1px solid #2a2a2a; border-radius: 8px;
      overflow: hidden; text-decoration: none; transition: transform 0.2s, border-color 0.2s;
    }
    .movie-card:hover { transform: translateY(-4px); border-color: #e50914; }
    .movie-poster { aspect-ratio: 2/3; background: #0a0a0a; overflow: hidden; }
    .movie-poster img { width: 100%; height: 100%; object-fit: cover; }
    .poster-placeholder {
      width: 100%; height: 100%; display: flex;
      align-items: center; justify-content: center; font-size: 3rem;
    }
    .movie-info { padding: 0.8rem; }
    h3 { color: #fff; font-size: 0.95rem; margin: 0 0 0.4rem; line-height: 1.3; }
    .movie-meta { display: flex; gap: 0.8rem; margin: 0 0 0.5rem; }
    .year { color: #888; font-size: 0.8rem; }
    .rating { color: #f5c518; font-size: 0.8rem; }
    .genres { display: flex; flex-wrap: wrap; gap: 0.3rem; }
    .genre-tag {
      background: #2a2a2a; color: #aaa; padding: 0.2rem 0.5rem;
      border-radius: 3px; font-size: 0.7rem;
    }
    .loader { color: #aaa; text-align: center; padding: 3rem; }
    .error-banner {
      background: #3a0a0a; border: 1px solid #e50914; color: #e50914;
      padding: 1rem; border-radius: 4px; margin-bottom: 1rem;
    }
    .empty-state { color: #aaa; text-align: center; padding: 3rem; }
  `]
})
export class MoviesComponent implements OnInit {
  movies = signal<Movie[]>([]);
  loading = signal(true);
  error = signal('');
  searchQuery = '';
  private allMovies: Movie[] = [];

  constructor(private movieService: MovieService) {}

  ngOnInit(): void {
    this.movieService.getAll().subscribe({
      next: (films) => {
        this.allMovies = films;
        this.movies.set(films);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Impossible de charger les films. Backend démarré ?');
        this.loading.set(false);
      }
    });
  }

  onSearch(): void {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) {
      this.movies.set(this.allMovies);
    } else {
      this.movies.set(this.allMovies.filter(m =>
        m.title.toLowerCase().includes(q) ||
        m.genres?.some(g => g.name.toLowerCase().includes(q))
      ));
    }
  }

  onImgError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
  }
}
