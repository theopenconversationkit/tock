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

import { Component, EventEmitter, Output } from '@angular/core';
import { AnalyticsService } from '../../analytics.service';
import { StateService } from '../../../core-nlp/state.service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { Subject, take } from 'rxjs';

@Component({
  selector: 'tock-activate-satisfaction',
  templateUrl: './activate-satisfaction.component.html',
  styleUrls: ['./activate-satisfaction.component.css']
})
export class ActivateSatisfactionComponent {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Output() enableSatisfaction = new EventEmitter<boolean>();
  loading = false;

  constructor(private analytics: AnalyticsService, private state: StateService, private dialog: DialogService) {}

  activate() {
    this.loading = true;
    this.analytics
      .createSatisfactionModule()
      .pipe(take(1))
      .subscribe(() => {
        this.enableSatisfaction.emit(true);
        this.state.resetConfiguration();
        this.loading = false;
        this.dialog.notify('Satisfaction Module Activated', 'Module Activation', {
          duration: 2000,
          status: 'success'
        });
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
