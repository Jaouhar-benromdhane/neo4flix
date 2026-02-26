import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { TwoFactorService } from '../../services/twofactor.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page">
      <div class="profile-container">
        <h1 class="page-title">Mon Profil</h1>

        <!-- User info -->
        <div class="card">
          <h2>Informations</h2>
          <div class="info-row"><span class="label">Nom d'utilisateur</span><span class="value">{{ auth.currentUser()?.username }}</span></div>
          <div class="info-row"><span class="label">Email</span><span class="value">{{ auth.currentUser()?.email }}</span></div>
          <div class="info-row"><span class="label">Rôle</span><span class="value badge">{{ auth.currentUser()?.role }}</span></div>
        </div>

        <!-- 2FA section -->
        <div class="card">
          <h2>🔐 Double authentification (2FA TOTP)</h2>
          <div class="status-row">
            <span>Statut :</span>
            @if (twoFAEnabled()) {
              <span class="badge-on">✅ Activée</span>
            } @else {
              <span class="badge-off">❌ Désactivée</span>
            }
          </div>

          @if (!twoFAEnabled()) {
            <!-- Step 1: setup -->
            @if (!qrCode()) {
              <button class="btn-primary" [disabled]="twoFALoading()" (click)="setup2FA()">
                {{ twoFALoading() ? 'Génération...' : 'Configurer la 2FA' }}
              </button>
            }

            <!-- Step 2: show QR + enter code to enable -->
            @if (qrCode()) {
              <div class="qr-section">
                <p class="instructions">
                  1. Scannez le QR code avec <strong>Google Authenticator</strong> ou <strong>Authy</strong><br/>
                  2. Entrez le code à 6 chiffres affiché dans l'application
                </p>
                <img [src]="'data:image/png;base64,' + qrCode()" alt="QR Code 2FA" class="qr-img" />
                <p class="secret-hint">Clé manuelle : <code>{{ secret() }}</code></p>
                <form [formGroup]="codeForm" (ngSubmit)="enable2FA()" class="code-form">
                  <input type="text" formControlName="code" placeholder="Code à 6 chiffres" maxlength="6"
                         class="code-input" autocomplete="one-time-code" />
                  <button type="submit" class="btn-primary" [disabled]="codeForm.invalid || twoFALoading()">
                    {{ twoFALoading() ? '...' : 'Activer la 2FA' }}
                  </button>
                </form>
              </div>
            }
          } @else {
            <!-- Disable 2FA -->
            <p class="instructions">Entrez un code de votre application pour désactiver la 2FA.</p>
            <form [formGroup]="codeForm" (ngSubmit)="disable2FA()" class="code-form">
              <input type="text" formControlName="code" placeholder="Code à 6 chiffres" maxlength="6"
                     class="code-input" autocomplete="one-time-code" />
              <button type="submit" class="btn-danger" [disabled]="codeForm.invalid || twoFALoading()">
                {{ twoFALoading() ? '...' : 'Désactiver la 2FA' }}
              </button>
            </form>
          }

          @if (twoFASuccess()) {
            <div class="success-msg">{{ twoFASuccess() }}</div>
          }
          @if (twoFAError()) {
            <div class="error-msg">{{ twoFAError() }}</div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page { min-height: calc(100vh - 64px); background: #0a0a0a; padding: 2rem; }
    .profile-container { max-width: 640px; margin: 0 auto; }
    .page-title { color: #fff; font-size: 2rem; margin-bottom: 2rem; }
    .card {
      background: #1a1a1a; border: 1px solid #2a2a2a; border-radius: 8px;
      padding: 1.8rem; margin-bottom: 1.5rem;
    }
    h2 { color: #fff; font-size: 1.2rem; margin: 0 0 1.2rem; }
    .info-row { display: flex; align-items: center; gap: 1rem; margin-bottom: 0.8rem; }
    .label { color: #888; font-size: 0.9rem; width: 160px; flex-shrink: 0; }
    .value { color: #fff; font-size: 0.95rem; }
    .badge { background: #333; padding: 0.15rem 0.5rem; border-radius: 4px; font-size: 0.8rem; }
    .status-row { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.2rem; color: #aaa; }
    .badge-on { background: #1a3a1a; color: #4caf50; padding: 0.2rem 0.7rem; border-radius: 12px; font-size: 0.85rem; }
    .badge-off { background: #3a1a1a; color: #e57373; padding: 0.2rem 0.7rem; border-radius: 12px; font-size: 0.85rem; }
    .btn-primary {
      background: #e50914; border: none; color: #fff; padding: 0.7rem 1.5rem;
      border-radius: 4px; cursor: pointer; font-size: 0.95rem; font-weight: 600;
    }
    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-danger {
      background: #333; border: 1px solid #e50914; color: #e50914; padding: 0.7rem 1.5rem;
      border-radius: 4px; cursor: pointer; font-size: 0.95rem; font-weight: 600;
    }
    .btn-danger:disabled { opacity: 0.5; cursor: not-allowed; }
    .qr-section { margin-top: 1rem; }
    .instructions { color: #aaa; font-size: 0.9rem; line-height: 1.6; margin-bottom: 1rem; }
    .qr-img { display: block; width: 200px; height: 200px; border: 4px solid #fff; border-radius: 4px; margin-bottom: 0.8rem; }
    .secret-hint { color: #666; font-size: 0.8rem; margin-bottom: 1rem; }
    code { background: #333; padding: 0.1rem 0.4rem; border-radius: 3px; color: #ccc; font-size: 0.8rem; }
    .code-form { display: flex; gap: 0.8rem; align-items: center; flex-wrap: wrap; }
    .code-input {
      padding: 0.7rem 1rem; background: #0a0a0a; border: 1px solid #444;
      border-radius: 4px; color: #fff; font-size: 1.2rem; letter-spacing: 0.3em;
      width: 160px; text-align: center;
    }
    .code-input:focus { outline: none; border-color: #e50914; }
    .success-msg { color: #4caf50; font-size: 0.9rem; margin-top: 1rem; }
    .error-msg { color: #e50914; font-size: 0.9rem; margin-top: 1rem; }
  `]
})
export class ProfileComponent implements OnInit {
  twoFAEnabled = signal(false);
  twoFALoading = signal(false);
  twoFASuccess = signal('');
  twoFAError = signal('');
  qrCode = signal('');
  secret = signal('');
  codeForm: FormGroup;

  constructor(
    public auth: AuthService,
    private twoFactorService: TwoFactorService,
    private fb: FormBuilder
  ) {
    this.codeForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });
  }

  ngOnInit(): void {
    this.twoFactorService.getStatus().subscribe({
      next: (s) => this.twoFAEnabled.set(s.twoFactorEnabled),
      error: () => {}
    });
  }

  setup2FA(): void {
    this.twoFALoading.set(true);
    this.twoFAError.set('');
    this.twoFactorService.setup().subscribe({
      next: (res) => {
        this.qrCode.set(res.qrCodeBase64);
        this.secret.set(res.secret);
        this.twoFALoading.set(false);
      },
      error: () => {
        this.twoFAError.set('Erreur lors de la configuration');
        this.twoFALoading.set(false);
      }
    });
  }

  enable2FA(): void {
    if (this.codeForm.invalid) return;
    this.twoFALoading.set(true);
    this.twoFAError.set('');
    this.twoFactorService.enable(this.codeForm.value.code).subscribe({
      next: () => {
        this.twoFAEnabled.set(true);
        this.twoFASuccess.set('✅ 2FA activée avec succès !');
        this.qrCode.set('');
        this.secret.set('');
        this.codeForm.reset();
        this.twoFALoading.set(false);
      },
      error: () => {
        this.twoFAError.set('Code invalide, veuillez réessayer');
        this.twoFALoading.set(false);
      }
    });
  }

  disable2FA(): void {
    if (this.codeForm.invalid) return;
    this.twoFALoading.set(true);
    this.twoFAError.set('');
    this.twoFactorService.disable(this.codeForm.value.code).subscribe({
      next: () => {
        this.twoFAEnabled.set(false);
        this.twoFASuccess.set('2FA désactivée.');
        this.codeForm.reset();
        this.twoFALoading.set(false);
      },
      error: () => {
        this.twoFAError.set('Code invalide, veuillez réessayer');
        this.twoFALoading.set(false);
      }
    });
  }
}
