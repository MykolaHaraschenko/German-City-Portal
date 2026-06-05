import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import * as QRCode from 'qrcode';
import { TranslationService } from '../../services/translation.service';
import { AuthService } from '../../services/auth.service';

interface StepDto {
  id: number;
  stepNumber: number;
  type: 'EUDI_SHARE' | 'FORM' | 'FILE_UPLOAD';
  titleDe: string;
  titleEn: string;
  configJson: string;
}

interface ServiceDetailsDto {
  nameDe: string;
  nameEn: string;
  descriptionDe: string;
  descriptionEn: string;
  steps: StepDto[];
}

@Component({
  selector: 'app-service-form',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="form-page-container">
      <div class="back-link-wrapper">
        <button class="btn-back" (click)="goBack()">
          <svg xmlns="http://www.w3.org/2000/svg" class="back-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          <span>{{ translate.t().back_button }}</span>
        </button>
      </div>

      <div class="form-header" *ngIf="serviceDetails()">
        <span class="service-subtitle">Online-Beantragung</span>
        <h1 class="service-title">
          {{ translate.currentLang() === 'de' ? serviceDetails()?.nameDe : serviceDetails()?.nameEn }}
        </h1>
      </div>

      <!-- Success Screen -->
      <div class="success-screen" *ngIf="submittedApplicationId()">
        <div class="success-icon-wrapper">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <h2>{{ translate.t().form_success_title }}</h2>
        <p>{{ translate.t().form_success_desc }}</p>
        <code class="application-id">{{ submittedApplicationId() }}</code>
        <button class="btn-back-home" (click)="goToProfile()">{{ translate.currentLang() === 'de' ? 'Zum Profil' : 'View Profile' }}</button>
      </div>

      <!-- Authorization Required State -->
      <div class="login-required-container" *ngIf="!authService.isLoggedIn()">
        <div class="login-warning-box">
          <svg class="warning-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
          </svg>
          <h2>{{ translate.currentLang() === 'de' ? 'Anmeldung erforderlich' : 'Authentication Required' }}</h2>
          <p>{{ translate.currentLang() === 'de' ? 'Bitte melden Sie sich zuerst mit Ihrer EUDI Wallet an, um dieses Online-Formular auszufüllen.' : 'Please authenticate with your EUDI Wallet first to complete this online form.' }}</p>
          <button class="btn-login-trigger" (click)="authService.loginModalOpen.set(true)">
            {{ translate.currentLang() === 'de' ? 'Mit EUDI Wallet anmelden' : 'Login with EUDI Wallet' }}
          </button>
        </div>
      </div>

      <!-- Dynamic Form Steps Stepper -->
      <div class="stepper-wrapper" *ngIf="authService.isLoggedIn() && serviceDetails() && !submittedApplicationId() && steps().length > 0">
        <!-- Stepper Indicator -->
        <div class="stepper-indicator">
          <div *ngFor="let step of steps(); let idx = index" class="step-dot-wrapper">
            <div 
              class="step-dot" 
              [class.active]="currentStepIdx() === idx" 
              [class.completed]="currentStepIdx() > idx"
            >
              {{ idx + 1 }}
            </div>
            <span class="step-dot-label" *ngIf="currentStepIdx() === idx">
              {{ translate.currentLang() === 'de' ? step.titleDe : step.titleEn }}
            </span>
          </div>
        </div>

        <!-- Form Step Content Card -->
        <div class="step-card">
          <h2 class="step-title">
            {{ translate.t().step_label }} {{ currentStepIdx() + 1 }}: 
            {{ translate.currentLang() === 'de' ? currentStep()?.titleDe : currentStep()?.titleEn }}
          </h2>

          <!-- STEP TYPE: EUDI_SHARE -->
          <div *ngIf="currentStep()?.type === 'EUDI_SHARE'" class="eudi-step-container">
            <div class="eudi-promo-box" *ngIf="!eudiVerified() && !eudiLoading()">
              <p>Möchten Sie Zeit sparen? Identifizieren Sie sich sicher mit Ihrer EUDI Wallet, um alle persönlichen Daten automatisch auszufüllen.</p>
              <button class="btn-prefill-eudi" (click)="startEudiSession()">
                <svg class="eudi-w-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>
                {{ translate.t().prefill_eudi }}
              </button>
            </div>

            <!-- EUDI QR Loading -->
            <div *ngIf="eudiLoading()" class="eudi-qr-state">
              <div class="spinner"></div>
              <p>Generiere QR-Code für EUDI-Freigabe...</p>
            </div>

            <!-- EUDI QR Code Display -->
            <div *ngIf="eudiQrCodeUrl() && !eudiVerified() && !eudiLoading()" class="eudi-qr-wrapper">
              <p class="qr-instruction">Scannen Sie diesen QR-Code mit Ihrer EUDI Wallet App:</p>
              <div class="qr-image-container">
                <img [src]="eudiQrCodeUrl()" alt="EUDI Share QR Code" />
              </div>
              <div class="waiting-indicator">
                <div class="pulse-dot"></div>
                <span>Warten auf Bestätigung...</span>
              </div>
            </div>

            <!-- EUDI Verified success -->
            <div *ngIf="eudiVerified()" class="eudi-success-state">
              <svg xmlns="http://www.w3.org/2000/svg" class="success-check" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <h3>{{ translate.t().prefilled_eudi }}</h3>
              <div class="eudi-claims-preview">
                <p><strong>Name:</strong> {{ eudiClaims()?.full_name }}</p>
                <p><strong>Adresse:</strong> {{ eudiClaims()?.residential_address }}</p>
              </div>
            </div>
          </div>

          <!-- STEP TYPE: FORM -->
          <div *ngIf="currentStep()?.type === 'FORM'" class="form-inputs-container">
            <div *ngFor="let field of currentStepFormFields()" class="input-group">
              <label [for]="field.name">{{ translate.currentLang() === 'de' ? field.labelDe : field.labelEn }}</label>
              <input 
                [id]="field.name" 
                [type]="field.type" 
                [(ngModel)]="formResponses[field.name]"
                [required]="field.required"
                class="form-control"
              />
            </div>
          </div>

          <!-- STEP TYPE: FILE_UPLOAD -->
          <div *ngIf="currentStep()?.type === 'FILE_UPLOAD'" class="files-upload-container">
            <div *ngFor="let doc of currentStepRequiredDocs()" class="file-upload-row">
              <span class="file-label">
                {{ translate.currentLang() === 'de' ? doc.labelDe : doc.labelEn }}
              </span>
              <div class="file-input-wrapper">
                <input 
                  type="file" 
                  [id]="doc.key" 
                  (change)="onFileSelected($event, doc.key)"
                  class="hidden-input"
                />
                <label [for]="doc.key" class="btn-file-select">
                  {{ uploadedFiles[doc.key] ? uploadedFiles[doc.key].name : 'Datei auswählen (PDF/PNG)' }}
                </label>
              </div>
            </div>
          </div>

          <!-- Form Navigation Controls -->
          <div class="form-actions-row">
            <button 
              class="btn-prev" 
              [disabled]="currentStepIdx() === 0" 
              (click)="prevStep()"
            >
              {{ translate.t().prev_button }}
            </button>
            
            <button 
              *ngIf="currentStepIdx() < steps().length - 1" 
              class="btn-next" 
              [disabled]="!isCurrentStepValid()"
              (click)="nextStep()"
            >
              {{ translate.t().next_button }}
            </button>
            
            <button 
              *ngIf="currentStepIdx() === steps().length - 1" 
              class="btn-submit" 
              (click)="submitApplication()"
              [disabled]="submitting() || !isCurrentStepValid()"
            >
              <span *ngIf="submitting()" class="spinner-small"></span>
              {{ translate.t().submit_button }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .form-page-container {
      max-width: 800px;
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
    .form-header {
      margin-bottom: 40px;
    }
    .service-subtitle {
      font-size: 0.85rem;
      color: #0071e3;
      text-transform: uppercase;
      font-weight: 700;
      letter-spacing: 0.05em;
      display: block;
      margin-bottom: 6px;
    }
    .service-title {
      font-size: 2.5rem;
      font-weight: 800;
      color: #1d1d1f;
      margin: 0;
      letter-spacing: -0.02em;
    }

    /* Success Screen */
    .success-screen {
      background: #ffffff;
      border: 1px solid #e5e5e7;
      border-radius: 16px;
      padding: 48px 32px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
    }
    .success-icon-wrapper {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: #e8f5e9;
      color: #34c759;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 24px;
    }
    .success-icon-wrapper svg {
      width: 36px;
      height: 36px;
    }
    .success-screen h2 {
      font-size: 1.8rem;
      font-weight: 700;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 12px;
    }
    .success-screen p {
      color: #86868b;
      font-size: 1rem;
      margin-bottom: 16px;
    }
    .application-id {
      background: #f5f5f7;
      padding: 10px 20px;
      border-radius: 8px;
      font-size: 1.1rem;
      font-weight: 700;
      color: #1d1d1f;
      font-family: monospace;
      margin-bottom: 32px;
      display: inline-block;
      border: 1px solid #eaeaea;
    }
    .btn-back-home {
      background: #000000;
      color: #ffffff;
      border: none;
      padding: 12px 32px;
      font-size: 0.95rem;
      font-weight: 600;
      border-radius: 8px;
      cursor: pointer;
      transition: background 0.2s ease;
    }
    .btn-back-home:hover {
      background: #2a2a2c;
    }

    /* Stepper indicator */
    .stepper-indicator {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 40px;
      background: #f5f5f7;
      padding: 14px 24px;
      border-radius: 12px;
      border: 1px solid #e5e5e7;
    }
    .step-dot-wrapper {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .step-dot {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: #ffffff;
      border: 1px solid #d2d2d7;
      color: #86868b;
      display: flex;
      justify-content: center;
      align-items: center;
      font-weight: 700;
      font-size: 0.85rem;
    }
    .step-dot.active {
      background: #000000;
      border-color: #000000;
      color: #ffffff;
    }
    .step-dot.completed {
      background: #34c759;
      border-color: #34c759;
      color: #ffffff;
    }
    .step-dot-label {
      font-size: 0.85rem;
      font-weight: 700;
      color: #1d1d1f;
    }

    /* Stepper content card */
    .step-card {
      background: #ffffff;
      border: 1px solid #e5e5e7;
      border-radius: 16px;
      padding: 40px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
    }
    .step-title {
      font-size: 1.4rem;
      font-weight: 700;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 32px;
      letter-spacing: -0.02em;
    }

    /* EUDI step styling */
    .eudi-step-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
    }
    .eudi-promo-box {
      max-width: 500px;
      margin-bottom: 12px;
    }
    .eudi-promo-box p {
      color: #515154;
      font-size: 0.95rem;
      line-height: 1.5;
      margin-bottom: 24px;
    }
    .btn-prefill-eudi {
      background: #000000;
      color: #ffffff;
      border: none;
      padding: 12px 24px;
      font-size: 0.95rem;
      font-weight: 600;
      border-radius: 8px;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 10px;
      transition: background 0.2s ease;
    }
    .btn-prefill-eudi:hover {
      background: #2a2a2c;
    }
    .eudi-w-icon {
      width: 18px;
      height: 18px;
    }
    .eudi-qr-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 24px;
      color: #86868b;
    }
    .spinner {
      width: 32px;
      height: 32px;
      border: 3px solid #e5e5e7;
      border-top: 3px solid #000000;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    .eudi-qr-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      background: #f5f5f7;
      padding: 24px;
      border-radius: 12px;
      border: 1px solid #eaeaea;
    }
    .qr-instruction {
      font-size: 0.9rem;
      font-weight: 600;
      color: #515154;
      margin-bottom: 16px;
    }
    .qr-image-container {
      background: #ffffff;
      padding: 12px;
      border-radius: 8px;
      margin-bottom: 16px;
    }
    .qr-image-container img {
      width: 200px;
      height: 200px;
      display: block;
    }
    .waiting-indicator {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.85rem;
      color: #86868b;
      font-weight: 600;
    }
    .pulse-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #0071e3;
      animation: pulse 1.5s infinite;
    }
    .eudi-success-state {
      background: #f5f5f7;
      border: 1px solid #e5e5e7;
      padding: 24px;
      border-radius: 12px;
      max-width: 450px;
      width: 100%;
    }
    .success-check {
      width: 48px;
      height: 48px;
      color: #34c759;
      margin-bottom: 12px;
    }
    .eudi-success-state h3 {
      font-size: 1.15rem;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 16px;
    }
    .eudi-claims-preview {
      text-align: left;
      font-size: 0.9rem;
      display: flex;
      flex-direction: column;
      gap: 8px;
      color: #515154;
    }

    /* Form step styling */
    .form-inputs-container {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
    .input-group {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .input-group label {
      font-size: 0.9rem;
      font-weight: 600;
      color: #515154;
    }
    .form-control {
      border: 1px solid #d2d2d7;
      border-radius: 8px;
      padding: 12px 16px;
      font-size: 0.95rem;
      color: #1d1d1f;
      outline: none;
      transition: border-color 0.2s ease;
    }
    .form-control:focus {
      border-color: #0071e3;
    }

    /* File upload step styling */
    .files-upload-container {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }
    .file-upload-row {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .file-label {
      font-size: 0.9rem;
      font-weight: 600;
      color: #515154;
    }
    .file-input-wrapper {
      position: relative;
    }
    .hidden-input {
      display: none;
    }
    .btn-file-select {
      display: block;
      border: 2px dashed #d2d2d7;
      border-radius: 10px;
      padding: 24px;
      text-align: center;
      font-size: 0.9rem;
      color: #86868b;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.2s ease;
      background: #f5f5f7;
    }
    .btn-file-select:hover {
      background: #ffffff;
      border-color: #000000;
      color: #1d1d1f;
    }

    /* Footer actions row */
    .form-actions-row {
      display: flex;
      justify-content: space-between;
      margin-top: 40px;
      border-top: 1px solid #eaeaea;
      padding-top: 24px;
    }
    .btn-prev {
      background: #f5f5f7;
      color: #1d1d1f;
      border: 1px solid #e5e5e7;
      padding: 10px 24px;
      font-weight: 600;
      border-radius: 8px;
      cursor: pointer;
      font-size: 0.9rem;
      transition: background 0.2s ease;
    }
    .btn-prev:hover:not(:disabled) {
      background: #e8e8ed;
    }
    .btn-prev:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
    .btn-next, .btn-submit {
      background: #000000;
      color: #ffffff;
      border: none;
      padding: 10px 28px;
      font-weight: 600;
      border-radius: 8px;
      cursor: pointer;
      font-size: 0.9rem;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: background 0.2s ease;
    }
    .btn-next:hover, .btn-submit:hover:not(:disabled) {
      background: #2a2a2c;
    }
    .btn-submit:disabled {
      background: #86868b;
      cursor: not-allowed;
    }
    .spinner-small {
      width: 14px;
      height: 14px;
      border: 2px solid #ffffff;
      border-top: 2px solid transparent;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    @keyframes pulse {
      0%, 100% { opacity: 0.4; transform: scale(1); }
      50% { opacity: 1; transform: scale(1.15); }
    }
    
    .login-required-container {
      margin-top: 40px;
    }
    .login-warning-box {
      background: #ffffff;
      border: 1px solid #e5e5e7;
      border-radius: 16px;
      padding: 48px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      max-width: 500px;
      margin: 40px auto;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
    }
    .warning-icon {
      width: 48px;
      height: 48px;
      color: #0071e3;
      margin-bottom: 16px;
    }
    .login-warning-box h2 {
      font-size: 1.5rem;
      font-weight: 700;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 8px;
    }
    .login-warning-box p {
      color: #86868b;
      font-size: 0.95rem;
      line-height: 1.45;
      margin-bottom: 24px;
    }
    .btn-login-trigger {
      background: #000000;
      color: #ffffff;
      border: none;
      padding: 12px 28px;
      font-weight: 600;
      border-radius: 8px;
      cursor: pointer;
      font-size: 0.95rem;
      transition: background 0.2s ease, transform 0.1s ease;
    }
    .btn-login-trigger:hover {
      background: #2a2a2c;
    }
    .btn-login-trigger:active {
      transform: scale(0.98);
    }
  `]
})
export class ServiceForm implements OnInit {
  protected slug = '';
  protected readonly serviceDetails = signal<ServiceDetailsDto | null>(null);
  protected readonly steps = signal<StepDto[]>([]);
  protected readonly currentStepIdx = signal<number>(0);
  protected readonly currentStep = computed<StepDto | null>(() => {
    const list = this.steps();
    const idx = this.currentStepIdx();
    return list.length > 0 ? list[idx] : null;
  });

  // Current parsed config variables
  protected readonly currentStepFormFields = computed<any[]>(() => {
    const step = this.currentStep();
    if (step && step.type === 'FORM') {
      try {
        return JSON.parse(step.configJson);
      } catch (e) {
        return [];
      }
    }
    return [];
  });

  protected readonly currentStepRequiredDocs = computed<any[]>(() => {
    const step = this.currentStep();
    if (step && step.type === 'FILE_UPLOAD') {
      try {
        const config = JSON.parse(step.configJson);
        return config.requiredDocuments || [];
      } catch (e) {
        return [];
      }
    }
    return [];
  });

  // Step responses state
  protected formResponses: { [key: string]: any } = {};
  protected uploadedFiles: { [key: string]: File } = {};

  // EUDI state specifically for steps
  protected readonly eudiLoading = signal(false);
  protected readonly eudiQrCodeUrl = signal('');
  protected readonly eudiVerified = signal(false);
  protected readonly eudiClaims = signal<any>(null);
  private eudiPollTimer: any = null;

  // Submit states
  protected readonly submitting = signal(false);
  protected readonly submittedApplicationId = signal<string>('');

  private readonly apiBaseUrl = 'http://localhost:8081';

  constructor(
    public translate: TranslationService,
    public authService: AuthService,
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
        this.steps.set(data.steps || []);
      },
      error: () => console.error('Failed to load service details for form')
    });
  }

  goBack() {
    window.history.back();
  }

  goToProfile() {
    this.router.navigate(['/profile']);
  }

  prevStep() {
    if (this.currentStepIdx() > 0) {
      this.currentStepIdx.update(i => i - 1);
      this.stopEudiPolling();
    }
  }

  nextStep() {
    if (this.currentStepIdx() < this.steps().length - 1) {
      this.currentStepIdx.update(i => i + 1);
      this.stopEudiPolling();
    }
  }

  // EUDI dynamic verification trigger
  startEudiSession() {
    this.eudiLoading.set(true);
    this.eudiQrCodeUrl.set('');
    this.eudiVerified.set(false);

    const step = this.currentStep();
    let requestedClaims = ['full_name', 'residential_address'];
    let optionalClaims: string[] = [];
    let documents: any[] = [];
    if (step) {
      try {
        const config = JSON.parse(step.configJson);
        if (config.requestedClaims) {
          requestedClaims = config.requestedClaims;
        }
        if (config.optionalClaims) {
          optionalClaims = config.optionalClaims;
        }
        if (config.documents) {
          documents = config.documents;
        }
      } catch (e) {}
    }

    const requestBody = {
      businessId: `form-stepper-${this.slug}`,
      requestedClaims: requestedClaims,
      optionalClaims: optionalClaims,
      documents: documents
    };

    this.http.post(`${this.apiBaseUrl}/api/v1/sessions`, requestBody).subscribe({
      next: async (response: any) => {
        this.eudiLoading.set(false);
        if (response.qrCodePayload) {
          try {
            const qrDataUrl = await QRCode.toDataURL(response.qrCodePayload, {
              margin: 1,
              width: 200,
              color: {
                dark: '#1d1d1f',
                light: '#ffffff'
              }
            });
            this.eudiQrCodeUrl.set(qrDataUrl);
            this.startEudiPolling(response.transactionId);
          } catch (qrErr) {
            console.error('Failed to render EUDI QR');
          }
        }
      },
      error: () => this.eudiLoading.set(false)
    });
  }

  private startEudiPolling(transactionId: string) {
    this.eudiPollTimer = setInterval(() => {
      this.http.get(`${this.apiBaseUrl}/api/v1/sessions/${transactionId}/status`).subscribe({
        next: (response: any) => {
          if (response.status === 'COMPLETED') {
            this.stopEudiPolling();
            this.eudiVerified.set(true);
            
            let claims: any = null;
            if (response.claims) {
              try {
                claims = JSON.parse(response.claims);
              } catch (e) {
                claims = { raw: response.claims };
              }
            }
            this.eudiClaims.set(claims);

            // Dynamically pre-fill any fields in formResponses that match EUDI claims!
            if (claims) {
              this.formResponses['fullName'] = claims.full_name || '';
              this.formResponses['street'] = claims.residential_address || '';
            }
          } else if (response.status === 'FAILED' || response.status === 'EXPIRED') {
            this.stopEudiPolling();
          }
        },
        error: () => this.stopEudiPolling()
      });
    }, 2000);
  }

  private stopEudiPolling() {
    if (this.eudiPollTimer) {
      clearInterval(this.eudiPollTimer);
      this.eudiPollTimer = null;
    }
  }

  // File Upload Handlers
  onFileSelected(event: any, key: string) {
    const fileList: FileList = event.target.files;
    if (fileList.length > 0) {
      this.uploadedFiles[key] = fileList[0];
    }
  }

  // Application Submission via Dynamic Form Engine
  submitApplication() {
    this.submitting.set(true);

    const formData = new FormData();
    
    // Append responses object
    formData.append('responses', JSON.stringify({
      eudiClaims: this.eudiClaims(),
      formInputs: this.formResponses,
      username: this.authService.isLoggedIn() ? (this.authService.currentUser()?.fullName || '') : ''
    }));

    // Append attachments
    Object.keys(this.uploadedFiles).forEach(key => {
      formData.append(key, this.uploadedFiles[key]);
    });

    this.http.post(`${this.apiBaseUrl}/api/v1/services/${this.slug}/apply`, formData).subscribe({
      next: (response: any) => {
        this.submitting.set(false);
        this.submittedApplicationId.set(response.applicationId);
      },
      error: (err) => {
        this.submitting.set(false);
        console.error('Failed to submit application', err);
      }
    });
  }

  isCurrentStepValid(): boolean {
    const step = this.currentStep();
    if (!step) return false;

    if (step.type === 'EUDI_SHARE') {
      return this.eudiVerified();
    }

    if (step.type === 'FORM') {
      const fields = this.currentStepFormFields();
      return fields.every(field => {
        if (!field.required) return true;
        const val = this.formResponses[field.name];
        return val !== undefined && val !== null && val.toString().trim() !== '';
      });
    }

    if (step.type === 'FILE_UPLOAD') {
      const docs = this.currentStepRequiredDocs();
      return docs.every(doc => {
        const file = this.uploadedFiles[doc.key];
        return file !== undefined && file !== null;
      });
    }

    return true;
  }
}
