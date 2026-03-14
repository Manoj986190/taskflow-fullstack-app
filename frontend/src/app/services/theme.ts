import { Injectable } from '@angular/core';
import { SettingsService } from './settings';

@Injectable({ providedIn: 'root' })
export class ThemeService {

  private readonly STORAGE_KEY = 'taskflow_theme';

  constructor(private settingsService: SettingsService) {}

  init() {
    const saved = localStorage.getItem(this.STORAGE_KEY) || 'LIGHT';
    this.applyThemePublic(saved);
  }

  setTheme(theme: string) {
    localStorage.setItem(this.STORAGE_KEY, theme);
    this.applyThemePublic(theme);
    this.settingsService.updatePreferences({ theme } as any).subscribe();
  }

  getTheme(): string {
    return localStorage.getItem(this.STORAGE_KEY) || 'LIGHT';
  }

  // ✅ Public so settings.ts can call on load
  applyThemePublic(theme: string) {
    const html = document.documentElement;
    html.removeAttribute('data-theme');
    if (theme === 'DARK') {
      html.setAttribute('data-theme', 'dark');
    } else if (theme === 'SYSTEM') {
      const prefersDark = window.matchMedia(
        '(prefers-color-scheme: dark)').matches;
      if (prefersDark) html.setAttribute('data-theme', 'dark');
    }
  }
}