import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header/header';
import { FooterComponent } from './components/footer/footer';
import { EudiModalComponent } from './components/eudi-modal/eudi-modal';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, HeaderComponent, FooterComponent, EudiModalComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  constructor(public authService: AuthService) {}

  onLoginRequest() {
    this.authService.loginModalOpen.set(true);
  }

  onModalClose() {
    this.authService.loginModalOpen.set(false);
  }

  onLoginSuccess(claims: any) {
    this.authService.login(claims);
    this.authService.loginModalOpen.set(false);
  }
}
