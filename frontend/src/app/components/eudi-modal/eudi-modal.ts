import { Component, Output, EventEmitter, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import * as QRCode from 'qrcode';
import { TranslationService } from '../../services/translation.service';

@Component({
  selector: 'app-eudi-modal',
  imports: [CommonModule],
  template: `
    <div class="modal-backdrop" (click)="onClose()">
      <div class="modal-card" (click)="$event.stopPropagation()">
        <button class="modal-close-x" (click)="onClose()">&times;</button>
        
        <h3 class="modal-title">{{ translate.t().modal_title }}</h3>
        <p class="modal-desc">{{ translate.t().modal_desc }}</p>

        <div class="qr-container">
          <div *ngIf="loading()" class="qr-state">
            <div class="spinner"></div>
            <p>{{ translate.t().modal_loading }}</p>
          </div>

          <div *ngIf="error()" class="qr-state error">
            <svg xmlns="http://www.w3.org/2000/svg" class="error-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <p>{{ error() }}</p>
          </div>

          <div *ngIf="qrCodeUrl() && !loading() && !error() && !success()" class="qr-code-wrapper">
            <img [src]="qrCodeUrl()" alt="EUDI Wallet Login QR Code" class="qr-image" />
          </div>

          <div *ngIf="success()" class="qr-state success">
            <svg xmlns="http://www.w3.org/2000/svg" class="success-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p>{{ translate.t().modal_success }}</p>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn-close" (click)="onClose()">{{ translate.t().modal_close }}</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modal-backdrop {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.4);
      backdrop-filter: blur(8px);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 1000;
      animation: fadeIn 0.25s ease-out;
    }
    .modal-card {
      background: #ffffff;
      border-radius: 16px;
      width: 90%;
      max-width: 440px;
      padding: 32px;
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
      position: relative;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      animation: scaleUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    }
    .modal-close-x {
      position: absolute;
      top: 16px;
      right: 20px;
      background: none;
      border: none;
      font-size: 1.5rem;
      cursor: pointer;
      color: #86868b;
      transition: color 0.2s ease;
    }
    .modal-close-x:hover {
      color: #1d1d1f;
    }
    .modal-title {
      font-size: 1.35rem;
      font-weight: 700;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 8px;
      letter-spacing: -0.02em;
    }
    .modal-desc {
      font-size: 0.9rem;
      color: #86868b;
      margin-bottom: 24px;
      line-height: 1.4;
    }
    .qr-container {
      width: 250px;
      height: 250px;
      display: flex;
      justify-content: center;
      align-items: center;
      background: #f5f5f7;
      border: 1px solid #eaeaea;
      border-radius: 12px;
      overflow: hidden;
      margin-bottom: 24px;
      position: relative;
    }
    .qr-code-wrapper {
      padding: 10px;
      background: white;
      border-radius: 8px;
    }
    .qr-image {
      width: 220px;
      height: 220px;
      display: block;
    }
    .qr-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 20px;
      color: #515154;
      font-size: 0.85rem;
      font-weight: 500;
    }
    .qr-state.success {
      color: #34c759;
    }
    .qr-state.error {
      color: #ff3b30;
    }
    .success-icon {
      width: 48px;
      height: 48px;
      color: #34c759;
    }
    .error-icon {
      width: 48px;
      height: 48px;
      color: #ff3b30;
    }
    .spinner {
      width: 32px;
      height: 32px;
      border: 3px solid #e5e5e7;
      border-top: 3px solid #0071e3;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    .modal-actions {
      width: 100%;
    }
    .btn-close {
      width: 100%;
      background: #f5f5f7;
      color: #1d1d1f;
      border: 1px solid #e5e5e7;
      padding: 12px;
      border-radius: 8px;
      font-weight: 600;
      font-size: 0.9rem;
      cursor: pointer;
      transition: background 0.2s ease;
    }
    .btn-close:hover {
      background: #e8e8ed;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
    @keyframes scaleUp {
      from { transform: scale(0.95); opacity: 0; }
      to { transform: scale(1); opacity: 1; }
    }
  `]
})
export class EudiModalComponent implements OnInit, OnDestroy {
  @Output() close = new EventEmitter<void>();
  @Output() loginSuccess = new EventEmitter<any>();

  protected readonly qrCodeUrl = signal<string>('');
  protected readonly loading = signal<boolean>(false);
  protected readonly error = signal<string>('');
  protected readonly success = signal<boolean>(false);

  private readonly apiBaseUrl = 'http://localhost:8081';
  private pollTimer: any = null;

  constructor(
    public translate: TranslationService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.startAuthSession();
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  onClose() {
    this.close.emit();
  }

  private startAuthSession() {
    this.loading.set(true);
    this.error.set('');

    const requestBody = {
      businessId: 'citizen-portal-mainpage',
      requestedClaims: ['given_name', 'address', 'date_of_birth']
    };

    this.http.post(`${this.apiBaseUrl}/api/v1/sessions`, requestBody).subscribe({
      next: async (response: any) => {
        this.loading.set(false);
        if (response.qrCodePayload) {
          try {
            const qrDataUrl = await QRCode.toDataURL(response.qrCodePayload, {
              margin: 1,
              width: 220,
              color: {
                dark: '#1d1d1f',
                light: '#ffffff'
              }
            });
            this.qrCodeUrl.set(qrDataUrl);
            this.startPolling(response.transactionId);
          } catch (qrErr) {
            this.error.set(this.translate.t().modal_error);
          }
        }
      },
      error: () => {
        this.loading.set(false);
        this.error.set(this.translate.t().modal_error);
      }
    });
  }

  private startPolling(transactionId: string) {
    this.pollTimer = setInterval(() => {
      this.http.get(`${this.apiBaseUrl}/api/v1/sessions/${transactionId}/status`).subscribe({
        next: (response: any) => {
          if (response.status === 'COMPLETED') {
            this.stopPolling();
            this.success.set(true);
            
            let claims: any = null;
            if (response.claims) {
              try {
                claims = JSON.parse(response.claims);
              } catch (e) {
                claims = { raw: response.claims };
              }
            }
            
            setTimeout(() => {
              this.loginSuccess.emit(claims);
            }, 1200);
          } else if (response.status === 'FAILED' || response.status === 'EXPIRED') {
            this.stopPolling();
            this.error.set(this.translate.t().modal_error);
          }
        },
        error: () => {
          this.stopPolling();
          this.error.set(this.translate.t().modal_error);
        }
      });
    }, 2000);
  }

  private stopPolling() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  }
}
