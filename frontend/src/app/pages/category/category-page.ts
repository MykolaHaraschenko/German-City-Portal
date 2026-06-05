import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TranslationService } from '../../services/translation.service';

interface ServiceDto {
  id: number;
  nameDe: string;
  nameEn: string;
  link: string;
  descriptionDe: string;
  descriptionEn: string;
}

interface CategoryDetailsDto {
  nameDe: string;
  nameEn: string;
  descriptionDe: string;
  descriptionEn: string;
  services: ServiceDto[];
}

@Component({
  selector: 'app-category-page',
  imports: [CommonModule],
  template: `
    <div class="category-page-container">
      <!-- Back Navigation Button -->
      <div class="back-nav-wrapper">
        <button class="btn-back" (click)="goBack()">
          <svg xmlns="http://www.w3.org/2000/svg" class="back-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          <span>{{ translate.t().back_button }}</span>
        </button>
      </div>

      <!-- Category Header Details -->
      <div class="category-details-header" *ngIf="categoryDetails()">
        <h1 class="category-title">
          {{ translate.currentLang() === 'de' ? categoryDetails()?.nameDe : categoryDetails()?.nameEn }}
        </h1>
        <p class="category-description">
          {{ translate.currentLang() === 'de' ? categoryDetails()?.descriptionDe : categoryDetails()?.descriptionEn }}
        </p>
      </div>

      <hr class="divider">

      <!-- Services Grid -->
      <div class="services-list-section" *ngIf="categoryDetails()">
        <h2 class="section-title">{{ translate.t().categories_title }}</h2>
        
        <div class="services-grid">
          <div *ngFor="let service of categoryDetails()?.services" class="service-card">
            <div class="service-card-content">
              <h3 class="service-card-title">
                {{ translate.currentLang() === 'de' ? service.nameDe : service.nameEn }}
              </h3>
              <p class="service-card-desc">
                {{ translate.currentLang() === 'de' ? service.descriptionDe : service.descriptionEn }}
              </p>
            </div>
            
            <button class="btn-apply" (click)="applyService(service.link)">
              <span>{{ translate.t().apply_online }}</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="btn-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .category-page-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 32px 24px 64px 24px;
      width: 100%;
      display: flex;
      flex-direction: column;
    }
    .back-nav-wrapper {
      margin-bottom: 32px;
    }
    .btn-back {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: none;
      border: none;
      font-size: 0.95rem;
      font-weight: 600;
      color: #86868b;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 6px;
      transition: all 0.2s ease;
    }
    .btn-back:hover {
      color: #1d1d1f;
      background: #f5f5f7;
    }
    .back-arrow {
      width: 16px;
      height: 16px;
      transition: transform 0.2s ease;
    }
    .btn-back:hover .back-arrow {
      transform: translateX(-3px);
    }
    .category-details-header {
      max-width: 800px;
      margin-bottom: 40px;
    }
    .category-title {
      font-size: 3rem;
      font-weight: 800;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 16px;
      letter-spacing: -0.03em;
    }
    .category-description {
      font-size: 1.15rem;
      color: #515154;
      line-height: 1.5;
      margin: 0;
    }
    .divider {
      border: 0;
      border-top: 1px solid #eaeaea;
      margin: 0 0 48px 0;
    }
    .services-list-section {
      width: 100%;
    }
    .section-title {
      font-size: 1.75rem;
      font-weight: 700;
      color: #1d1d1f;
      margin-bottom: 32px;
      letter-spacing: -0.02em;
    }
    .services-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
      gap: 24px;
    }
    .service-card {
      background: #ffffff;
      border: 1px solid #e5e5e7;
      border-radius: 12px;
      padding: 32px;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      min-height: 240px;
      transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.01);
    }
    .service-card:hover {
      border-color: #d2d2d7;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
      transform: translateY(-4px);
    }
    .service-card-content {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 24px;
    }
    .service-card-title {
      font-size: 1.25rem;
      font-weight: 700;
      color: #1d1d1f;
      margin: 0;
      letter-spacing: -0.02em;
    }
    .service-card-desc {
      font-size: 0.95rem;
      color: #515154;
      line-height: 1.45;
      margin: 0;
    }
    .btn-apply {
      background: #000000;
      color: #ffffff;
      border: none;
      padding: 12px 20px;
      font-size: 0.9rem;
      font-weight: 600;
      border-radius: 8px;
      cursor: pointer;
      display: flex;
      justify-content: space-between;
      align-items: center;
      transition: background 0.2s ease, transform 0.1s ease;
      width: 100%;
    }
    .btn-apply:hover {
      background: #2a2a2c;
    }
    .btn-apply:active {
      transform: scale(0.98);
    }
    .btn-arrow {
      width: 16px;
      height: 16px;
      transition: transform 0.2s ease;
    }
    .btn-apply:hover .btn-arrow {
      transform: translateX(3px);
    }
    @media (max-width: 768px) {
      .category-title {
        font-size: 2.2rem;
      }
      .services-grid {
        grid-template-columns: 1fr;
      }
      .service-card {
        padding: 24px;
        min-height: auto;
      }
    }
  `]
})
export class ServicesCategoryPage implements OnInit {
  protected slug = '';
  protected readonly categoryDetails = signal<CategoryDetailsDto | null>(null);
  
  private readonly apiBaseUrl = 'http://localhost:8081';

  constructor(
    public translate: TranslationService,
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.slug = params['slug'] || '';
      if (this.slug) {
        this.fetchCategoryDetails();
      }
    });
  }

  fetchCategoryDetails() {
    this.http.get<CategoryDetailsDto>(`${this.apiBaseUrl}/api/v1/catalog/categories/${this.slug}`).subscribe({
      next: (data) => this.categoryDetails.set(data),
      error: () => console.error('Failed to load category services')
    });
  }

  goBack() {
    this.router.navigate(['/']);
  }

  applyService(link: string) {
    this.router.navigate([link]);
  }
}
