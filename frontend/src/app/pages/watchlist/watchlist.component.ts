import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { WatchlistService } from '../../services/watchlist.service';
import { Movie } from '../../models/movie.model';

@Component({
  selector: 'app-watchlist',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <h1 class="page-title">Ma Liste</h1>

      <!-- SAVED -->
      <section class="section">
        <h2>🔖 Films sauvegardés</h2>
        @if (loadingSaved()) {
          <div class="loader">Chargement...</div>
        } @else if (saved().length === 0) {
          <p class="empty">Aucun film sauvegardé pour l'instant.</p>
        } @else {
          <div class="movies-grid">
            @for (m of saved(); track m.movieId) {
              <a class="movie-card" [routerLink]="['/movies', m.movieId]">
                @if (m.posterUrl) {
                  <img [src]="m.posterUrl" [alt]="m.title" class="poster" />
                } @else {
                  <div class="poster-placeholder">🎬</div>
                }
                <div class="card-info">
                  <div class="card-title">{{ m.title }}</div>
                  <div class="card-year">{{ m.releaseYear }}</div>
                </div>
                <button class="btn-remove" (click)="unsave(m.movieId, $event)">✕ Retirer</button>
              </a>
            }
          </div>
        }
      </section>

      <!-- WATCHED -->
      <section class="section">
        <h2>✅ Films vus</h2>
        @if (loadingWatched()) {
          <div class="loader">Chargement...</div>
        } @else if (watched().length === 0) {
          <p class="empty">Aucun film marqué comme vu pour l'instant.</p>
        } @else {
          <div class="movies-grid">
            @for (m of watched(); track m.movieId) {
              <a class="movie-card" [routerLink]="['/movies', m.movieId]">
                @if (m.posterUrl) {
                  <img [src]="m.posterUrl" [alt]="m.title" class="poster" />
                } @else {
                  <div class="poster-placeholder">🎬</div>
                }
                <div class="card-info">
                  <div class="card-title">{{ m.title }}</div>
                  <div class="card-year">{{ m.releaseYear }}</div>
                </div>
                <button class="btn-remove" (click)="unwatch(m.movieId, $event)">✕ Retirer</button>
              </a>
            }
          </div>
        }
      </section>
    </div>
  `,
  styles: [`
    .page { max-width: 1200px; margin: 0 auto; padding: 2rem; }
    .page-title { color: #fff; font-size: 2rem; margin-bottom: 2rem; }
    .section { margin-bottom: 3rem; }
    h2 { color: #fff; font-size: 1.3rem; margin-bottom: 1.2rem; border-bottom: 1px solid #2a2a2a; padding-bottom: 0.5rem; }
    .empty { color: #666; font-style: italic; }
    .loader { color: #aaa; }
    .movies-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 1rem;
    }
    .movie-card {
      background: #1a1a1a;
      border: 1px solid #2a2a2a;
      border-radius: 6px;
      overflow: hidden;
      text-decoration: none;
      position: relative;
      transition: border-color 0.2s;
      cursor: pointer;
    }
    .movie-card:hover { border-color: #e50914; }
    .poster { width: 100%; aspect-ratio: 2/3; object-fit: cover; display: block; }
    .poster-placeholder {
      width: 100%; aspect-ratio: 2/3;
      display: flex; align-items: center; justify-content: center;
      background: #0a0a0a; font-size: 3rem;
    }
    .card-info { padding: 0.6rem; }
    .card-title { color: #fff; font-size: 0.85rem; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .card-year { color: #888; font-size: 0.75rem; }
    .btn-remove {
      position: absolute; top: 0.4rem; right: 0.4rem;
      background: rgba(0,0,0,0.8); border: 1px solid #e50914;
      color: #e50914; padding: 0.2rem 0.5rem; border-radius: 4px;
      font-size: 0.7rem; cursor: pointer; transition: all 0.2s;
    }
    .btn-remove:hover { background: #e50914; color: #fff; }
  `]
})
export class WatchlistComponent implements OnInit {
  saved = signal<Movie[]>([]);
  watched = signal<Movie[]>([]);
  loadingSaved = signal(true);
  loadingWatched = signal(true);

  constructor(private watchlistService: WatchlistService) {}

  ngOnInit(): void {
    this.watchlistService.getSaved().subscribe({
      next: (movies) => { this.saved.set(movies); this.loadingSaved.set(false); },
      error: () => this.loadingSaved.set(false)
    });
    this.watchlistService.getWatched().subscribe({
      next: (movies) => { this.watched.set(movies); this.loadingWatched.set(false); },
      error: () => this.loadingWatched.set(false)
    });
  }

  unsave(movieId: string, event: Event): void {
    event.preventDefault();
    this.watchlistService.unsaveMovie(movieId).subscribe(() => {
      this.saved.update(list => list.filter(m => m.movieId !== movieId));
    });
  }

  unwatch(movieId: string, event: Event): void {
    event.preventDefault();
    this.watchlistService.removeWatched(movieId).subscribe(() => {
      this.watched.update(list => list.filter(m => m.movieId !== movieId));
    });
  }
}
