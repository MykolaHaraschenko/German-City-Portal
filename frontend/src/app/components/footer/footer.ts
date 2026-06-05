import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslationService } from '../../services/translation.service';

@Component({
  selector: 'app-footer',
  imports: [CommonModule],
  template: `
    <footer class="footer-container">
      <div class="footer-content">
        <div class="footer-left">
          <span class="footer-brand">{{ translate.t().portal_title }}</span>
          <p class="footer-copy">{{ translate.t().copyright }}</p>
        </div>
        <div class="footer-right">
          <ul class="footer-links">
            <li><a href="javascript:void(0)">{{ translate.t().imprint }}</a></li>
            <li><a href="javascript:void(0)">{{ translate.t().privacy }}</a></li>
            <li><a href="javascript:void(0)">{{ translate.t().accessibility }}</a></li>
            <li><a href="javascript:void(0)">{{ translate.t().contact }}</a></li>
          </ul>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    .footer-container {
      background: #f5f5f7;
      padding: 40px 48px;
      border-top: 1px solid #eaeaea;
      margin-top: auto;
    }
    .footer-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      max-width: 1200px;
      margin: 0 auto;
      flex-wrap: wrap;
      gap: 24px;
    }
    .footer-left {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .footer-brand {
      font-size: 0.95rem;
      font-weight: 700;
      color: #1d1d1f;
    }
    .footer-copy {
      font-size: 0.8rem;
      color: #86868b;
      margin: 0;
    }
    .footer-right {
      display: flex;
    }
    .footer-links {
      list-style: none;
      display: flex;
      gap: 24px;
      padding: 0;
      margin: 0;
      flex-wrap: wrap;
    }
    .footer-links a {
      font-size: 0.8rem;
      color: #86868b;
      text-decoration: none;
      transition: color 0.2s ease;
      font-weight: 500;
    }
    .footer-links a:hover {
      color: #1d1d1f;
      text-decoration: underline;
    }
    @media (max-width: 768px) {
      .footer-container {
        padding: 30px 20px;
      }
      .footer-content {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }
      .footer-links {
        gap: 16px;
      }
    }
  `]
})
export class FooterComponent {
  constructor(public translate: TranslationService) {}
}
