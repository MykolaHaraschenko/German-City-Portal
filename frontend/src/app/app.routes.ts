import { Routes } from '@angular/router';
import { MainPage } from './pages/main/main-page';
import { ServicesCategoryPage } from './pages/category/category-page';
import { ServiceDetailsPage } from './pages/service/service-page';
import { ServiceForm } from './pages/service-form/service-form';
import { UserPage } from './pages/user/user-page';

export const routes: Routes = [
  { path: '', component: MainPage },
  { path: 'categories/:slug', component: ServicesCategoryPage },
  { path: 'services/:slug', component: ServiceDetailsPage },
  { path: 'services/:slug/apply', component: ServiceForm },
  { path: 'profile', component: UserPage },
  { path: '**', redirectTo: '' }
];
