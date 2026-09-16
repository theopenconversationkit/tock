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

/**
 * Kept for consistency with the other feature modules: the tabset renders nothing,
 * navigation goes through the main sidebar menu. Reproducing the pattern rather than
 * cleaning it up keeps this module aligned with the rest of the studio.
 */
@Component({
  selector: 'tock-dashboard-tabs',
  template: '<nb-route-tabset></nb-route-tabset>',
  standalone: false
})
export class DashboardTabsComponent {}
