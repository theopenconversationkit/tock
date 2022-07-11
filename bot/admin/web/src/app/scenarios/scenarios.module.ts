import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  NbAccordionModule,
  NbAlertModule,
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbContextMenuModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbListModule,
  NbSelectModule,
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
import { IntentsSearchComponent } from './scenario-designer/intents-search/intents-search.component';
import { IntentCreateComponent } from './scenario-designer/intent-create/intent-create.component';
import { IntentEditComponent } from './scenario-designer/intent-edit/intent-edit.component';
import { ActionEditComponent } from './scenario-designer/action-edit/action-edit.component';
import { ContextCreateComponent } from './scenario-designer/context-create/context-create.component';
import { SentenceEditComponent } from './scenario-designer/intent-edit/sentence/sentence-edit.component';
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
import { NlpService } from '../nlp-tabs/nlp.service';
import { ScenarioFiltersComponent } from './scenarios-list/scenario-filters/scenario-filters.component';
import { ScenariosResolver } from './scenarios.resolver';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DndModule,
    ScenariosRoutingModule,
    BotSharedModule,
    SharedModule,
    NbAutocompleteModule,
    NbListModule,
    ReactiveFormsModule,
    NbButtonModule,
    NbCardModule,
    NbChatModule,
    NbCheckboxModule,
    NbFormFieldModule,
    NbIconModule,
    NbInputModule,
    NbSelectModule,
    NbSpinnerModule,
    NbTagModule,
    NbTooltipModule,
    NbTreeGridModule,
    NbAccordionModule,
    NbContextMenuModule,
    NbAlertModule
  ],
  declarations: [
    ScenariosListComponent,
    ScenarioEditComponent,
    ScenarioListSimpleComponent,
    ScenarioTreeComponent,
    ScenarioDesignerComponent,
    ScenarioDesignerEntryComponent,
    ScenarioFiltersComponent,
    IntentsSearchComponent,
    IntentCreateComponent,
    IntentEditComponent,
    ActionEditComponent,
    ContextCreateComponent,
    SentenceEditComponent
  ],
  exports: [],
  providers: [
    ScenarioService,
    ScenarioApiService,
    ScenarioDesignerNavigationGuard,
    ScenariosResolver,
    NlpService
  ],
  entryComponents: []
})
export class ScenariosModule {
  constructor() {}
}
