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

        <!-- STEP 1 : email + password -->
        @if (!requires2FA()) {
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

          <!-- Google OAuth2 -->
          <div class="divider"><span>ou</span></div>
          <a class="btn-google" href="http://localhost:8082/oauth2/authorization/google">
            <svg width="18" height="18" viewBox="0 0 48 48" style="margin-right:10px"><path fill="#EA4335" d="M24 9.5c3.54 0 6.72 1.22 9.22 3.22l6.86-6.86C35.64 2.38 30.12 0 24 0 14.62 0 6.52 5.48 2.46 13.44l7.98 6.2C12.38 13.12 17.72 9.5 24 9.5z"/><path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.74 6c4.52-4.18 7.08-10.36 7.08-17.65z"/><path fill="#FBBC05" d="M10.44 28.36A14.56 14.56 0 0 1 9.5 24c0-1.52.26-2.98.72-4.36l-7.98-6.2A23.93 23.93 0 0 0 0 24c0 3.84.92 7.46 2.56 10.68l7.88-6.32z"/><path fill="#34A853" d="M24 48c6.48 0 11.92-2.14 15.9-5.82l-7.74-6c-2.16 1.46-4.96 2.32-8.16 2.32-6.28 0-11.62-3.62-13.56-8.78l-7.88 6.32C6.5 42.52 14.62 48 24 48z"/></svg>
            Se connecter avec Google
          </a>
          <p class="switch-link">Pas encore de compte ? <a routerLink="/register">S'inscrire</a></p>
        }

        <!-- STEP 2 : 2FA code -->
        @if (requires2FA()) {
          <div class="twofa-step">
            <p class="twofa-hint">🔐 Code 2FA requis. Ouvrez votre application Authenticator.</p>
            <form [formGroup]="tfaForm" (ngSubmit)="submit2FA()">
              <div class="field">
                <label>Code à 6 chiffres</label>
                <input type="text" formControlName="code" placeholder="123456" maxlength="6"
                       class="code-input" autocomplete="one-time-code" />
              </div>
              @if (error()) {
                <div class="error-msg">{{ error() }}</div>
              }
              <button type="submit" [disabled]="loading() || tfaForm.invalid" class="btn-submit">
                {{ loading() ? 'Vérification...' : 'Valider' }}
              </button>
            </form>
            <button class="btn-back" (click)="requires2FA.set(false)">← Retour</button>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .auth-wrapper {
      min-height: calc(100vh - 64px); display: flex;
      align-items: center; justify-content: center; background: #0a0a0a;
    }
    .auth-card {
      background: #1a1a1a; border: 1px solid #2a2a2a;
      border-radius: 8px; padding: 2.5rem; width: 400px;
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
    .divider {
      display: flex; align-items: center; gap: 0.8rem;
      color: #555; margin: 1.2rem 0; font-size: 0.85rem;
    }
    .divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: #333; }
    .btn-google {
      display: flex; align-items: center; justify-content: center;
      width: 100%; padding: 0.75rem; background: #fff; border: none;
      border-radius: 4px; cursor: pointer; font-size: 0.95rem; font-weight: 600;
      color: #333; text-decoration: none; box-sizing: border-box; transition: background 0.2s;
    }
    .btn-google:hover { background: #f0f0f0; }
    .twofa-step { text-align: center; }
    .twofa-hint { color: #aaa; font-size: 0.9rem; margin-bottom: 1.5rem; line-height: 1.5; }
    .code-input { letter-spacing: 0.3em; text-align: center; font-size: 1.3rem !important; }
    .btn-back {
      background: transparent; border: none; color: #888;
      cursor: pointer; margin-top: 1rem; font-size: 0.9rem;
    }
    .btn-back:hover { color: #fff; }
  `]
})
export class LoginComponent {
  form: FormGroup;
  tfaForm: FormGroup;
  loading = signal(false);
  error = signal('');
  requires2FA = signal(false);
  private pendingCredentials: { email: string; password: string } | null = null;

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
    this.tfaForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set('');
    this.pendingCredentials = this.form.value;
    this.auth.login(this.form.value).subscribe({
      next: (res: any) => {
        if (res?.requires2FA) {
          this.requires2FA.set(true);
          this.loading.set(false);
        } else {
          this.router.navigate(['/movies']);
        }
      },
      error: (err: any) => {
        this.error.set(err.error?.message || 'Email ou mot de passe incorrect');
        this.loading.set(false);
      }
    });
  }

  submit2FA(): void {
    if (this.tfaForm.invalid || !this.pendingCredentials) return;
    this.loading.set(true);
    this.error.set('');
    this.auth.login({ ...this.pendingCredentials, twoFactorCode: this.tfaForm.value.code }).subscribe({
      next: () => this.router.navigate(['/movies']),
      error: (err: any) => {
        this.error.set(err.error?.message || 'Code invalide');
        this.loading.set(false);
      }
    });
  }
}

