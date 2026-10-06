import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule, provideTranslocoScope } from '@jsverse/transloco';
import {
  NbAccordionModule,
  NbAlertModule,
  NbBadgeModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTagModule,
  NbToggleModule,
  NbTooltipModule
} from '@nebular/theme';

import { BotSharedModule } from '../shared/bot-shared.module';
import { DiagnosticComponent } from './diagnostic/diagnostic.component';
import { ExplorationComponent } from './exploration/exploration.component';
import { VectorStoreInspectionRoutingModule } from './vector-store-inspection-routing.module';
import { VectorStoreInspectionRestService } from './services/vector-store-inspection-rest.service';
import { VectorStoreInspectionStateService } from './services/vector-store-inspection-state.service';
import { VectorStoreInspectionService } from './services/vector-store-inspection.service';
import { VectorStoreInspectionTabsComponent } from './vector-store-inspection-tabs.component';
import { DocumentEntryComponent } from './exploration/document-entry/document-entry.component';
import { ResultEntryComponent } from './diagnostic/result-entry/result-entry.component';
import { RunComparisonComponent } from './diagnostic/run-comparison/run-comparison.component';
import { MetadataViewComponent } from './utils/metadata-view/metadata-view.component';
import { IndexSelectorComponent } from './utils/index-selector/index-selector.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BotSharedModule,
    NbRouteTabsetModule,
    VectorStoreInspectionRoutingModule,
    TranslocoModule,
    NbAccordionModule,
    NbAlertModule,
    NbBadgeModule,
    NbButtonModule,
    NbCardModule,
    NbCheckboxModule,
    NbFormFieldModule,
    NbIconModule,
    NbInputModule,
    NbSelectModule,
    NbSpinnerModule,
    NbTagModule,
    NbToggleModule,
    NbTooltipModule
  ],
  declarations: [
    VectorStoreInspectionTabsComponent,
    ExplorationComponent,
    DocumentEntryComponent,
    DiagnosticComponent,
    ResultEntryComponent,
    RunComparisonComponent,
    MetadataViewComponent,
    IndexSelectorComponent
  ],
  providers: [
    provideTranslocoScope({ scope: 'vector-store-inspection', alias: 'vsi' }),
    { provide: VectorStoreInspectionService, useClass: VectorStoreInspectionRestService },
    VectorStoreInspectionStateService
  ]
})
export class VectorStoreInspectionModule {}
