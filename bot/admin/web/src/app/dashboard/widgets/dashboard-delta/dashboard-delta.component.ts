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

/** Small signed variation chip. Renders nothing when the previous period is unknown. */
@Component({
  selector: 'tock-dashboard-delta',
  templateUrl: './dashboard-delta.component.html',
  styleUrls: ['./dashboard-delta.component.scss'],
  standalone: false
})
export class DashboardDeltaComponent {
  /** Relative variation, e.g. 0.18 for +18%. */
  @Input() value: number | null = null;

  /** Below this absolute variation the delta is considered flat. */
  @Input() flatThreshold: number = 0.01;

  get isFlat(): boolean {
    return this.value !== null && Math.abs(this.value) < this.flatThreshold;
  }

  get isUp(): boolean {
    return this.value !== null && this.value > 0 && !this.isFlat;
  }

  get isDown(): boolean {
    return this.value !== null && this.value < 0 && !this.isFlat;
  }
}
