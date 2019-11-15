/*
 * Copyright (C) 2017/2019 VSCT
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

import {Component, Input, OnInit} from '@angular/core';

import {NbMenuService, NbSidebarService, NbThemeService} from '@nebular/theme';
import {StateService} from "../../../core-nlp/state.service";
import {AuthService} from "../../../core-nlp/auth/auth.service";
import {SettingsService} from "../../../core-nlp/settings.service";
import {map, takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';

@Component({
  selector: 'ngx-header',
  styleUrls: ['./header.component.scss'],
  templateUrl: './header.component.html',
})
export class HeaderComponent implements OnInit {

  @Input() position = 'normal';

  isDark = false;
  currentTheme = 'default';
  private destroy$: Subject<void> = new Subject<void>();

  constructor(private sidebarService: NbSidebarService,
              private menuService: NbMenuService,
              public state: StateService,
              public auth: AuthService,
              public settings: SettingsService,
              private themeService: NbThemeService,) {

  }

  ngOnInit() {
    this.currentTheme = this.themeService.currentTheme;
    this.themeService.onThemeChange()
      .pipe(
        map(({name}) => name),
        takeUntil(this.destroy$),
      )
      .subscribe(themeName => this.currentTheme = themeName);
  }

  changeTheme(themeName: string) {
    this.themeService.changeTheme(themeName);
  }

  switchTheme() {
    this.isDark = !this.isDark;
    if (this.isDark) {
      this.changeTheme('dark')
    } else {
      this.changeTheme('default')
    }
  }

  toggleSidebar(): boolean {
    this.sidebarService.toggle(true, 'menu-sidebar');

    return false;
  }

  goToHome() {
    this.menuService.navigateHome();
  }

  changeApplication(app) {
    setTimeout(_ => {
      this.state.changeApplicationWithName(app);
      this.settings.onApplicationChange(app);
    });
  }

  changeLocale(locale) {
    setTimeout(_ => this.state.changeLocale(locale));
  }

}
