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
import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

import { BotSharedService } from '../../shared/bot-shared.service';
import { DashboardPeriod } from '../models/dashboard.model';

/**
 * Client-side state of the dashboard: the bot currently in context and the
 * reporting period shared by every time-based widget.
 *
 * Nothing is persisted. Leaving the feature resets the state.
 */
@Injectable()
export class DashboardStateService {
  private readonly botSharedService = inject(BotSharedService);

  private readonly _period = new BehaviorSubject<DashboardPeriod>(30);
  private readonly _displayTests = new BehaviorSubject<boolean>(
    // Shared with the metrics board and the dialogs views through session storage,
    // so the choice follows the user across the studio.
    !!this.botSharedService.session_storage?.dialogs?.displayTests
  );
  private currentBotKey: string | null = null;

  get period(): DashboardPeriod {
    return this._period.value;
  }

  get period$(): Observable<DashboardPeriod> {
    return this._period.asObservable();
  }

  setPeriod(period: DashboardPeriod): void {
    if (period !== this._period.value) {
      this._period.next(period);
    }
  }

  get displayTests(): boolean {
    return this._displayTests.value;
  }

  get displayTests$(): Observable<boolean> {
    return this._displayTests.asObservable();
  }

  toggleDisplayTests(): void {
    const displayTests = !this._displayTests.value;

    this.botSharedService.session_storage = {
      ...this.botSharedService.session_storage,
      dialogs: { ...this.botSharedService.session_storage?.dialogs, displayTests }
    };

    this._displayTests.next(displayTests);
  }

  /**
   * Called on every emission of botConfiguration.configurations.
   * Resets only when the bot actually changed: resetting on every emission would
   * wipe the state on plain view navigation.
   */
  applyBotContext(botKey: string): boolean {
    if (this.currentBotKey === botKey) {
      return false;
    }
    this.currentBotKey = botKey;
    this.reset();
    return true;
  }

  /** The bot changed: only the period is reset. displayTests is a user preference. */
  reset(): void {
    this._period.next(30);
  }
}
