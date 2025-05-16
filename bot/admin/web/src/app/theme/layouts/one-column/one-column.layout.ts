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

import { Component, OnDestroy } from '@angular/core';
import { NbThemeService } from '@nebular/theme';
import { takeWhile } from 'rxjs/operators';
import { AuthService } from '../../../core-nlp/auth/auth.service';

@Component({
  selector: 'tock-one-column-layout',
  styleUrls: ['./one-column.layout.scss'],
  template: `
    <nb-layout>
      <nb-layout-header
        fixed
        *ngIf="auth.isLoggedIn()"
      >
        <tock-header></tock-header>
      </nb-layout-header>

      <nb-sidebar
        class="menu-sidebar"
        tag="menu-sidebar"
        responsive
        [compactedBreakpoints]="['xs', 'sm']"
        *ngIf="auth.isLoggedIn()"
      >
        <ng-content select="nb-menu"></ng-content>
      </nb-sidebar>

      <nb-layout-column>
        <ng-content select="router-outlet"></ng-content>
      </nb-layout-column>

      <nb-layout-footer
        fixed
        *ngIf="auth.isLoggedIn()"
      >
        <tock-footer></tock-footer>
      </nb-layout-footer>
    </nb-layout>
  `
})
export class OneColumnLayoutComponent implements OnDestroy {
  private alive = true;

  currentTheme: string;

  constructor(public auth: AuthService, protected themeService: NbThemeService) {
    this.themeService
      .getJsTheme()
      .pipe(takeWhile(() => this.alive))
      .subscribe((theme) => {
        this.currentTheme = theme.name;
      });
  }

  ngOnDestroy() {
    this.alive = false;
  }
}
