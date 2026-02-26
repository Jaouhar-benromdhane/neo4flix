import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-oauth2-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="min-height:100vh;display:flex;align-items:center;justify-content:center;background:#0a0a0a;color:#fff;font-size:1.2rem;">
      @if (error()) {
        <div style="color:#e50914">❌ Erreur lors de la connexion Google. <a style="color:#e50914" href="/login">Retour</a></div>
      } @else {
        <div>⏳ Connexion Google en cours...</div>
      }
    </div>
  `
})
export class OAuth2CallbackComponent implements OnInit {
  error = () => this._error;
  private _error = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParams;
    const token    = params['token'];
    const userId   = params['userId'];
    const username = params['username'];
    const email    = params['email'];
    const role     = params['role'];

    if (!token) {
      this._error = true;
      return;
    }

    // Stocker la session comme si c'était un login normal
    (this.auth as any).saveSessionFromOAuth2({ token, userId, username, email, role });
    this.router.navigate(['/movies']);
  }
}
