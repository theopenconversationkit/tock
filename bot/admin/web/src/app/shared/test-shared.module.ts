import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NbEvaIconsModule } from '@nebular/eva-icons';
import { NbThemeModule } from '@nebular/theme';

import { AutofocusDirective } from './directives';

@NgModule({
  declarations: [AutofocusDirective],
  imports: [
    BrowserAnimationsModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,

    NbThemeModule.forRoot({ name: 'default' }),
    NbEvaIconsModule
  ],
  exports: [AutofocusDirective, CommonModule, FormsModule, ReactiveFormsModule]
})
export class TestSharedModule {}
