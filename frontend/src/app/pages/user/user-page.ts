import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import * as QRCode from 'qrcode';
import { AuthService } from '../../services/auth.service';
import { TranslationService } from '../../services/translation.service';

interface AttachmentDto {
  id: string;
  fileName: string;
}

interface ApplicationDto {
  id: string;
  serviceNameDe: string;
  serviceNameEn: string;
  status: string;
  submittedAt: string;
  attachments: AttachmentDto[];
}

@Component({
  selector: 'app-user-page',
  imports: [CommonModule],
  template: `
    <div class="profile-page-container">
      <h1 class="page-title">{{ translate.currentLang() === 'de' ? 'Mein Profil & Anträge' : 'My Profile & Applications' }}</h1>
      
      <div *ngIf="authService.isLoggedIn(); else notLoggedIn" class="profile-grid">
        <!-- Left Side: User Profile Information -->
        <div class="profile-card">
          <div class="avatar-section">
            <div class="avatar-circle">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <h3>{{ authService.currentUser()?.fullName }}</h3>
            <span class="verified-badge">EUDI VERIFIED</span>
          </div>

          <div class="profile-info-list">
            <div class="info-item" *ngIf="authService.currentUser()?.address">
              <span class="info-label">Adresse</span>
              <span class="info-value">{{ authService.currentUser()?.address }}</span>
            </div>
            
            <div class="info-item" *ngIf="authService.currentUser()?.birthdate">
              <span class="info-label">Geburtsdatum</span>
              <span class="info-value">{{ authService.currentUser()?.birthdate }}</span>
            </div>

            <div class="info-item" *ngIf="authService.currentUser()?.nationality">
              <span class="info-label">Staatsangehörigkeit</span>
              <span class="info-value">{{ authService.currentUser()?.nationality }}</span>
            </div>
          </div>
        </div>

        <!-- Right Side: Submitted Applications -->
        <div class="applications-card">
          <h2 class="card-title">{{ translate.currentLang() === 'de' ? 'Meine Anträge' : 'My Applications' }}</h2>
          
          <div class="no-applications" *ngIf="applications().length === 0">
            <svg xmlns="http://www.w3.org/2000/svg" class="empty-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            <p>Keine Anträge gefunden.</p>
          </div>

          <div class="table-responsive" *ngIf="applications().length > 0">
            <table class="applications-table">
              <thead>
                <tr>
                  <th>Dienstleistung</th>
                  <th>Eingereicht am</th>
                  <th>Status</th>
                  <th>Dokumente</th>
                  <th>Aktion</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let app of applications()">
                  <td class="app-name">
                    {{ translate.currentLang() === 'de' ? app.serviceNameDe : app.serviceNameEn }}
                  </td>
                  <td class="app-date">
                    {{ app.submittedAt | date:'mediumDate' }}
                  </td>
                  <td>
                    <span class="status-badge" [class]="app.status.toLowerCase()">
                      {{ app.status }}
                    </span>
                  </td>
                  <td class="app-attachments">
                    <div *ngFor="let att of app.attachments" class="attachment-row">
                      <a [href]="getDownloadLink(att.id)" class="download-link">
                        <svg xmlns="http://www.w3.org/2000/svg" class="download-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                          <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                        </svg>
                        <span>{{ att.fileName }}</span>
                      </a>
                    </div>
                  </td>
                  <td>
                    <button *ngIf="app.status === 'APPROVED'" class="btn-save-wallet" (click)="saveToWallet(app.id)">
                      <svg xmlns="http://www.w3.org/2000/svg" class="wallet-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 12V7H5a2 2 0 0 1 0-4h14v4"/>
                        <path d="M3 10h18v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                      </svg>
                      <span>In Wallet speichern</span>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <ng-template #notLoggedIn>
        <div class="login-warning-box">
          <svg xmlns="http://www.w3.org/2000/svg" class="warning-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <h2>Anmeldung erforderlich</h2>
          <p>Bitte melden Sie sich über die Schaltfläche oben rechts mit Ihrer EUDI Wallet an, um Ihr Profil und Ihre Anträge einzusehen.</p>
        </div>
      </ng-template>
    </div>

    <!-- EUDI Wallet Issuance Modal -->
    <div class="modal-backdrop" *ngIf="showWalletModal()" (click)="closeWalletModal()">
      <div class="wallet-modal-card" (click)="$event.stopPropagation()">
        <button class="btn-close-modal" (click)="closeWalletModal()">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
        
        <div class="modal-header">
          <svg class="wallet-modal-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 12V7H5a2 2 0 0 1 0-4h14v4M3 10h18v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          </svg>
          <h3>{{ translate.currentLang() === 'de' ? 'In EUDI Wallet speichern' : 'Save to EUDI Wallet' }}</h3>
        </div>
        
        <div class="modal-body">
          <div *ngIf="walletQrLoading()" class="qr-loading-state">
            <div class="spinner"></div>
            <p>Erstelle Dokument-Angebot...</p>
          </div>
          
          <div *ngIf="!walletQrLoading() && walletQrCodeUrl()" class="qr-display-state">
            <p class="qr-instruction">
              {{ translate.currentLang() === 'de' 
                 ? 'Scannen Sie diesen QR-Code mit Ihrer EUDI Wallet App, um Ihr digitales Dokument zu empfangen:' 
                 : 'Scan this QR code with your EUDI Wallet App to receive your digital credential:' }}
            </p>
            <div class="qr-code-wrapper">
              <img [src]="walletQrCodeUrl()" alt="EUDI Offer QR Code" />
            </div>
            <p class="qr-hint">
              {{ translate.currentLang() === 'de' 
                 ? 'Dieses Dokument wird sicher als eID-konforme Bestätigung in Ihrer Wallet gespeichert.' 
                 : 'This document will be stored securely as an eID-compliant credential in your wallet.' }}
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .profile-page-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 48px 24px 80px 24px;
      width: 100%;
    }
    .page-title {
      font-size: 2.2rem;
      font-weight: 800;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 36px;
      letter-spacing: -0.02em;
    }
    .profile-grid {
      display: grid;
      grid-template-columns: 0.8fr 1.2fr;
      gap: 32px;
      align-items: start;
    }
    
    /* Left card - profile info */
    .profile-card {
      background: #ffffff;
      border: 1px solid #e5e5e7;
      border-radius: 16px;
      padding: 32px;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
    }
    .avatar-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 32px;
    }
    .avatar-circle {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: #f5f5f7;
      color: #515154;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 16px;
    }
    .avatar-circle svg {
      width: 40px;
      height: 40px;
    }
    .avatar-section h3 {
      font-size: 1.3rem;
      font-weight: 700;
      color: #1d1d1f;
      margin: 0 0 8px 0;
      letter-spacing: -0.01em;
    }
    .verified-badge {
      background: #e8f5e9;
      color: #2e7d32;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 4px 10px;
      border-radius: 4px;
      letter-spacing: 0.05em;
    }
    .profile-info-list {
      width: 100%;
      display: flex;
      flex-direction: column;
      gap: 20px;
      text-align: left;
    }
    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
      border-bottom: 1px solid #f5f5f7;
      padding-bottom: 12px;
    }
    .info-item:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }
    .info-label {
      font-size: 0.8rem;
      font-weight: 600;
      color: #86868b;
      text-transform: uppercase;
    }
    .info-value {
      font-size: 1rem;
      font-weight: 600;
      color: #1d1d1f;
    }

    /* Right card - applications */
    .applications-card {
      background: #ffffff;
      border: 1px solid #e5e5e7;
      border-radius: 16px;
      padding: 32px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
      min-height: 400px;
    }
    .card-title {
      font-size: 1.5rem;
      font-weight: 700;
      color: #1d1d1f;
      margin-top: 0;
      margin-bottom: 24px;
      letter-spacing: -0.02em;
    }
    .no-applications {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 250px;
      color: #86868b;
    }
    .empty-icon {
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
      color: #d2d2d7;
    }
    
    /* Applications Table */
    .table-responsive {
      width: 100%;
      overflow-x: auto;
    }
    .applications-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
    }
    .applications-table th {
      font-size: 0.85rem;
      font-weight: 700;
      color: #86868b;
      border-bottom: 1px solid #e5e5e7;
      padding: 12px 16px;
      text-transform: uppercase;
    }
    .applications-table td {
      padding: 18px 16px;
      border-bottom: 1px solid #f5f5f7;
      font-size: 0.95rem;
    }
    .app-name {
      font-weight: 700;
      color: #1d1d1f;
    }
    .app-date {
      color: #515154;
    }
    
    /* Badges */
    .status-badge {
      display: inline-block;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 4px 8px;
      border-radius: 4px;
      text-transform: uppercase;
      letter-spacing: 0.02em;
    }
    .status-badge.pending {
      background: #fff8e1;
      color: #f57f17;
    }
    .status-badge.approved {
      background: #e8f5e9;
      color: #2e7d32;
    }
    .status-badge.rejected {
      background: #ffebee;
      color: #c62828;
    }

    /* Attachments */
    .app-attachments {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .download-link {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 0.85rem;
      color: #0071e3;
      text-decoration: none;
      font-weight: 600;
      transition: color 0.2s ease;
    }
    .download-link:hover {
      color: #002580;
      text-decoration: underline;
    }
    .download-icon {
      width: 14px;
      height: 14px;
    }

    /* Warnings */
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
      color: #ba1a1a;
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
      margin: 0;
    }

    @media (max-width: 900px) {
      .profile-grid {
        grid-template-columns: 1fr;
      }
    }

    /* Save to Wallet Button */
    .btn-save-wallet {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: #0071e3;
      color: #ffffff;
      border: none;
      padding: 6px 12px;
      font-size: 0.85rem;
      font-weight: 600;
      border-radius: 6px;
      cursor: pointer;
      transition: background 0.2s ease, transform 0.1s ease;
    }
    .btn-save-wallet:hover {
      background: #0056b3;
    }
    .btn-save-wallet:active {
      transform: scale(0.97);
    }
    .wallet-btn-icon {
      width: 14px;
      height: 14px;
    }

    /* Modal Backdrop */
    .modal-backdrop {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: rgba(0, 0, 0, 0.4);
      backdrop-filter: blur(10px);
      -webkit-backdrop-filter: blur(10px);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 1000;
    }
    
    /* Modal Card */
    .wallet-modal-card {
      background: #ffffff;
      border: 1px solid #e5e5e7;
      border-radius: 16px;
      padding: 36px;
      width: 100%;
      max-width: 440px;
      box-shadow: 0 12px 30px rgba(0,0,0,0.15);
      position: relative;
      text-align: center;
      animation: modalFadeIn 0.25s cubic-bezier(0.16, 1, 0.3, 1);
    }
    @keyframes modalFadeIn {
      from { opacity: 0; transform: scale(0.95); }
      to { opacity: 1; transform: scale(1); }
    }
    
    .btn-close-modal {
      position: absolute;
      top: 16px;
      right: 16px;
      background: #f5f5f7;
      border: none;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      color: #86868b;
      cursor: pointer;
      display: flex;
      justify-content: center;
      align-items: center;
      transition: all 0.2s ease;
    }
    .btn-close-modal:hover {
      background: #e8e8ed;
      color: #1d1d1f;
    }
    .btn-close-modal svg {
      width: 14px;
      height: 14px;
    }
    
    .modal-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      margin-bottom: 24px;
    }
    .wallet-modal-icon {
      width: 44px;
      height: 44px;
      color: #0071e3;
    }
    .modal-header h3 {
      font-size: 1.35rem;
      font-weight: 800;
      color: #1d1d1f;
      margin: 0;
      letter-spacing: -0.02em;
    }
    
    /* QR Display and loading */
    .qr-instruction {
      font-size: 0.95rem;
      color: #515154;
      line-height: 1.45;
      margin-bottom: 20px;
    }
    .qr-code-wrapper {
      background: #ffffff;
      border: 1px solid #e5e5e7;
      border-radius: 12px;
      padding: 16px;
      display: inline-block;
      margin-bottom: 20px;
    }
    .qr-code-wrapper img {
      display: block;
      width: 200px;
      height: 200px;
    }
    .qr-hint {
      font-size: 0.8rem;
      color: #86868b;
      margin: 0;
    }
    
    /* Loading Spinner */
    .qr-loading-state {
      padding: 40px 0;
    }
    .spinner {
      width: 32px;
      height: 32px;
      border: 3px solid #f5f5f7;
      border-top: 3px solid #0071e3;
      border-radius: 50%;
      margin: 0 auto 16px auto;
      animation: spin 0.8s linear infinite;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class UserPage implements OnInit {
  protected readonly applications = signal<ApplicationDto[]>([]);
  
  protected readonly showWalletModal = signal(false);
  protected readonly walletQrCodeUrl = signal('');
  protected readonly walletQrLoading = signal(false);

  private readonly apiBaseUrl = 'http://localhost:8081';

  constructor(
    public authService: AuthService,
    public translate: TranslationService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.fetchUserApplications();
    }
  }

  fetchUserApplications() {
    const username = this.authService.currentUser()?.fullName || '';
    this.http.get<ApplicationDto[]>(`${this.apiBaseUrl}/api/v1/profile/applications`, {
      params: { username }
    }).subscribe({
      next: (data) => this.applications.set(data),
      error: () => console.error('Failed to load user applications')
    });
  }

  getDownloadLink(attachmentId: string): string {
    return `${this.apiBaseUrl}/api/v1/profile/attachments/${attachmentId}`;
  }

  saveToWallet(appId: string) {
    this.showWalletModal.set(true);
    this.walletQrLoading.set(true);
    this.walletQrCodeUrl.set('');

    this.http.post<any>(`${this.apiBaseUrl}/api/v1/profile/applications/${appId}/issue-offer`, {}).subscribe({
      next: async (response) => {
        this.walletQrLoading.set(false);
        if (response.offerUri) {
          try {
            const qrDataUrl = await QRCode.toDataURL(response.offerUri, {
              margin: 1,
              width: 200,
              color: {
                dark: '#1d1d1f',
                light: '#ffffff'
              }
            });
            this.walletQrCodeUrl.set(qrDataUrl);
          } catch (err) {
            console.error('Failed to generate offer QR', err);
          }
        }
      },
      error: (err) => {
        this.walletQrLoading.set(false);
        console.error('Failed to retrieve credential offer', err);
      }
    });
  }

  closeWalletModal() {
    this.showWalletModal.set(false);
    this.walletQrCodeUrl.set('');
  }
}
