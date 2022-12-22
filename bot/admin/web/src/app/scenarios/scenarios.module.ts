import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  NbAccordionModule,
  NbAlertModule,
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbChatModule,
  NbCheckboxModule,
  NbContextMenuModule,
  NbDialogModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbListModule,
  NbRadioModule,
  NbSelectModule,
  NbSpinnerModule,
  NbStepperModule,
  NbTagModule,
  NbTooltipModule,
  NbTreeGridModule
} from '@nebular/theme';
import { DndModule } from 'ngx-drag-drop';

import { ScenariosRoutingModule } from './scenarios-routing.module';
import { ScenariosResolver } from './scenarios.resolver';
import { ScenariosListComponent } from './scenarios-list/scenarios-list.component';
import { ScenarioDesignerNavigationGuard, ScenarioDesignerComponent } from './scenario-designer/scenario-designer.component';
import { ScenarioConceptionItemComponent } from './scenario-designer/scenario-conception/scenario-conception-item.component';
import { IntentsSearchComponent } from './scenario-designer/scenario-conception/intents-search/intents-search.component';
import { IntentCreateComponent } from './scenario-designer/scenario-conception/intent-create/intent-create.component';
import { IntentEditComponent } from './scenario-designer/scenario-conception/intent-edit/intent-edit.component';
import { ActionEditComponent } from './scenario-designer/scenario-conception/action-edit/action-edit.component';
import { ContextCreateComponent } from './scenario-designer/scenario-conception/context-create/context-create.component';
import { SentenceEditComponent } from './scenario-designer/scenario-conception/intent-edit/sentence/sentence-edit.component';
import { ModeStepperComponent } from './scenario-designer/mode-stepper/mode-stepper.component';
import { ScenarioConceptionComponent } from './scenario-designer/scenario-conception/scenario-conception.component';
import { ScenarioProductionComponent } from './scenario-designer/scenario-production/scenario-production.component';
import { ScenarioStateGroupComponent } from './scenario-designer/scenario-production/state-group/state-group.component';
import { ScenarioProductionStateGroupAddComponent } from './scenario-designer/scenario-production/state-group/state-group-add/state-group-add.component';
import { ScenarioTransitionComponent } from './scenario-designer/scenario-production/state-group/transition/transition.component';
import { ScenarioPublishingComponent } from './scenario-designer/scenario-publishing/scenario-publishing.component';
import { BotSharedModule } from '../shared/bot-shared.module';
import { SharedModule } from '../shared-nlp/shared.module';
import { ScenarioEditComponent } from './scenarios-list/scenario-edit/scenario-edit.component';
import { ScenarioListSimpleComponent } from './scenarios-list/scenario-list-simple/scenario-list-simple.component';
import { ScenarioImportComponent } from './scenarios-list/scenario-import/scenario-import.component';
import { ScenarioExportComponent } from './scenarios-list/scenario-export/scenario-export.component';
import { NlpService } from '../nlp-tabs/nlp.service';
import { ScenarioFiltersComponent } from './scenarios-list/scenario-filters/scenario-filters.component';
import { ScenarioDesignerService } from './scenario-designer/scenario-designer.service';
import { ContextsGraphComponent } from './scenario-designer/contexts-graph/contexts-graph.component';
import { ScenarioCanvasWrapperComponent } from './scenario-designer/scenario-canvas-wrapper/scenario-canvas-wrapper.component';
import { TriggerCreateComponent } from './scenario-designer/scenario-conception/trigger-create/trigger-create.component';
import { ScenariosSettingsComponent } from './scenarios-list/scenarios-settings/scenarios-settings.component';
import { ScenarioApiService, ScenarioService, ScenarioSettingsService } from './services';

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
    NbAlertModule,
    NbStepperModule,
    NbRadioModule,
    NbDialogModule.forChild()
  ],
  declarations: [
    ScenariosListComponent,
    ScenarioEditComponent,
    ScenarioListSimpleComponent,
    ScenarioDesignerComponent,
    ScenarioConceptionItemComponent,
    ScenarioFiltersComponent,
    IntentsSearchComponent,
    IntentCreateComponent,
    IntentEditComponent,
    ActionEditComponent,
    ContextCreateComponent,
    SentenceEditComponent,
    ModeStepperComponent,
    ScenarioConceptionComponent,
    ScenarioProductionComponent,
    ScenarioPublishingComponent,
    ScenarioStateGroupComponent,
    ScenarioProductionStateGroupAddComponent,
    ScenarioTransitionComponent,
    ScenarioImportComponent,
    ScenarioExportComponent,
    ContextsGraphComponent,
    ScenarioCanvasWrapperComponent,
    TriggerCreateComponent,
    ScenariosSettingsComponent
  ],
  exports: [],
  providers: [
    ScenarioService,
    ScenarioApiService,
    ScenarioSettingsService,
    ScenarioDesignerNavigationGuard,
    ScenariosResolver,
    NlpService,
    ScenarioDesignerService
  ],
  entryComponents: []
})
export class ScenariosModule {
  constructor() {}
}
