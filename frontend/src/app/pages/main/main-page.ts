import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { TranslationService } from '../../services/translation.service';

interface CategoryDto {
  id: number;
  nameDe: string;
  nameEn: string;
  link: string;
  icon: string;
}

interface ServiceDto {
  id: number;
  nameDe: string;
  nameEn: string;
  link: string;
}

@Component({
  selector: 'app-main-page',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="main-page-container">
      <!-- Hero Section -->
      <section class="hero-section">
        <div class="hero-overlay"></div>
        <div class="hero-content">
          <h1 class="hero-title">{{ translate.t().hero_title }}</h1>
          
          <!-- Search Bar -->
          <div class="search-wrapper">
            <div class="search-bar">
              <svg xmlns="http://www.w3.org/2000/svg" class="search-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input 
                type="text" 
                [placeholder]="translate.t().search_placeholder" 
                [(ngModel)]="searchQuery" 
                (input)="onSearchInput()"
                (focus)="showSuggestions.set(true)"
                (blur)="onSearchBlur()"
              />
              <button class="btn-search" (click)="onSearchSubmit()">
                {{ translate.t().search_button }}
              </button>
            </div>

            <!-- Autocomplete suggestions -->
            <div class="search-suggestions" *ngIf="showSuggestions() && searchResults().length > 0">
              <div 
                *ngFor="let service of searchResults()" 
                class="suggestion-item"
                (mousedown)="selectService(service)"
              >
                {{ translate.currentLang() === 'de' ? service.nameDe : service.nameEn }}
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Tabs Navigation (Bürger / Unternehmen) -->
      <section class="tab-nav-section">
        <div class="tab-nav-wrapper">
          <button 
            [class.active]="activeTab() === 'bürger'" 
            (click)="activeTab.set('bürger')"
          >
            {{ translate.t().citizens_tab }}
          </button>
          <button 
            [class.active]="activeTab() === 'unternehmen'" 
            (click)="activeTab.set('unternehmen')"
          >
            {{ translate.t().businesses_tab }}
          </button>
        </div>
      </section>

      <!-- Citizen Content -->
      <ng-container *ngIf="activeTab() === 'bürger'">
        <!-- Top Services Grid -->
        <section class="section-container top-services-section">
          <h2 class="section-title">{{ translate.t().top_services }}</h2>
          <div class="services-grid">
            <div 
              *ngFor="let service of topServices()" 
              class="service-card"
              (click)="goToService(service.link)"
            >
              <span class="service-name">
                {{ translate.currentLang() === 'de' ? service.nameDe : service.nameEn }}
              </span>
              <svg xmlns="http://www.w3.org/2000/svg" class="arrow-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </div>
        </section>

        <!-- Categories Grid -->
        <section class="section-container categories-section">
          <h2 class="section-title">{{ translate.t().categories_title }}</h2>
          <div class="categories-grid">
            <div 
              *ngFor="let category of categories()" 
              class="category-card"
              (click)="goToCategory(category.link)"
            >
              <div class="category-icon-wrapper">
                <span class="material-icons">{{ getMaterialIcon(category.icon) }}</span>
              </div>
              <span class="category-name">
                {{ translate.currentLang() === 'de' ? category.nameDe : category.nameEn }}
              </span>
            </div>
          </div>
        </section>
      </ng-container>

      <!-- Business Content / Under Construction -->
      <ng-container *ngIf="activeTab() === 'unternehmen'">
        <section class="section-container under-construction-section">
          <div class="under-construction-card">
            <span class="material-icons construction-icon">construction</span>
            <h3>{{ translate.t().under_construction }}</h3>
          </div>
        </section>
      </ng-container>
    </div>
  `,
  styles: [`
    .main-page-container {
      display: flex;
      flex-direction: column;
      width: 100%;
    }
    .hero-section {
      position: relative;
      background: url(/site_wallpaper.png) no-repeat center center;
      padding: 100px 24px;
      display: flex;
      justify-content: center;
      align-items: center;
      color: #ffffff;
      text-align: center;
      min-height: 380px;
    }
    .hero-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: url('/site_wallpaper.png') no-repeat center center;
      background-size: cover;
      opacity: 0.15;
      mix-blend-mode: overlay;
      pointer-events: none;
    }
    .hero-content {
      position: relative;
      z-index: 2;
      width: 100%;
      max-width: 680px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .hero-title {
      font-size: 2.5rem;
      font-weight: 800;
      margin-bottom: 32px;
      letter-spacing: -0.03em;
      line-height: 1.2;
    }
    .search-wrapper {
      position: relative;
      width: 100%;
    }
    .search-bar {
      display: flex;
      align-items: center;
      background: #ffffff;
      border-radius: 99px;
      padding: 6px 6px 6px 24px;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
      width: 100%;
      border: 1px solid rgba(255, 255, 255, 0.2);
    }
    .search-icon {
      width: 22px;
      height: 22px;
      color: #86868b;
      margin-right: 12px;
      flex-shrink: 0;
    }
    .search-bar input {
      border: none;
      background: none;
      outline: none;
      font-size: 1.1rem;
      color: #1d1d1f;
      width: 100%;
      padding: 8px 0;
    }
    .search-bar input::placeholder {
      color: #86868b;
    }
    .btn-search {
      background: #000000;
      color: #ffffff;
      border: none;
      padding: 12px 28px;
      font-size: 0.95rem;
      font-weight: 600;
      border-radius: 99px;
      cursor: pointer;
      transition: background 0.2s ease, transform 0.15s ease;
      flex-shrink: 0;
    }
    .btn-search:hover {
      background: #2a2a2c;
    }
    .btn-search:active {
      transform: scale(0.97);
    }
    .search-suggestions {
      position: absolute;
      top: calc(100% + 8px);
      left: 0;
      right: 0;
      background: #ffffff;
      border-radius: 16px;
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
      border: 1px solid #eaeaea;
      z-index: 10;
      overflow: hidden;
      max-height: 250px;
      overflow-y: auto;
      text-align: left;
    }
    .suggestion-item {
      padding: 14px 24px;
      font-size: 0.95rem;
      color: #1d1d1f;
      font-weight: 500;
      cursor: pointer;
      transition: background 0.2s ease;
      border-bottom: 1px solid #f5f5f7;
    }
    .suggestion-item:last-child {
      border-bottom: none;
    }
    .suggestion-item:hover {
      background: #f5f5f7;
    }
    
    /* Tabs Navigation */
    .tab-nav-section {
      background: #ffffff;
      border-bottom: 1px solid #eaeaea;
      display: flex;
      justify-content: center;
    }
    .tab-nav-wrapper {
      display: flex;
      gap: 32px;
    }
    .tab-nav-wrapper button {
      background: none;
      border: none;
      font-size: 1.1rem;
      font-weight: 600;
      color: #86868b;
      padding: 20px 8px;
      cursor: pointer;
      position: relative;
      transition: color 0.2s ease;
    }
    .tab-nav-wrapper button.active {
      color: #1d1d1f;
    }
    .tab-nav-wrapper button.active::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: #000000;
      border-radius: 3px 3px 0 0;
    }

    /* Grids & Cards */
    .section-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 48px 24px;
      width: 100%;
    }
    .section-title {
      font-size: 1.75rem;
      font-weight: 700;
      color: #1d1d1f;
      margin-bottom: 28px;
      letter-spacing: -0.02em;
    }
    
    /* Services Grid */
    .services-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 16px;
    }
    .service-card {
      background: #f5f5f7;
      border: 1px solid #e5e5e7;
      border-radius: 8px;
      padding: 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      cursor: pointer;
      transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    }
    .service-card:hover {
      background: #ffffff;
      border-color: #d2d2d7;
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05);
      transform: translateY(-2px);
    }
    .service-name {
      font-size: 1rem;
      font-weight: 600;
      color: #1d1d1f;
    }
    .arrow-icon {
      width: 18px;
      height: 18px;
      color: #86868b;
      transition: transform 0.2s ease, color 0.2s ease;
    }
    .service-card:hover .arrow-icon {
      color: #1d1d1f;
      transform: translateX(3px);
    }

    /* Categories Grid */
    .categories-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;
    }
    .category-card {
      background: #f5f5f7;
      border: 1px solid #e5e5e7;
      border-radius: 8px;
      padding: 24px;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      cursor: pointer;
      transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
      gap: 14px;
    }
    .category-card:hover {
      background: #ffffff;
      border-color: #d2d2d7;
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05);
      transform: translateY(-2px);
    }
    .category-icon-wrapper {
      width: 48px;
      height: 48px;
      display: flex;
      justify-content: center;
      align-items: center;
      background: #ffffff;
      border-radius: 12px;
      color: #102144;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
      transition: transform 0.2s ease;
    }
    .category-card:hover .category-icon-wrapper {
      transform: scale(1.08);
      background: #f5f5f7;
    }
    .category-icon-wrapper .material-icons {
      font-size: 24px;
    }
    .category-name {
      font-size: 0.9rem;
      font-weight: 600;
      color: #1d1d1f;
      line-height: 1.3;
    }

    /* Under Construction */
    .under-construction-section {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 60px 24px;
      width: 100%;
    }
    .under-construction-card {
      background: #f5f5f7;
      border: 1px solid #e5e5e7;
      border-radius: 12px;
      padding: 48px 24px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      text-align: center;
      max-width: 400px;
      width: 100%;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
    }
    .construction-icon {
      font-size: 48px;
      color: #86868b;
    }
    .under-construction-card h3 {
      font-size: 1.25rem;
      font-weight: 600;
      color: #1d1d1f;
      margin: 0;
    }

    @media (max-width: 768px) {
      .hero-title {
        font-size: 1.8rem;
      }
      .services-grid {
        grid-template-columns: 1fr;
      }
      .categories-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }
  `]
})
export class MainPage implements OnInit {
  protected readonly activeTab = signal<'bürger' | 'unternehmen'>('bürger');
  protected readonly topServices = signal<ServiceDto[]>([]);
  protected readonly categories = signal<CategoryDto[]>([]);
  
  protected searchQuery = '';
  protected readonly searchResults = signal<ServiceDto[]>([]);
  protected readonly showSuggestions = signal(false);

  private readonly apiBaseUrl = 'http://localhost:8081';

  constructor(
    public translate: TranslationService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit() {
    this.fetchTopServices();
    this.fetchCategories();
  }

  getMaterialIcon(iconName: string): string {
    const mapping: Record<string, string> = {
      'home_work': 'home_work',
      'family_restroom': 'family_restroom',
      'car': 'directions_car',
      'apartment': 'apartment',
      'briefcase': 'work',
      'health_and_safety': 'health_and_safety',
      'graduation-cap': 'school',
      'shield_radar': 'shield',
      'trees': 'forest',
      'globe': 'public'
    };
    return mapping[iconName] || 'help_outline';
  }

  fetchTopServices() {
    this.http.get<ServiceDto[]>(`${this.apiBaseUrl}/api/v1/catalog/top`).subscribe({
      next: (data) => this.topServices.set(data),
      error: () => console.error('Failed to load top services')
    });
  }

  fetchCategories() {
    this.http.get<CategoryDto[]>(`${this.apiBaseUrl}/api/v1/catalog`).subscribe({
      next: (data) => this.categories.set(data),
      error: () => console.error('Failed to load categories')
    });
  }

  onSearchInput() {
    if (this.searchQuery.trim().length < 2) {
      this.searchResults.set([]);
      return;
    }
    this.http.get<ServiceDto[]>(`${this.apiBaseUrl}/api/v1/catalog/search`, {
      params: { query: this.searchQuery }
    }).subscribe({
      next: (data) => this.searchResults.set(data),
      error: () => this.searchResults.set([])
    });
  }

  onSearchBlur() {
    // delay hiding to let clicks execute
    setTimeout(() => {
      this.showSuggestions.set(false);
    }, 200);
  }

  onSearchSubmit() {
    if (this.searchQuery.trim()) {
      // For MVP: Search redirects to search list page or loads matching catalog
      console.log('Searching for:', this.searchQuery);
    }
  }

  selectService(service: ServiceDto) {
    this.searchQuery = this.translate.currentLang() === 'de' ? service.nameDe : service.nameEn;
    this.showSuggestions.set(false);
    this.goToService(service.link);
  }

  goToService(link: string) {
    this.router.navigate([link]);
  }

  goToCategory(link: string) {
    this.router.navigate([link]);
  }
}
