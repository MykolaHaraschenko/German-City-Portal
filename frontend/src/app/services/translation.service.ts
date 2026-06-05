import { Injectable, signal, computed } from '@angular/core';

export interface TranslationDictionary {
  portal_title: string;
  hero_title: string;
  search_placeholder: string;
  search_button: string;
  citizens_tab: string;
  businesses_tab: string;
  top_services: string;
  categories_title: string;
  login_eudi: string;
  logout: string;
  profile: string;
  imprint: string;
  privacy: string;
  accessibility: string;
  contact: string;
  copyright: string;
  modal_title: string;
  modal_desc: string;
  modal_close: string;
  modal_success: string;
  modal_loading: string;
  modal_error: string;
  back_button: string;
  apply_online: string;
  back_to_overview: string;
  important_info: string;
  requirements: string;
  documents_required: string;
  cost: string;
  processing_time: string;
  faq_title: string;
  faq_desc: string;
  step_label: string;
  next_button: string;
  prev_button: string;
  submit_button: string;
  prefill_eudi: string;
  prefilled_eudi: string;
  form_success_title: string;
  form_success_desc: string;
  under_construction: string;
}

const de: TranslationDictionary = {
  portal_title: 'Bürgerportal Deutschland',
  hero_title: 'Wie können wir Ihnen heute helfen?',
  search_placeholder: 'Intelligente Suche...',
  search_button: 'Suchen',
  citizens_tab: 'Bürger',
  businesses_tab: 'Unternehmen',
  top_services: 'Top-Dienstleistungen',
  categories_title: 'Dienstleistungen',
  login_eudi: 'Mit EUDI anmelden',
  logout: 'abmelden',
  profile: 'Profile',
  imprint: 'Impressum',
  privacy: 'Datenschutz',
  accessibility: 'Barrierefreiheit',
  contact: 'Kontakt aufnehmen',
  copyright: '© 2026 Freistaat Sachsen. Alle Rechte vorbehalten.',
  modal_title: 'Mit EUDI Wallet anmelden',
  modal_desc: 'Scannen Sie den QR-Code mit Ihrer EUDI Wallet App, um sich sicher zu authentifizieren.',
  modal_close: 'Schließen',
  modal_success: 'Erfolgreich angemeldet!',
  modal_loading: 'Verbindung wird hergestellt...',
  modal_error: 'Fehler bei der Wallet-Authentifizierung. Bitte versuchen Sie es erneut.',
  back_button: 'Zurück',
  apply_online: 'Online beantragen',
  back_to_overview: 'Zurück zur Übersicht',
  important_info: 'Wichtige Informationen',
  requirements: 'Voraussetzungen',
  documents_required: 'Benötigte Unterlagen',
  cost: 'Kosten',
  processing_time: 'Bearbeitungszeit',
  faq_title: 'Häufig gestellte Fragen',
  faq_desc: 'Haben Sie noch Unklarheiten? Hier finden Sie Antworten auf die wichtigsten Fragen.',
  step_label: 'Schritt',
  next_button: 'Weiter',
  prev_button: 'Zurück',
  submit_button: 'Antrag einreichen',
  prefill_eudi: 'Mit EUDI Wallet ausfüllen',
  prefilled_eudi: 'Aus EUDI Wallet ausgefüllt!',
  form_success_title: 'Antrag erfolgreich eingereicht!',
  form_success_desc: 'Ihr Antrag wird unter der folgenden ID bearbeitet:',
  under_construction: 'Noch in Arbeit'
};

const en: TranslationDictionary = {
  portal_title: 'Germany Citizen Portal',
  hero_title: 'How can we help you today?',
  search_placeholder: 'Smart search...',
  search_button: 'Search',
  citizens_tab: 'Citizens',
  businesses_tab: 'Businesses',
  top_services: 'Top Services',
  categories_title: 'Services Categories',
  login_eudi: 'Sign in with EUDI',
  logout: 'Logout',
  profile: 'Profile',
  imprint: 'Imprint',
  privacy: 'Privacy Policy',
  accessibility: 'Accessibility Statement',
  contact: 'Contact Us',
  copyright: '© 2026 Free State of Saxony. All rights reserved.',
  modal_title: 'Sign in with EUDI Wallet',
  modal_desc: 'Scan the QR code using your EUDI Wallet App to authenticate securely.',
  modal_close: 'Close',
  modal_success: 'Successfully authenticated!',
  modal_loading: 'Establishing connection...',
  modal_error: 'Wallet authentication failed. Please try again.',
  back_button: 'Back',
  apply_online: 'Apply Online',
  back_to_overview: 'Back to Overview',
  important_info: 'Important Information',
  requirements: 'Requirements',
  documents_required: 'Required Documents',
  cost: 'Costs',
  processing_time: 'Processing Time',
  faq_title: 'Frequently Asked Questions',
  faq_desc: 'Do you still have questions? Find answers to the most important questions here.',
  step_label: 'Step',
  next_button: 'Next',
  prev_button: 'Previous',
  submit_button: 'Submit Application',
  prefill_eudi: 'Fill with EUDI Wallet',
  prefilled_eudi: 'Pre-filled from EUDI Wallet!',
  form_success_title: 'Application submitted successfully!',
  form_success_desc: 'Your application is being processed under the following ID:',
  under_construction: 'Under construction'
};

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  readonly currentLang = signal<'de' | 'en'>('de');

  readonly t = computed<TranslationDictionary>(() => {
    return this.currentLang() === 'en' ? en : de;
  });

  setLang(lang: 'de' | 'en') {
    this.currentLang.set(lang);
  }

  toggleLang() {
    this.currentLang.update(l => l === 'de' ? 'en' : 'de');
  }
}
