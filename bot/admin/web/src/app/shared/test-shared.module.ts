import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NbEvaIconsModule } from '@nebular/eva-icons';
import { NbThemeModule } from '@nebular/theme';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forRoot([]),

    NbThemeModule.forRoot({ name: 'default' }),
    NbEvaIconsModule
  ],
  exports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class TestSharedModule {}
