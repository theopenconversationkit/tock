import { Component, OnDestroy } from '@angular/core';
import { NbThemeService } from '@nebular/theme';
import { takeWhile } from 'rxjs/operators';
import {AuthService} from "../../../core-nlp/auth/auth.service";

@Component({
  selector: 'ngx-one-column-layout',
  styleUrls: ['./one-column.layout.scss'],
  template: `
    <nb-layout>

      <nb-layout-header fixed *ngIf="auth.isLoggedIn()">
        <ngx-header></ngx-header>
      </nb-layout-header>

      <nb-sidebar class="menu-sidebar" tag="menu-sidebar" responsive *ngIf="auth.isLoggedIn()">
        <ng-content select="nb-menu"></ng-content>
      </nb-sidebar>

      <nb-layout-column>
        <ng-content select="router-outlet"></ng-content>
      </nb-layout-column>

      <nb-layout-footer fixed *ngIf="auth.isLoggedIn()">
        <ngx-footer></ngx-footer>
      </nb-layout-footer>
      
    </nb-layout>
  `,
})
export class OneColumnLayoutComponent implements OnDestroy {

  private alive = true;

  currentTheme: string;

  constructor(public auth: AuthService,
  protected themeService: NbThemeService) {
    this.themeService.getJsTheme()
      .pipe(takeWhile(() => this.alive))
      .subscribe(theme => {
        this.currentTheme = theme.name;
    });
  }

  ngOnDestroy() {
    this.alive = false;
  }
}
