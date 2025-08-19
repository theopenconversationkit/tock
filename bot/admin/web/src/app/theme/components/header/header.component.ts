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

import { Component, Inject, Input, OnDestroy, OnInit } from '@angular/core';

import { NbMenuService, NbSidebarService, NbThemeService } from '@nebular/theme';
import { StateService } from '../../../core-nlp/state.service';
import { AuthService } from '../../../core-nlp/auth/auth.service';
import { SettingsService } from '../../../core-nlp/settings.service';
import { Subject, take, takeUntil } from 'rxjs';
import { APP_BASE_HREF } from '@angular/common';
import { ApplicationService } from '../../../core-nlp/applications.service';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { CoreConfig } from '../../../core-nlp/core.config';
import { Router } from '@angular/router';
import { TestDialogService } from '../../../shared/components/test-dialog/test-dialog.service';

@Component({
  selector: 'tock-header',
  styleUrls: ['./header.component.scss'],
  templateUrl: './header.component.html'
})
export class HeaderComponent implements OnInit, OnDestroy {
  private destroy: Subject<boolean> = new Subject<boolean>();

  @Input() position = 'normal';

  currentTheme = 'default';

  currentApplicationName: string;

  constructor(
    private sidebarService: NbSidebarService,
    private menuService: NbMenuService,
    public state: StateService,
    public auth: AuthService,
    public settings: SettingsService,
    private themeService: NbThemeService,
    @Inject(APP_BASE_HREF) public baseHref: string,
    private applicationService: ApplicationService,
    private botConfiguration: BotConfigurationService,
    private config: CoreConfig,
    private router: Router,
    private testDialogService: TestDialogService
  ) {}

  ngOnInit() {
    this.currentTheme = this.settings.currentTheme ? this.settings.currentTheme : 'default';

    if (this.currentTheme !== this.themeService.currentTheme) {
      this.themeService.changeTheme(this.currentTheme);
    }
    if (this.settings.currentLocale != null) {
      this.state.currentLocale = this.settings.currentLocale;
    }

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy)).subscribe((confs) => {
      this.currentApplicationName = '';
      setTimeout(() => {
        this.currentApplicationName = this.state?.currentApplication?.name;
      });
    });
  }

  get currentNamespaceName() {
    return this.state.namespaces?.find((n) => n.current).namespace;
  }

  changeNamespace(namespace: string) {
    this.applicationService
      .selectNamespace(namespace)
      .pipe(take(1))
      .subscribe((_) =>
        this.auth.loadUser().subscribe((_) => {
          this.applicationService.resetConfiguration();

          this.applicationService
            .getApplications()
            .pipe(take(1))
            .subscribe((applications) => {
              if (!applications.length) {
                this.router.navigateByUrl(this.config.configurationUrl);
              }
            });
        })
      );
  }

  changeTheme(themeName: string) {
    this.settings.onThemeChange(themeName);
    this.themeService.changeTheme(themeName);
    this.currentTheme = themeName;
  }

  switchTheme() {
    if (!this.isDarkTheme()) {
      this.changeTheme('dark');
    } else {
      this.changeTheme('default');
    }
  }

  isDarkTheme() {
    return this.currentTheme === 'dark';
  }

  toggleSidebar(): boolean {
    this.sidebarService.toggle(true, 'menu-sidebar');

    return false;
  }

  goToHome() {
    this.menuService.navigateHome();
  }

  goToDialogs() {
    this.router.navigateByUrl('/analytics/dialogs');
  }

  changeApplication(app) {
    setTimeout((_) => {
      this.state.changeApplicationWithName(app);
      this.settings.onApplicationChange(app);
    });
  }

  changeLocale(locale) {
    setTimeout((_) => this.state.changeLocale(locale));
  }

  openDialogTest() {
    this.testDialogService.openTestDialog();
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
