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
import { Component } from '@angular/core';
import { NbToastrService } from '@nebular/theme';
import { SettingsService } from 'src/app/core-nlp/settings.service';

import { AnalyticsService } from '../analytics.service';
import { UserAnalyticsPreferences } from './UserAnalyticsPreferences';

@Component({
  selector: 'tock-preferences',
  templateUrl: './preferences.component.html',
  styleUrls: ['./preferences.component.css']
})
export class PreferencesComponent {
  preferences: UserAnalyticsPreferences;

  constructor(protected settings: SettingsService, private toastrService: NbToastrService, private analytics: AnalyticsService) {
    this.preferences = this.analytics.getUserPreferences();
  }

  save() {
    this.analytics.onUserAnalyticsSettingsChange(JSON.stringify(this.preferences));
    this.toastrService.show('User preferences updated successfully', 'Configuration update', {
      duration: 3000,
      status: 'success'
    });
  }

  cancel() {
    this.preferences = this.analytics.getUserPreferences();
  }
}
