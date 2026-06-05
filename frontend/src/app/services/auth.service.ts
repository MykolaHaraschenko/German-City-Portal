import { Injectable, signal } from '@angular/core';

export interface UserProfile {
  fullName: string;
  address?: string;
  birthdate?: string;
  nationality?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  readonly isLoggedIn = signal<boolean>(false);
  readonly currentUser = signal<UserProfile | null>(null);
  readonly loginModalOpen = signal<boolean>(false);

  login(claims: any) {
    let profile: UserProfile = { fullName: 'Citizen' };
    
    // Helper to recursively extract string values from nested objects or arrays
    const extractValue = (val: any): string => {
      if (val == null) return '';
      if (typeof val === 'string') return val;
      if (typeof val === 'number' || typeof val === 'boolean') return val.toString();
      if (Array.isArray(val)) {
        return val.map(v => extractValue(v)).filter(v => v && !v.includes('...') && !v.includes('[object')).join(', ');
      }
      if (typeof val === 'object') {
        if (val['...'] !== undefined) return ''; // Ignore SD-JWT hash placeholders
        if (val.value !== undefined) return extractValue(val.value);
        return Object.entries(val)
          .map(([key, v]) => extractValue(v))
          .filter(v => v && !v.includes('...') && !v.includes('[object'))
          .join(' ');
      }
      return '';
    };
    
    if (claims) {
      const givenName = extractValue(claims.given_name || claims.givenName || '');
      const familyName = extractValue(claims.family_name || claims.familyName || '');
      const fullName = (givenName && familyName) ? `${givenName} ${familyName}` : extractValue(claims.full_name || claims.fullName || 'John Doe');
      
      // Extract address components robustly (supporting root-level and nested structure)
      let addressStr = '';
      const street = extractValue(claims.street_address || (claims.address && claims.address.street_address) || claims.street || (claims.address && claims.address.street) || '');
      const zip = extractValue(claims.postal_code || (claims.address && claims.address.postal_code) || claims.zip || (claims.address && claims.address.zip) || '');
      const city = extractValue(claims.locality || (claims.address && claims.address.locality) || claims.city || (claims.address && claims.address.city) || '');
      const country = extractValue(claims.country || (claims.address && claims.address.country) || claims.resident_country || '');
      
      addressStr = [street, [zip, city].filter(Boolean).join(' '), country].filter(Boolean).join(', ');
      
      const birthdate = extractValue(claims.birthdate || claims.birth_date || claims.date_of_birth || '');
      let nationality = extractValue(claims.nationalities || claims.nationality || '');
      
      // Fallback to 'DE' for German preprod PID provider if nationalities were not disclosed
      if (!nationality && claims.iss?.includes('bundesdruckerei.de')) {
        nationality = 'DE';
      }
      
      profile = {
        fullName,
        address: addressStr || undefined,
        birthdate: birthdate || undefined,
        nationality: nationality || undefined
      };
    }
    
    this.currentUser.set(profile);
    this.isLoggedIn.set(true);
  }

  logout() {
    this.currentUser.set(null);
    this.isLoggedIn.set(false);
  }
}
