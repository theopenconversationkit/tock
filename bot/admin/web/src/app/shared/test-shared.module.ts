import { APP_BASE_HREF, CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbDatepickerModule,
  NbDialogModule,
  NbFormFieldModule,
  NbIconLibraries,
  NbIconModule,
  NbInputModule,
  NbRadioModule,
  NbSelectModule,
  NbSpinnerModule,
  NbThemeModule,
  NbToastrModule,
  NbTooltipModule,
  NbWindowModule
} from '@nebular/theme';
import { TranslocoTestingModule, TranslocoTestingOptions } from '@jsverse/transloco';
import { DialogService } from '../core-nlp/dialog.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RestService } from '../core-nlp/rest/rest.service';
import { BotService } from '../bot/bot-service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ScrollTopButtonComponent } from './components';

export function getTranslocoTestingModule(options: TranslocoTestingOptions = {}) {
  return TranslocoTestingModule.forRoot({
    langs: { en: {}, fr: {} },
    translocoConfig: { availableLangs: ['en', 'fr'], defaultLang: 'en' },
    preloadLangs: true,
    ...options
  });
}

@NgModule({
  declarations: [ScrollTopButtonComponent],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslocoTestingModule,
    NbCardModule,
    NbButtonModule,
    NbIconModule,
    NbInputModule,
    NbSelectModule,
    NbFormFieldModule,
    NbCheckboxModule,
    NbTooltipModule,
    NbSpinnerModule,
    NbToastrModule,
    NbWindowModule,
    NbDialogModule,
    NbDatepickerModule,
    NbRadioModule,
    ScrollTopButtonComponent
  ],
  imports: [
    NoopAnimationsModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forRoot([]),
    NbThemeModule.forRoot({ name: 'default' }),
    getTranslocoTestingModule(),
    NbCardModule,
    NbButtonModule,
    NbIconModule,
    NbInputModule,
    NbSelectModule,
    NbFormFieldModule,
    NbCheckboxModule,
    NbTooltipModule,
    NbSpinnerModule,
    NbToastrModule.forRoot(),
    NbWindowModule.forRoot(),
    NbDialogModule.forRoot(),
    NbDatepickerModule.forRoot(),
    NbRadioModule
  ],
  providers: [
    DialogService,
    RestService,
    BotService,
    { provide: APP_BASE_HREF, useValue: '/' },
    provideHttpClient(withInterceptorsFromDi()),
    provideHttpClientTesting()
  ]
})
export class TestSharedModule {
  constructor(private iconLibraries: NbIconLibraries) {
    this.iconLibraries.registerFontPack('bootstrap-icons', { iconClassPrefix: 'bi' });
    this.iconLibraries.setDefaultPack('bootstrap-icons');
  }
}
