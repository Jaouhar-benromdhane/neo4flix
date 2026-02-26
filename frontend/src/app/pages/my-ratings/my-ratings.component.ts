import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RatingService } from '../../services/rating.service';
import { Rating } from '../../models/rating.model';

@Component({
  selector: 'app-my-ratings',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <h1>Mes notes</h1>
      @if (loading()) {
        <div class="loader">Chargement...</div>
      }
      @if (!loading() && ratings().length === 0) {
        <div class="empty-state">
          <p>Vous n'avez encore noté aucun film.</p>
          <a routerLink="/movies" class="btn-browse">Découvrir des films</a>
        </div>
      }
      <div class="ratings-grid">
        @for (rating of ratings(); track rating.ratingId) {
          <div class="rating-card">
            <div class="rating-header">
              <a [routerLink]="['/movies', rating.movieId]" class="movie-title">
                {{ rating.movieTitle }}
              </a>
              <div class="score-badge">{{ rating.score }}/10</div>
            </div>
            @if (rating.comment) {
              <p class="comment">{{ rating.comment }}</p>
            }
            <div class="rating-footer">
              <span class="date">{{ rating.createdAt | date:'dd/MM/yyyy' }}</span>
              <button class="btn-delete" (click)="deleteRating(rating.ratingId)">
                Supprimer
              </button>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .page { max-width: 900px; margin: 0 auto; padding: 2rem; }
    h1 { color: #fff; font-size: 1.8rem; margin-bottom: 2rem; }
    .loader, .empty-state { color: #aaa; text-align: center; padding: 3rem; }
    .btn-browse {
      display: inline-block; margin-top: 1rem; background: #e50914; color: #fff;
      padding: 0.6rem 1.5rem; border-radius: 4px; text-decoration: none; font-weight: 600;
    }
    .ratings-grid { display: flex; flex-direction: column; gap: 1rem; }
    .rating-card {
      background: #1a1a1a; border: 1px solid #2a2a2a; border-radius: 8px; padding: 1.2rem;
    }
    .rating-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.6rem; }
    .movie-title { color: #fff; font-size: 1.1rem; font-weight: 600; text-decoration: none; }
    .movie-title:hover { color: #e50914; }
    .score-badge {
      background: #e50914; color: #fff; padding: 0.3rem 0.8rem;
      border-radius: 20px; font-weight: 700; font-size: 0.9rem;
    }
    .comment { color: #ccc; margin: 0 0 0.8rem; font-style: italic; }
    .rating-footer { display: flex; justify-content: space-between; align-items: center; }
    .date { color: #666; font-size: 0.8rem; }
    .btn-delete {
      background: transparent; border: 1px solid #555; color: #888;
      padding: 0.3rem 0.7rem; border-radius: 4px; cursor: pointer; font-size: 0.8rem;
      transition: all 0.2s;
    }
    .btn-delete:hover { border-color: #e50914; color: #e50914; }
  `]
})
export class MyRatingsComponent implements OnInit {
  ratings = signal<Rating[]>([]);
  loading = signal(true);

  constructor(private ratingService: RatingService) {}

  ngOnInit(): void {
    this.ratingService.getMyRatings().subscribe({
      next: (r) => {
        this.ratings.set(r);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  deleteRating(id: string): void {
    this.ratingService.deleteRating(id).subscribe({
      next: () => this.ratings.update(r => r.filter(x => x.ratingId !== id)),
      error: () => {}
    });
  }
}
