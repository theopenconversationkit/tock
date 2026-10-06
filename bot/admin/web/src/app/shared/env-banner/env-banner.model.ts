// src/app/shared/env-banner/env-banner.model.ts
export interface EnvBannerConfig {
  /** Free-form label displayed in the banner, e.g. 'REC', 'STAGING', 'QA-2'. */
  label: string;
  /** Optional background color. Defaults to amber. */
  color?: string;
  /** Optional text color. Auto-derived from `color` when omitted. */
  textColor?: string;
}
