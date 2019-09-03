import { Component, Input, OnInit } from '@angular/core';

import { NbMenuService, NbSidebarService } from '@nebular/theme';
import {StateService} from "../../../core-nlp/state.service";
import {AuthService} from "../../../core-nlp/auth/auth.service";
import {SettingsService} from "../../../core-nlp/settings.service";

@Component({
  selector: 'ngx-header',
  styleUrls: ['./header.component.scss'],
  templateUrl: './header.component.html',
})
export class HeaderComponent implements OnInit {

  @Input() position = 'normal';

  selectedLocale: string;
  selectedApplication: string;

  constructor(private sidebarService: NbSidebarService,
              private menuService: NbMenuService,
              public state: StateService,
              public auth: AuthService,
              public settings: SettingsService) {

    this.selectedLocale = settings.currentLocale ? settings.currentLocale : state.currentLocale;
    this.selectedApplication = settings.currentApplicationName ? settings.currentApplicationName : state.currentApplication.name;
    this.state.loadLocale(this.selectedLocale);
  }

  ngOnInit() {
  }

  toggleSidebar(): boolean {
    this.sidebarService.toggle(true, 'menu-sidebar');

    return false;
  }

  goToHome() {
    this.menuService.navigateHome();
  }

  changeApplication() {
    this.state.changeApplicationWithName(this.selectedApplication);
    this.settings.onApplicationChange(this.selectedApplication)
  }

  changeLocale() {
    this.state.changeLocale(this.selectedLocale);
  }

}
