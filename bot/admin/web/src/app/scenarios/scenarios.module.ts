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
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  NbButtonModule,
  NbCardModule,
  NbIconModule,
  NbInputModule,
  NbSpinnerModule,
  NbTagModule,
  NbTooltipModule,
  NbTreeGridModule
} from '@nebular/theme';

import { ScenariosListComponent } from './scenarios-list/scenarios-list.component';
import {
  ScenarioDesignerNavigationGuard,
  ScenarioDesignerComponent
} from './scenario-designer/scenario-designer.component';
import { ScenarioDesignerEntryComponent } from './scenario-designer/scenario-designer-entry.component';
import { DndModule } from 'ngx-drag-drop';
import { BotSharedModule } from '../shared/bot-shared.module';
import { SharedModule } from '../shared-nlp/shared.module';
import { NbChatModule, NbCheckboxModule } from '@nebular/theme';
import { ScenarioService } from './services/scenario.service';
import { ScenarioApiService } from './services/scenario.api.service';
import { ScenarioEditComponent } from './scenario-edit/scenario-edit.component';
import { ScenariosRoutingModule } from './scenarios-routing.module';
import { ScenarioListSimpleComponent } from './scenarios-list/scenario-list-simple/scenario-list-simple.component';
import { ScenarioTreeComponent } from './scenarios-list/scenario-tree/scenario-tree.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    SharedModule,
    BotSharedModule,
    ScenariosRoutingModule,
    DndModule,
    NbCheckboxModule,
    NbChatModule,
    NbCardModule,
    NbTreeGridModule,
    NbSpinnerModule,
    NbButtonModule,
    NbIconModule,
    NbTooltipModule,
    NbInputModule,
    NbTagModule,
    ReactiveFormsModule
  ],
  declarations: [
    ScenariosListComponent,
    ScenarioEditComponent,
    ScenarioListSimpleComponent,
    ScenarioTreeComponent,
    ScenarioDesignerComponent,
    ScenarioDesignerEntryComponent,
    ScenarioEditComponent
  ],
  exports: [],
  providers: [ScenarioService, ScenarioApiService, ScenarioDesignerNavigationGuard],
  entryComponents: []
})
export class ScenariosModule {
  constructor() {}
}
