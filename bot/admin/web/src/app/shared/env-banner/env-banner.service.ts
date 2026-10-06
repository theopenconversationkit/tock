// src/app/shared/env-banner/env-banner.service.ts
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { EnvBannerConfig } from './env-banner.model';

const CONFIG_URL = 'assets/env-banner.json';
const STORAGE_KEY = 'tock-env-banner-hidden';
const QUERY_PARAM = 'env-banner';
const DEFAULT_COLOR = '#f2a900';

@Injectable({ providedIn: 'root' })
export class EnvBannerService {
  private config: EnvBannerConfig | null = null;
  private hidden = false;

  /**
   * Resolves the banner configuration at runtime.
   * Order of precedence: query param override > runtime file > build-time default.
   * A missing file, an empty label, or any fetch failure resolves to no banner.
   */
  async load(): Promise<void> {
    const override = new URLSearchParams(location.search).get(QUERY_PARAM);

    if (override) {
      sessionStorage.removeItem(STORAGE_KEY);
      if (override.toLowerCase() !== 'show') {
        this.config = { label: override.toUpperCase() };
        return;
      }
    }

    this.hidden = sessionStorage.getItem(STORAGE_KEY) === 'true';
    this.config = (await this.fetchConfig()) ?? environment.envBanner ?? null;
  }

  private async fetchConfig(): Promise<EnvBannerConfig | null> {
    try {
      const response = await fetch(CONFIG_URL, { cache: 'no-store' });
      if (!response.ok) return null;
      // A SPA fallback returns index.html with a 200 status, so the status alone is not enough.
      if (!response.headers.get('content-type')?.includes('json')) return null;

      const config = (await response.json()) as EnvBannerConfig;
      return config?.label ? config : null;
    } catch {
      return null;
    }
  }

  get label(): string | null {
    return this.config?.label ?? null;
  }

  get color(): string {
    return this.config?.color ?? DEFAULT_COLOR;
  }

  get textColor(): string {
    return this.config?.textColor ?? this.contrastOn(this.color);
  }

  get visible(): boolean {
    return !!this.config && !this.hidden;
  }

  hide(): void {
    this.hidden = true;
    sessionStorage.setItem(STORAGE_KEY, 'true');
  }

  /** Picks a readable foreground color for an arbitrary background, using relative luminance. */
  private contrastOn(background: string): string {
    const hex = background.replace('#', '');
    if (hex.length !== 6) return '#1a1a1a';

    const [r, g, b] = [0, 2, 4].map((i) => parseInt(hex.slice(i, i + 2), 16) / 255);
    const luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
    return luminance > 0.55 ? '#1a1a1a' : '#ffffff';
  }
}
