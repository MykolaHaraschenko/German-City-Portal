import { Component, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TranslationService } from '../../services/translation.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-header',
  imports: [CommonModule],
  template: `
    <header class="header-container">
      <div class="header-left" (click)="goHome()">
        <!-- Saxony Crest SVG -->
        <svg class="crest-svg" viewBox="0 0 100 120" xmlns="http://www.w3.org/2000/svg">
          <!-- Shield background stripes: black and gold -->
          <g>
            <rect x="10" y="10" width="80" height="10" fill="#000000"/>
            <rect x="10" y="20" width="80" height="10" fill="#FFCC00"/>
            <rect x="10" y="30" width="80" height="10" fill="#000000"/>
            <rect x="10" y="40" width="80" height="10" fill="#FFCC00"/>
            <rect x="10" y="50" width="80" height="10" fill="#000000"/>
            <rect x="10" y="60" width="80" height="10" fill="#FFCC00"/>
            <rect x="10" y="70" width="80" height="10" fill="#000000"/>
            <rect x="10" y="80" width="80" height="10" fill="#FFCC00"/>
            <rect x="10" y="90" width="80" height="10" fill="#000000"/>
            <rect x="10" y="100" width="80" height="10" fill="#FFCC00"/>
          </g>
          <!-- Shield outline & mask for bottom curve -->
          <path d="M 10 10 L 90 10 L 90 70 A 40 40 0 0 1 50 110 A 40 40 0 0 1 10 70 Z" fill="none" stroke="#222" stroke-width="4"/>
          <!-- Diagonal green crancelin (Rue crown) -->
          <path d="M 8 102 L 92 18 C 92 18, 70 8, 50 25 C 30 42, 8 102, 8 102" fill="none" stroke="#008000" stroke-width="8" stroke-linecap="round" opacity="0.95"/>
          <!-- Crown leaves along the path -->
          <path d="M 28 80 L 32 72 M 48 58 L 52 50 M 68 36 L 72 28" stroke="#008000" stroke-width="6" stroke-linecap="round"/>
        </svg>
        <span class="portal-title">{{ translate.t().portal_title }}</span>
      </div>
      
      <div class="header-right">
        <!-- Language Selector -->
        <div class="lang-selector">
          <button [class.active]="translate.currentLang() === 'de'" (click)="setLanguage('de')">DE</button>
          <span class="lang-divider">/</span>
          <button [class.active]="translate.currentLang() === 'en'" (click)="setLanguage('en')">EN</button>
        </div>

        <!-- Auth Button / Dropdown -->
        <ng-container *ngIf="!authService.isLoggedIn(); else userMenu">
          <button class="btn-eudi-login" (click)="onLoginClick()">
            {{ translate.t().login_eudi }}
          </button>
        </ng-container>
        
        <ng-template #userMenu>
          <div class="user-menu-container">
            <button class="user-profile-trigger" (click)="toggleDropdown()">
              <svg xmlns="http://www.w3.org/2000/svg" class="user-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span class="user-name-label">{{ authService.currentUser()?.fullName }}</span>
            </button>
            
            <div class="dropdown-menu" *ngIf="dropdownOpen()">
              <button class="dropdown-item" (click)="goToProfile()">
                {{ translate.t().profile }}
              </button>
              <hr class="dropdown-divider">
              <button class="dropdown-item logout-btn" (click)="logout()">
                {{ translate.t().logout }}
              </button>
            </div>
          </div>
        </ng-template>
      </div>
    </header>
  `,
  styles: [`
    .header-container {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 48px;
      background: #ffffff;
      border-bottom: 1px solid #eaeaea;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
    }
    .crest-svg {
      width: 28px;
      height: 34px;
    }
    .portal-title {
      font-size: 1.15rem;
      font-weight: 700;
      color: #1d1d1f;
      letter-spacing: -0.02em;
    }
    .header-right {
      display: flex;
      align-items: center;
      gap: 24px;
    }
    .lang-selector {
      display: flex;
      align-items: center;
      gap: 4px;
      background: #f5f5f7;
      padding: 4px 10px;
      border-radius: 6px;
      border: 1px solid #e5e5e7;
    }
    .lang-selector button {
      background: none;
      border: none;
      font-size: 0.8rem;
      font-weight: 500;
      color: #86868b;
      cursor: pointer;
      padding: 2px 6px;
      border-radius: 4px;
      transition: all 0.2s ease;
    }
    .lang-selector button.active {
      color: #1d1d1f;
      font-weight: 700;
      background: #ffffff;
      box-shadow: 0 1px 2px rgba(0,0,0,0.1);
    }
    .lang-divider {
      color: #d2d2d7;
      font-size: 0.8rem;
    }
    .btn-eudi-login {
      background: #000000;
      color: #ffffff;
      border: none;
      padding: 10px 20px;
      font-size: 0.85rem;
      font-weight: 600;
      border-radius: 6px;
      cursor: pointer;
      transition: background 0.2s ease, transform 0.1s ease;
    }
    .btn-eudi-login:hover {
      background: #2a2a2c;
    }
    .btn-eudi-login:active {
      transform: scale(0.98);
    }
    .user-menu-container {
      position: relative;
    }
    .user-profile-trigger {
      display: flex;
      align-items: center;
      gap: 8px;
      background: #f5f5f7;
      border: 1px solid #e5e5e7;
      padding: 8px 14px;
      border-radius: 6px;
      cursor: pointer;
      color: #1d1d1f;
      font-weight: 600;
      font-size: 0.85rem;
      transition: background 0.2s ease;
    }
    .user-profile-trigger:hover {
      background: #e8e8ed;
    }
    .user-icon {
      width: 18px;
      height: 18px;
      color: #515154;
    }
    .dropdown-menu {
      position: absolute;
      right: 0;
      top: calc(100% + 8px);
      background: #ffffff;
      border: 1px solid #e5e5e7;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      min-width: 150px;
      padding: 6px 0;
      display: flex;
      flex-direction: column;
      animation: fadeIn 0.15s ease-out;
    }
    .dropdown-item {
      background: none;
      border: none;
      padding: 10px 16px;
      font-size: 0.85rem;
      text-align: left;
      cursor: pointer;
      color: #1d1d1f;
      font-weight: 500;
      transition: background 0.2s ease;
    }
    .dropdown-item:hover {
      background: #f5f5f7;
    }
    .dropdown-divider {
      border: 0;
      border-top: 1px solid #eaeaea;
      margin: 4px 0;
    }
    .logout-btn {
      color: #e30613;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(-5px); }
      to { opacity: 1; transform: translateY(0); }
    }
    @media (max-width: 768px) {
      .header-container {
        padding: 16px 20px;
      }
      .portal-title {
        font-size: 1rem;
      }
    }
  `]
})
export class HeaderComponent {
  @Output() loginRequest = new EventEmitter<void>();

  protected readonly dropdownOpen = signal(false);

  constructor(
    public translate: TranslationService,
    public authService: AuthService,
    private router: Router
  ) {}

  goHome() {
    this.router.navigate(['/']);
  }

  setLanguage(lang: 'de' | 'en') {
    this.translate.setLang(lang);
  }

  onLoginClick() {
    this.loginRequest.emit();
  }

  toggleDropdown() {
    this.dropdownOpen.update(v => !v);
  }

  goToProfile() {
    this.dropdownOpen.set(false);
    this.router.navigate(['/profile']);
  }

  logout() {
    this.dropdownOpen.set(false);
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
