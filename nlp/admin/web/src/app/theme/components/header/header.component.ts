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
