/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfirmDialogComponent } from './confirm-dialog/confirm-dialog.component';
import { MapToIterablePipe } from './map-to-iterable.pipe';
import { ScrollComponent } from '../scroll/scroll.component';
import { NbEvaIconsModule } from '@nebular/eva-icons';
import {
  NbButtonModule,
  NbDatepickerModule,
  NbInputModule,
  NbToggleModule,
  NbIconModule,
  NbActionsModule,
  NbTreeGridModule,
  NbCardModule,
  NbSelectModule
} from '@nebular/theme';
import { TempPaginationComponent } from './temp-pagination/temp-pagination.component';

@NgModule({
  imports: [
    CommonModule,
    NbActionsModule,
    NbButtonModule,
    NbEvaIconsModule,
    NbDatepickerModule,
    NbIconModule,
    NbInputModule,
    NbToggleModule,
    NbTreeGridModule,
    NbCardModule,
    NbSelectModule
  ],
  declarations: [ConfirmDialogComponent, MapToIterablePipe, ScrollComponent, TempPaginationComponent],
  providers: [],
  exports: [
    FormsModule,
    MapToIterablePipe,
    NbActionsModule,
    NbButtonModule,
    NbEvaIconsModule,
    NbDatepickerModule,
    NbIconModule,
    NbInputModule,
    NbToggleModule,
    NbTreeGridModule,
    TempPaginationComponent
  ]
})
export class SharedModule {}
