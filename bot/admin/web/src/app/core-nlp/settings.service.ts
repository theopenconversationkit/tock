/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Injectable } from '@angular/core';

@Injectable()
export class SettingsService {
  currentApplicationName: string;
  currentLocale: string;
  currentTheme: string;

  constructor() {
    this.currentApplicationName = localStorage.getItem('_tock_current_app');
    this.currentLocale = localStorage.getItem('_tock_current_locale');
    this.currentTheme = localStorage.getItem('_tock_current_theme');
  }

  onApplicationChange(applicationName: string): void {
    this.currentApplicationName = applicationName;
    localStorage.setItem('_tock_current_app', this.currentApplicationName);
  }

  onLocaleChange(locale: string): void {
    this.currentLocale = locale;
    localStorage.setItem('_tock_current_locale', this.currentLocale);
  }

  onThemeChange(theme: string): void {
    this.currentTheme = theme;
    localStorage.setItem('_tock_current_theme', this.currentTheme);
  }
}
