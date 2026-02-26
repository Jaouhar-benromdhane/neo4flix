import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RecommendationService } from '../../services/recommendation.service';
import { AuthService } from '../../services/auth.service';
import { Recommendation } from '../../models/recommendation.model';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <h1>Recommandations</h1>

      @if (auth.isLoggedIn()) {
        <section class="section">
          <h2>Pour vous</h2>
          @if (myLoading()) {
            <div class="loader">Analyse de vos goûts...</div>
          }
          <div class="rec-grid">
            @for (rec of myRecs(); track rec.movieId) {
              <a class="rec-card" [routerLink]="['/movies', rec.movieId]">
                <div class="rec-title">{{ rec.title }}</div>
                <div class="rec-meta">
                  <span class="year">{{ rec.year }}</span>
                  @if (rec.averageRating > 0) {
                    <span class="rating">⭐ {{ rec.averageRating | number:'1.1-1' }}</span>
                  }
                </div>
                @if (rec.reason) {
                  <div class="reason">{{ rec.reason }}</div>
                }
                <div class="relevance">Score: {{ rec.relevanceScore | number:'1.0-1' }}</div>
              </a>
            }
          </div>
          @if (!myLoading() && myRecs().length === 0) {
            <p class="hint">Notez quelques films pour obtenir des recommandations personnalisées.</p>
          }
        </section>
      }

      <section class="section">
        <h2>Films populaires</h2>
        @if (popularLoading()) {
          <div class="loader">Chargement...</div>
        }
        <div class="rec-grid">
          @for (rec of popular(); track rec.movieId) {
            <a class="rec-card" [routerLink]="['/movies', rec.movieId]">
              <div class="rec-title">{{ rec.title }}</div>
              <div class="rec-meta">
                <span class="year">{{ rec.year }}</span>
                @if (rec.averageRating > 0) {
                  <span class="rating">⭐ {{ rec.averageRating | number:'1.1-1' }}</span>
                }
              </div>
              <div class="relevance">Score popularité: {{ rec.relevanceScore | number:'1.0-1' }}</div>
            </a>
          }
        </div>
      </section>
    </div>
  `,
  styles: [`
    .page { max-width: 1100px; margin: 0 auto; padding: 2rem; }
    h1 { color: #fff; font-size: 1.8rem; margin-bottom: 2rem; }
    .section { margin-bottom: 3rem; }
    h2 { color: #fff; font-size: 1.3rem; margin-bottom: 1.2rem; border-bottom: 1px solid #2a2a2a; padding-bottom: 0.5rem; }
    .loader { color: #aaa; padding: 1rem 0; }
    .hint { color: #888; font-style: italic; }
    .rec-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 1rem;
    }
    .rec-card {
      background: #1a1a1a; border: 1px solid #2a2a2a; border-radius: 8px;
      padding: 1.2rem; text-decoration: none; transition: border-color 0.2s, transform 0.2s;
    }
    .rec-card:hover { border-color: #e50914; transform: translateY(-2px); }
    .rec-title { color: #fff; font-size: 1rem; font-weight: 600; margin-bottom: 0.5rem; line-height: 1.3; }
    .rec-meta { display: flex; gap: 0.8rem; margin-bottom: 0.5rem; }
    .year { color: #888; font-size: 0.85rem; }
    .rating { color: #f5c518; font-size: 0.85rem; }
    .reason { color: #aaa; font-size: 0.8rem; margin-bottom: 0.4rem; font-style: italic; }
    .relevance { color: #e50914; font-size: 0.8rem; font-weight: 600; }
  `]
})
export class RecommendationsComponent implements OnInit {
  myRecs = signal<Recommendation[]>([]);
  popular = signal<Recommendation[]>([]);
  myLoading = signal(false);
  popularLoading = signal(true);

  constructor(public auth: AuthService, private recService: RecommendationService) {}

  ngOnInit(): void {
    this.recService.getPopular().subscribe({
      next: (r) => {
        this.popular.set(r);
        this.popularLoading.set(false);
      },
      error: () => this.popularLoading.set(false)
    });

    if (this.auth.isLoggedIn()) {
      this.myLoading.set(true);
      this.recService.getMyRecommendations().subscribe({
        next: (r) => {
          this.myRecs.set(r);
          this.myLoading.set(false);
        },
        error: () => this.myLoading.set(false)
      });
    }
  }
}
