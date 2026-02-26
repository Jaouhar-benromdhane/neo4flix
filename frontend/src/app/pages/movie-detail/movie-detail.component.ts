import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MovieService } from '../../services/movie.service';
import { RatingService } from '../../services/rating.service';
import { RecommendationService } from '../../services/recommendation.service';
import { AuthService } from '../../services/auth.service';
import { WatchlistService } from '../../services/watchlist.service';
import { Movie } from '../../models/movie.model';
import { Rating } from '../../models/rating.model';
import { Recommendation } from '../../models/recommendation.model';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      @if (loading()) {
        <div class="loader">Chargement...</div>
      }

      @if (movie()) {
        <div class="movie-hero">
          <div class="hero-poster">
            @if (movie()!.posterUrl) {
              <img [src]="movie()!.posterUrl" [alt]="movie()!.title" />
            } @else {
              <div class="poster-placeholder">🎬</div>
            }
          </div>
          <div class="hero-details">
            <h1>{{ movie()!.title }}</h1>
            <div class="meta-row">
              <span class="year">{{ movie()!.releaseYear }}</span>
              @if (movie()!.averageRating > 0) {
                <span class="avg-rating">⭐ {{ movie()!.averageRating | number:'1.1-1' }} / 10</span>
                <span class="vote-count">({{ movie()!.totalRatings }} votes)</span>
              }
            </div>
            @if (movie()!.genres?.length) {
              <div class="genres">
                @for (g of movie()!.genres; track g) {
                  <span class="genre-tag">{{ g }}</span>
                }
              </div>
            }
            @if (movie()!.directors?.length) {
              <p class="crew">
                <strong>Réalisateur :</strong>
                {{ directorNames() }}
              </p>
            }
            @if (movie()!.actors?.length) {
              <p class="crew">
                <strong>Acteurs :</strong>
                {{ actorNames() }}
              </p>
            }
            <p class="synopsis">{{ movie()!.synopsis }}</p>

            <!-- Watchlist buttons -->
            @if (auth.isLoggedIn()) {
              <div class="watchlist-actions">
                <button class="btn-watch" [class.active]="isWatched()" (click)="toggleWatched()">
                  {{ isWatched() ? '✅ Vu' : '👁 Marquer comme vu' }}
                </button>
                <button class="btn-save" [class.active]="isSaved()" (click)="toggleSaved()">
                  {{ isSaved() ? '🔖 Sauvegardé' : '+ Sauvegarder' }}
                </button>
              </div>
            }
          </div>
        </div>

        <!-- Rating form -->
        @if (auth.isLoggedIn()) {
          <div class="section">
            <h2>Donner une note</h2>
            <form [formGroup]="ratingForm" (ngSubmit)="submitRating()" class="rating-form">
              <div class="field-row">
                <div class="field">
                  <label>Note (1-10)</label>
                  <input type="number" formControlName="score" min="1" max="10" step="0.5" />
                </div>
                <div class="field flex-grow">
                  <label>Commentaire (optionnel)</label>
                  <input type="text" formControlName="comment" placeholder="Votre avis..." />
                </div>
                <button type="submit" [disabled]="ratingForm.invalid || ratingLoading()" class="btn-rate">
                  {{ ratingLoading() ? '...' : 'Noter' }}
                </button>
              </div>
              @if (ratingSuccess()) {
                <div class="success-msg">Note envoyée !</div>
              }
              @if (ratingError()) {
                <div class="error-msg">{{ ratingError() }}</div>
              }
            </form>
          </div>
        }

        <!-- Ratings list -->
        @if (ratings().length) {
          <div class="section">
            <h2>Avis des utilisateurs ({{ ratings().length }})</h2>
            <div class="ratings-list">
              @for (r of ratings(); track r.ratingId) {
                <div class="rating-item">
                  <div class="rating-score">{{ r.score }}/10</div>
                  <div class="rating-body">
                    @if (r.comment) {
                      <p>{{ r.comment }}</p>
                    }
                    <span class="rating-date">{{ r.createdAt | date:'dd/MM/yyyy' }}</span>
                  </div>
                </div>
              }
            </div>
          </div>
        }

        <!-- Similar movies -->
        @if (similar().length) {
          <div class="section">
            <h2>Films similaires</h2>
            <div class="similar-grid">
              @for (rec of similar().slice(0, 4); track rec.movieId) {
                <a class="similar-card" [routerLink]="['/movies', rec.movieId]">
                  <div class="similar-title">{{ rec.title }}</div>
                  <div class="similar-year">{{ rec.year }}</div>
                </a>
              }
            </div>
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .page { max-width: 1100px; margin: 0 auto; padding: 2rem; }
    .loader { color: #aaa; text-align: center; padding: 3rem; }
    .movie-hero { display: flex; gap: 2.5rem; margin-bottom: 3rem; }
    .hero-poster { width: 260px; flex-shrink: 0; }
    .hero-poster img { width: 100%; border-radius: 8px; }
    .poster-placeholder {
      width: 100%; aspect-ratio: 2/3; background: #1a1a1a; border-radius: 8px;
      display: flex; align-items: center; justify-content: center; font-size: 4rem;
    }
    .hero-details { flex: 1; }
    h1 { color: #fff; font-size: 2rem; margin: 0 0 0.8rem; }
    .meta-row { display: flex; gap: 1rem; align-items: center; margin-bottom: 1rem; }
    .year { color: #888; }
    .avg-rating { color: #f5c518; font-size: 1.1rem; font-weight: 600; }
    .vote-count { color: #888; font-size: 0.85rem; }
    .genres { display: flex; flex-wrap: wrap; gap: 0.4rem; margin-bottom: 1rem; }
    .genre-tag {
      background: #e50914; color: #fff; padding: 0.3rem 0.7rem;
      border-radius: 4px; font-size: 0.8rem; font-weight: 500;
    }
    .crew { color: #aaa; margin: 0.4rem 0; font-size: 0.9rem; }
    .crew strong { color: #fff; }
    .synopsis { color: #ccc; line-height: 1.6; margin-top: 1rem; }
    .section { margin-bottom: 2.5rem; }
    h2 { color: #fff; font-size: 1.3rem; margin-bottom: 1rem; border-bottom: 1px solid #2a2a2a; padding-bottom: 0.5rem; }
    .rating-form { background: #1a1a1a; padding: 1.2rem; border-radius: 8px; }
    .field-row { display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap; }
    .field { display: flex; flex-direction: column; gap: 0.3rem; }
    .field.flex-grow { flex: 1; }
    label { color: #aaa; font-size: 0.8rem; }
    input[type=number] { width: 80px; padding: 0.5rem; background: #0a0a0a; border: 1px solid #333; border-radius: 4px; color: #fff; }
    input[type=text] { width: 100%; padding: 0.5rem; background: #0a0a0a; border: 1px solid #333; border-radius: 4px; color: #fff; }
    .btn-rate {
      padding: 0.55rem 1.2rem; background: #e50914; border: none; color: #fff;
      border-radius: 4px; cursor: pointer; font-weight: 600;
    }
    .btn-rate:disabled { opacity: 0.5; }
    .success-msg { color: #2ecc71; font-size: 0.85rem; margin-top: 0.5rem; }
    .error-msg { color: #e50914; font-size: 0.85rem; margin-top: 0.5rem; }
    .ratings-list { display: flex; flex-direction: column; gap: 0.8rem; }
    .rating-item {
      display: flex; gap: 1rem; align-items: flex-start;
      background: #1a1a1a; padding: 1rem; border-radius: 6px;
    }
    .rating-score {
      font-size: 1.2rem; font-weight: 700; color: #f5c518;
      min-width: 60px; text-align: center;
    }
    .rating-body p { color: #ccc; margin: 0 0 0.3rem; }
    .rating-date { color: #666; font-size: 0.8rem; }
    .similar-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; }
    .similar-card {
      background: #1a1a1a; border: 1px solid #2a2a2a; border-radius: 6px;
      padding: 1rem; text-decoration: none; transition: border-color 0.2s;
    }
    .similar-card:hover { border-color: #e50914; }
    .similar-title { color: #fff; font-size: 0.9rem; margin-bottom: 0.3rem; }
    .similar-year { color: #888; font-size: 0.8rem; }
    .watchlist-actions { display: flex; gap: 0.8rem; margin-top: 1.2rem; flex-wrap: wrap; }
    .btn-watch, .btn-save {
      padding: 0.6rem 1.2rem; border-radius: 4px; cursor: pointer;
      font-size: 0.9rem; font-weight: 600; transition: all 0.2s;
    }
    .btn-watch { background: #1a1a1a; border: 1px solid #555; color: #ccc; }
    .btn-watch.active { background: #1a3a1a; border-color: #4caf50; color: #4caf50; }
    .btn-watch:hover { border-color: #4caf50; color: #4caf50; }
    .btn-save { background: #1a1a1a; border: 1px solid #555; color: #ccc; }
    .btn-save.active { background: #1a2a3a; border-color: #2196f3; color: #2196f3; }
    .btn-save:hover { border-color: #2196f3; color: #2196f3; }
    @media (max-width: 700px) {
      .movie-hero { flex-direction: column; }
      .hero-poster { width: 100%; max-width: 300px; }
      .similar-grid { grid-template-columns: repeat(2, 1fr); }
    }
  `]
})
export class MovieDetailComponent implements OnInit {
  movie = signal<Movie | null>(null);
  ratings = signal<Rating[]>([]);
  similar = signal<Recommendation[]>([]);
  loading = signal(true);
  ratingLoading = signal(false);
  ratingSuccess = signal(false);
  ratingError = signal('');
  isWatched = signal(false);
  isSaved = signal(false);
  ratingForm: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private movieService: MovieService,
    private ratingService: RatingService,
    private recService: RecommendationService,
    public auth: AuthService,
    private watchlistService: WatchlistService,
    private fb: FormBuilder
  ) {
    this.ratingForm = this.fb.group({
      score: ['', [Validators.required, Validators.min(1), Validators.max(10)]],
      comment: ['']
    });
  }

  directorNames(): string {
    return (this.movie()?.directors || []).join(', ');
  }

  actorNames(): string {
    return (this.movie()?.actors || []).slice(0, 4).join(', ');
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.movieService.getById(id).subscribe({
      next: (m) => {
        this.movie.set(m);
        this.loading.set(false);
        this.loadRatings(id);
        this.loadSimilar(id);
        if (this.auth.isLoggedIn()) {
          this.loadWatchlistStatus(id);
        }
      },
      error: () => this.loading.set(false)
    });
  }

  loadWatchlistStatus(id: string): void {
    this.watchlistService.getWatched().subscribe({
      next: (movies) => this.isWatched.set(movies.some(m => m.movieId === id)),
      error: () => {}
    });
    this.watchlistService.getSaved().subscribe({
      next: (movies) => this.isSaved.set(movies.some(m => m.movieId === id)),
      error: () => {}
    });
  }

  toggleWatched(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    if (this.isWatched()) {
      this.watchlistService.removeWatched(id).subscribe(() => this.isWatched.set(false));
    } else {
      this.watchlistService.markWatched(id).subscribe(() => this.isWatched.set(true));
    }
  }

  toggleSaved(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    if (this.isSaved()) {
      this.watchlistService.unsaveMovie(id).subscribe(() => this.isSaved.set(false));
    } else {
      this.watchlistService.saveMovie(id).subscribe(() => this.isSaved.set(true));
    }
  }

  loadRatings(id: string): void {
    this.ratingService.getMovieRatings(id).subscribe({
      next: (r) => this.ratings.set(r),
      error: () => {}
    });
  }

  loadSimilar(id: string): void {
    this.recService.getSimilar(id).subscribe({
      next: (r) => this.similar.set(r),
      error: () => {}
    });
  }

  submitRating(): void {
    if (this.ratingForm.invalid) return;
    this.ratingLoading.set(true);
    this.ratingError.set('');
    const id = this.route.snapshot.paramMap.get('id')!;
    this.ratingService.rateMovie({ movieId: id, ...this.ratingForm.value }).subscribe({
      next: () => {
        this.ratingSuccess.set(true);
        this.ratingLoading.set(false);
        this.ratingForm.reset();
        this.loadRatings(id);
        setTimeout(() => this.ratingSuccess.set(false), 3000);
      },
      error: (err) => {
        this.ratingError.set(err.error?.message || 'Erreur lors de la notation');
        this.ratingLoading.set(false);
      }
    });
  }
}
