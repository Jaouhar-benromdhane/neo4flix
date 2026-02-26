import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <div class="navbar-brand">
        <a routerLink="/">🎬 Neo4flix</a>
      </div>
      <div class="navbar-links">
        <a routerLink="/movies" routerLinkActive="active">Films</a>
        <a routerLink="/recommendations" routerLinkActive="active">Recommandations</a>
        @if (auth.isLoggedIn()) {
          <a routerLink="/my-ratings" routerLinkActive="active">Mes notes</a>
          <span class="user-info">{{ auth.currentUser()?.username }}</span>
          @if (auth.isAdmin()) {
            <span class="badge-admin">ADMIN</span>
          }
          <button class="btn-logout" (click)="auth.logout()">Déconnexion</button>
        } @else {
          <a routerLink="/login" routerLinkActive="active">Connexion</a>
          <a routerLink="/register" routerLinkActive="active" class="btn-register">S'inscrire</a>
        }
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 2rem;
      height: 64px;
      background: #141414;
      border-bottom: 1px solid #2a2a2a;
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .navbar-brand a {
      font-size: 1.5rem;
      font-weight: 700;
      color: #e50914;
      text-decoration: none;
    }
    .navbar-links {
      display: flex;
      align-items: center;
      gap: 1.5rem;
    }
    .navbar-links a {
      color: #ccc;
      text-decoration: none;
      font-size: 0.95rem;
      transition: color 0.2s;
    }
    .navbar-links a:hover, .navbar-links a.active {
      color: #fff;
    }
    .btn-register {
      background: #e50914;
      color: #fff !important;
      padding: 0.4rem 1rem;
      border-radius: 4px;
    }
    .btn-logout {
      background: transparent;
      border: 1px solid #555;
      color: #ccc;
      cursor: pointer;
      padding: 0.4rem 0.8rem;
      border-radius: 4px;
      font-size: 0.9rem;
      transition: all 0.2s;
    }
    .btn-logout:hover { border-color: #e50914; color: #e50914; }
    .user-info { color: #fff; font-size: 0.9rem; }
    .badge-admin {
      background: #e50914;
      color: #fff;
      padding: 0.2rem 0.5rem;
      border-radius: 3px;
      font-size: 0.7rem;
      font-weight: 700;
    }
  `]
})
export class NavbarComponent {
  constructor(public auth: AuthService) {}
}
