import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TranslationService } from '../../services/translation.service';

interface RequirementDto {
  id: number;
  type: 'TEXT' | 'DOCUMENT';
  titleDe: string;
  titleEn: string;
  descriptionDe: string | null;
  descriptionEn: string | null;
}

interface FaqDto {
  id: number;
  questionDe: string;
  questionEn: string;
  answerDe: string;
  answerEn: string;
}

interface ServiceDetailsDto {
  nameDe: string;
  nameEn: string;
  descriptionDe: string;
  descriptionEn: string;
  costDe: string;
  costEn: string;
  processingTimeDe: string;
  processingTimeEn: string;
  requirements: RequirementDto[];
  faqs: FaqDto[];
}

@Component({
  selector: 'app-service-page',
  imports: [CommonModule],
  template: `
    <div class="service-page-container" *ngIf="serviceDetails()">
      <!-- Back navigation link -->
      <div class="back-link-wrapper">
        <button class="btn-back" (click)="goBack()">
          <svg xmlns="http://www.w3.org/2000/svg" class="back-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          <span>{{ translate.t().back_to_overview }}</span>
        </button>
      </div>

      <!-- Service Title / Info Hero -->
      <section class="service-hero">
        <div class="hero-left">
          <h1 class="service-title">
            {{ translate.currentLang() === 'de' ? serviceDetails()?.nameDe : serviceDetails()?.nameEn }}
          </h1>
          <p class="service-description">
            {{ translate.currentLang() === 'de' ? serviceDetails()?.descriptionDe : serviceDetails()?.descriptionEn }}
          </p>
          <button class="btn-apply-cta" (click)="onApplyClick()">
            <span>{{ translate.t().apply_online }}</span>
            <svg xmlns="http://www.w3.org/2000/svg" class="cta-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
        <div class="hero-right-illustration"></div>
      </section>

      <!-- Processing Time Box (Requested to be right at the top of Wichtige Informationen) -->
      <section class="section-container time-container">
        <div class="section-header-row">
          <h2 class="section-title">{{ translate.t().important_info }}</h2>
        </div>

        <div class="time-box-card">
          <div class="time-box-left">
            <div class="time-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="time-text-content">
              <h4 class="time-title">{{ translate.t().processing_time }}</h4>
              <p class="time-desc">Wie lange dauert die Bearbeitung?</p>
            </div>
          </div>
          <div class="time-box-right">
            <span class="online-badge">ONLINE</span>
            <span class="time-value">
              {{ translate.currentLang() === 'de' ? serviceDetails()?.processingTimeDe : serviceDetails()?.processingTimeEn }}
            </span>
          </div>
        </div>
      </section>

      <!-- Main requirements details grid -->
      <section class="section-container info-grid-section">
        <div class="info-grid">
          <!-- Left Part: TEXT Requirements ("Voraussetzungen") -->
          <div class="requirements-list-card">
            <div class="card-header-icon-row">
              <div class="req-header-icon">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m4 8H3a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z" />
                </svg>
              </div>
              <h3 class="card-title">{{ translate.t().requirements }}</h3>
            </div>
            
            <div class="requirements-items">
              <div *ngFor="let req of textRequirements()" class="req-item">
                <h4 class="req-item-title">
                  <svg xmlns="http://www.w3.org/2000/svg" class="check-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                  </svg>
                  {{ translate.currentLang() === 'de' ? req.titleDe : req.titleEn }}
                </h4>
                <p class="req-item-desc">
                  {{ translate.currentLang() === 'de' ? req.descriptionDe : req.descriptionEn }}
                </p>
              </div>
            </div>
          </div>

          <!-- Right Part: DOCUMENTS & COST -->
          <div class="docs-cost-sidebar">
            <!-- Documents Card (Black background) -->
            <div class="documents-card">
              <div class="docs-header">
                <svg xmlns="http://www.w3.org/2000/svg" class="docs-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <h3>{{ translate.t().documents_required }}</h3>
              </div>
              <ul class="docs-list">
                <li *ngFor="let doc of documentRequirements()">
                  {{ translate.currentLang() === 'de' ? doc.titleDe : doc.titleEn }}
                </li>
              </ul>
            </div>

            <!-- Cost Card -->
            <div class="cost-card">
              <div class="cost-icon-wrapper">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                </svg>
              </div>
              <h4 class="cost-label">{{ translate.t().cost }}</h4>
              <span class="cost-amount">
                {{ translate.currentLang() === 'de' ? serviceDetails()?.costDe : serviceDetails()?.costEn }}
              </span>
              <p class="cost-subtext" *ngIf="serviceDetails()?.costDe === 'Gebührenfrei'">
                Die Anmeldung des Wohnsitzes ist grundsätzlich kostenlos.
              </p>
            </div>
          </div>
        </div>
      </section>

      <!-- FAQ Section -->
      <section class="section-container faq-section">
        <div class="faq-header">
          <h2 class="faq-main-title">{{ translate.t().faq_title }}</h2>
          <p class="faq-sub-title">{{ translate.t().faq_desc }}</p>
        </div>

        <div class="faq-accordion-list">
          <div *ngFor="let faq of serviceDetails()?.faqs; let i = index" class="faq-item" [class.open]="openFaqIndex() === i">
            <button class="faq-question-btn" (click)="toggleFaq(i)">
              <span>{{ translate.currentLang() === 'de' ? faq.questionDe : faq.questionEn }}</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="faq-chevron" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            <div class="faq-answer-panel">
              <p class="faq-answer-content">
                {{ translate.currentLang() === 'de' ? faq.answerDe : faq.answerEn }}
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .service-page-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 32px 24px 80px 24px;
      width: 100%;
    }
    .back-link-wrapper {
      margin-bottom: 24px;
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
    
    /* Hero section */
    .service-hero {
      display: grid;
      grid-template-columns: 1.2fr 0.8fr;
      gap: 48px;
      align-items: center;
      margin-bottom: 48px;
    }
    .service-title {
      font-size: 3.2rem;
      font-weight: 800;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 20px;
      letter-spacing: -0.03em;
      line-height: 1.15;
    }
    .service-description {
      font-size: 1.2rem;
      color: #515154;
      line-height: 1.55;
      margin-bottom: 32px;
    }
    .btn-apply-cta {
      background: #000000;
      color: #ffffff;
      border: none;
      padding: 16px 36px;
      font-size: 1.05rem;
      font-weight: 700;
      border-radius: 8px;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 12px;
      transition: background 0.2s ease, transform 0.1s ease;
    }
    .btn-apply-cta:hover {
      background: #2a2a2c;
    }
    .btn-apply-cta:active {
      transform: scale(0.98);
    }
    .cta-arrow {
      width: 18px;
      height: 18px;
      transition: transform 0.2s ease;
    }
    .btn-apply-cta:hover .cta-arrow {
      transform: translateX(4px);
    }
    .hero-right-illustration {
      border-radius: 16px;
      height: 280px;
      width: 100%;
    }

    /* Section container styling */
    .section-container {
      margin-bottom: 48px;
    }
    .section-title {
      font-size: 2rem;
      font-weight: 700;
      color: #1d1d1f;
      letter-spacing: -0.02em;
    }
    .section-header-row {
      border-bottom: 1px solid #eaeaea;
      padding-bottom: 16px;
      margin-bottom: 24px;
    }

    /* Bearbeitungszeit Card */
    .time-container {
      margin-bottom: 36px;
    }
    .time-box-card {
      background: #f5f5f7;
      border: 1px solid #e5e5e7;
      border-radius: 12px;
      padding: 24px 32px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 20px;
    }
    .time-box-left {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .time-icon-wrapper {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: #ffffff;
      color: #515154;
      display: flex;
      justify-content: center;
      align-items: center;
      box-shadow: 0 2px 8px rgba(0,0,0,0.03);
    }
    .time-icon-wrapper svg {
      width: 24px;
      height: 24px;
    }
    .time-text-content {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .time-title {
      font-size: 1.1rem;
      font-weight: 700;
      color: #1d1d1f;
      margin: 0;
    }
    .time-desc {
      font-size: 0.85rem;
      color: #86868b;
      margin: 0;
    }
    .time-box-right {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 4px;
    }
    .online-badge {
      background: #e8f5e9;
      color: #2e7d32;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 2px 8px;
      border-radius: 4px;
      letter-spacing: 0.05em;
    }
    .time-value {
      font-size: 1.5rem;
      font-weight: 800;
      color: #1d1d1f;
      letter-spacing: -0.02em;
    }

    /* Split layout */
    .info-grid {
      display: grid;
      grid-template-columns: 1.2fr 0.8fr;
      gap: 32px;
    }
    
    /* TEXT requirements prerequisites */
    .requirements-list-card {
      background: #f5f5f7;
      border: 1px solid #e5e5e7;
      border-radius: 12px;
      padding: 32px;
    }
    .card-header-icon-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 24px;
    }
    .req-header-icon {
      color: #1d1d1f;
    }
    .req-header-icon svg {
      width: 24px;
      height: 24px;
    }
    .card-title {
      font-size: 1.35rem;
      font-weight: 700;
      color: #1d1d1f;
      margin: 0;
    }
    .requirements-items {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }
    .req-item-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 1.05rem;
      font-weight: 700;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 8px;
    }
    .check-icon {
      width: 18px;
      height: 18px;
      color: #34c759;
    }
    .req-item-desc {
      font-size: 0.95rem;
      color: #515154;
      line-height: 1.45;
      margin: 0;
      padding-left: 28px;
    }

    /* Documents Sidebar */
    .docs-cost-sidebar {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }
    .documents-card {
      background: #000000;
      color: #ffffff;
      border-radius: 12px;
      padding: 32px;
    }
    .docs-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;
    }
    .docs-header h3 {
      font-size: 1.25rem;
      font-weight: 700;
      margin: 0;
    }
    .docs-icon {
      width: 22px;
      height: 22px;
      color: #ffffff;
    }
    .docs-list {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-direction: column;
      gap: 14px;
    }
    .docs-list li {
      font-size: 0.9rem;
      line-height: 1.4;
      position: relative;
      padding-left: 20px;
      font-weight: 500;
    }
    .docs-list li::before {
      content: '■';
      position: absolute;
      left: 0;
      top: 0;
      font-size: 0.7rem;
      color: #86868b;
    }
    
    /* Cost Card */
    .cost-card {
      background: #ffffff;
      border: 1px solid #e5e5e7;
      border-radius: 12px;
      padding: 32px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .cost-icon-wrapper {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      background: #f5f5f7;
      color: #1d1d1f;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 12px;
    }
    .cost-icon-wrapper svg {
      width: 20px;
      height: 20px;
    }
    .cost-label {
      font-size: 0.9rem;
      font-weight: 600;
      color: #86868b;
      margin: 0 0 6px 0;
      text-transform: uppercase;
    }
    .cost-amount {
      font-size: 2.2rem;
      font-weight: 800;
      color: #1d1d1f;
      margin-bottom: 8px;
      letter-spacing: -0.02em;
    }
    .cost-subtext {
      font-size: 0.8rem;
      color: #86868b;
      margin: 0;
    }

    /* FAQ Section */
    .faq-section {
      border-top: 1px solid #eaeaea;
      padding-top: 64px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .faq-header {
      text-align: center;
      max-width: 600px;
      margin-bottom: 40px;
    }
    .faq-main-title {
      font-size: 2.2rem;
      font-weight: 800;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 12px;
      letter-spacing: -0.03em;
    }
    .faq-sub-title {
      font-size: 1.05rem;
      color: #86868b;
      line-height: 1.45;
      margin: 0;
    }
    .faq-accordion-list {
      width: 100%;
      max-width: 800px;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .faq-item {
      background: #f5f5f7;
      border: 1px solid #e5e5e7;
      border-radius: 12px;
      overflow: hidden;
      transition: all 0.2s ease;
    }
    .faq-item.open {
      border-color: #d2d2d7;
      background: #ffffff;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.02);
    }
    .faq-question-btn {
      width: 100%;
      padding: 24px 28px;
      background: none;
      border: none;
      outline: none;
      text-align: left;
      font-size: 1.05rem;
      font-weight: 700;
      color: #1d1d1f;
      cursor: pointer;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 20px;
    }
    .faq-chevron {
      width: 16px;
      height: 16px;
      color: #86868b;
      transition: transform 0.25s ease;
    }
    .faq-item.open .faq-chevron {
      transform: rotate(180deg);
      color: #1d1d1f;
    }
    .faq-answer-panel {
      max-height: 0;
      overflow: hidden;
      transition: max-height 0.25s cubic-bezier(0.16, 1, 0.3, 1);
    }
    .faq-item.open .faq-answer-panel {
      max-height: 200px; /* arbitrary limit for smooth animation */
    }
    .faq-answer-content {
      padding: 0 28px 24px 28px;
      margin: 0;
      font-size: 0.95rem;
      color: #515154;
      line-height: 1.5;
    }

    @media (max-width: 900px) {
      .service-hero {
        grid-template-columns: 1fr;
        gap: 32px;
      }
      .hero-right-illustration {
        display: none;
      }
      .info-grid {
        grid-template-columns: 1fr;
      }
      .service-title {
        font-size: 2.5rem;
      }
    }
  `]
})
export class ServiceDetailsPage implements OnInit {
  protected slug = '';
  protected readonly serviceDetails = signal<ServiceDetailsDto | null>(null);
  protected readonly textRequirements = signal<RequirementDto[]>([]);
  protected readonly documentRequirements = signal<RequirementDto[]>([]);
  
  protected readonly openFaqIndex = signal<number | null>(null);

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
        this.fetchServiceDetails();
      }
    });
  }

  fetchServiceDetails() {
    this.http.get<ServiceDetailsDto>(`${this.apiBaseUrl}/api/v1/catalog/services/${this.slug}`).subscribe({
      next: (data) => {
        this.serviceDetails.set(data);
        
        // Filter requirements by type
        const textReqs = data.requirements.filter(r => r.type === 'TEXT');
        const docReqs = data.requirements.filter(r => r.type === 'DOCUMENT');
        this.textRequirements.set(textReqs);
        this.documentRequirements.set(docReqs);
      },
      error: () => console.error('Failed to load service details')
    });
  }

  goBack() {
    window.history.back();
  }

  toggleFaq(index: number) {
    if (this.openFaqIndex() === index) {
      this.openFaqIndex.set(null);
    } else {
      this.openFaqIndex.set(index);
    }
  }

  onApplyClick() {
    this.router.navigate([`/services/${this.slug}/apply`]);
  }
}
