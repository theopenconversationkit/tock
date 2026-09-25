// src/app/shared/env-banner/env-banner.component.ts
import { Component, inject } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { EnvBannerService } from './env-banner.service';

@Component({
  selector: 'tock-env-banner',
  standalone: true,
  imports: [TranslocoModule],
  styleUrls: ['./env-banner.component.scss'],
  template: `
    @if (envBanner.label) {
    <div
      class="env-banner__rule"
      [style.background]="envBanner.color"
      aria-hidden="true"
    ></div>

    @if (envBanner.visible) {
    <div
      class="env-banner"
      role="status"
      [style.background]="envBanner.color"
      [style.color]="envBanner.textColor"
    >
      <span class="env-banner__label">{{ envBanner.label }}</span>
      <button
        type="button"
        class="env-banner__close"
        [attr.aria-label]="'env-banner.hide' | transloco"
        [title]="'env-banner.hide' | transloco"
        (click)="envBanner.hide()"
      >
        <i class="bi bi-x-lg"></i>
      </button>
    </div>
    } }
  `
})
export class EnvBannerComponent {
  readonly envBanner = inject(EnvBannerService);
}
