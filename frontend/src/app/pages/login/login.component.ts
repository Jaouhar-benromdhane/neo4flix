import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-wrapper">
      <div class="auth-card">
        <h1>🎬 Neo4flix</h1>
        <h2>Connexion</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="field">
            <label>Email</label>
            <input type="email" formControlName="email" placeholder="alice@example.com" />
          </div>
          <div class="field">
            <label>Mot de passe</label>
            <input type="password" formControlName="password" placeholder="••••••••" />
          </div>
          @if (error()) {
            <div class="error-msg">{{ error() }}</div>
          }
          <button type="submit" [disabled]="loading() || form.invalid" class="btn-submit">
            {{ loading() ? 'Connexion...' : 'Se connecter' }}
          </button>
        </form>
        <p class="switch-link">Pas encore de compte ? <a routerLink="/register">S'inscrire</a></p>
      </div>
    </div>
  `,
  styles: [`
    .auth-wrapper {
      min-height: calc(100vh - 64px);
      display: flex;
      align-items: center;
      justify-content: center;
      background: #0a0a0a;
    }
    .auth-card {
      background: #1a1a1a;
      border: 1px solid #2a2a2a;
      border-radius: 8px;
      padding: 2.5rem;
      width: 400px;
    }
    h1 { color: #e50914; font-size: 1.8rem; margin: 0 0 0.5rem; }
    h2 { color: #fff; margin: 0 0 2rem; font-size: 1.3rem; }
    .field { margin-bottom: 1.2rem; }
    label { display: block; color: #aaa; font-size: 0.85rem; margin-bottom: 0.4rem; }
    input {
      width: 100%; padding: 0.7rem; background: #0a0a0a;
      border: 1px solid #333; border-radius: 4px; color: #fff; font-size: 1rem;
      box-sizing: border-box;
    }
    input:focus { outline: none; border-color: #e50914; }
    .btn-submit {
      width: 100%; padding: 0.8rem; background: #e50914; border: none;
      color: #fff; font-size: 1rem; border-radius: 4px; cursor: pointer;
      font-weight: 600; margin-top: 0.5rem;
    }
    .btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
    .error-msg { color: #e50914; font-size: 0.85rem; margin-bottom: 0.8rem; }
    .switch-link { color: #aaa; text-align: center; margin-top: 1.5rem; font-size: 0.9rem; }
    .switch-link a { color: #e50914; text-decoration: none; }
  `]
})
export class LoginComponent {
  form: FormGroup;
  loading = signal(false);
  error = signal('');

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set('');
    this.auth.login(this.form.value).subscribe({
      next: () => this.router.navigate(['/movies']),
      error: (err) => {
        this.error.set(err.error?.message || 'Email ou mot de passe incorrect');
        this.loading.set(false);
      }
    });
  }
}
