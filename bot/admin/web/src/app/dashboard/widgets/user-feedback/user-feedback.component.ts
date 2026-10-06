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
import { Component, Input } from '@angular/core';

import { DashboardUsage, WidgetState } from '../../models/dashboard.model';

@Component({
  selector: 'tock-user-feedback',
  templateUrl: './user-feedback.component.html',
  styleUrls: ['./user-feedback.component.scss'],
  standalone: false
})
export class UserFeedbackComponent {
  @Input() usage: DashboardUsage;
  @Input() state: WidgetState = WidgetState.loading;

  WidgetState = WidgetState;

  get total(): number {
    return (this.usage?.feedbackUp ?? 0) + (this.usage?.feedbackDown ?? 0);
  }

  get positiveRate(): number {
    return this.total ? this.usage.feedbackUp / this.total : 0;
  }

  get responseRate(): number {
    return this.usage?.total ? this.total / this.usage.total : 0;
  }

  get delta(): number | null {
    if (!this.usage?.previousPositiveRate) return null;
    return (this.positiveRate - this.usage.previousPositiveRate) / this.usage.previousPositiveRate;
  }
}
