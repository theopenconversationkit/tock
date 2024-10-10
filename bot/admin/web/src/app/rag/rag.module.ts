import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RagTabsComponent } from './rag-tabs.component';
import {
  NbAccordionModule,
  NbAlertModule,
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRadioModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbToggleModule,
  NbTooltipModule
} from '@nebular/theme';
import { RagSettingsComponent } from './rag-settings/rag-settings.component';
import { BotSharedModule } from '../shared/bot-shared.module';
import { RagRoutingModule } from './rag-routing.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RagExcludedComponent } from './rag-excluded/rag-excluded.component';
import { RagSourcesBoardComponent } from './rag-sources/rag-sources-board.component';
import { NewSourceComponent } from './rag-sources/new-source/new-source.component';
import { SourceEntryComponent } from './rag-sources/source-entry/source-entry.component';
import { SourceImportComponent } from './rag-sources/source-import/source-import.component';
import { SourceNormalizationCsvComponent } from './rag-sources/source-normalization/csv/source-normalization-csv.component';
import { SourceNormalizationJsonComponent } from './rag-sources/source-normalization/json/source-normalization-json.component';
import { JsonIteratorComponent } from './rag-sources/source-normalization/json/json-iterator/json-iterator.component';
import { SourceManagementService } from './rag-sources/source-management.service';
import { SourceManagementApiService } from './rag-sources/source-management.api.service';

@NgModule({
  imports: [
    CommonModule,
    BotSharedModule,
    RagRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    NbRouteTabsetModule,
    NbSelectModule,
    NbToggleModule,
    NbRadioModule,
    NbSpinnerModule,
    NbCardModule,
    NbButtonModule,
    NbInputModule,
    NbIconModule,
    NbAccordionModule,
    NbTooltipModule,
    NbCheckboxModule,
    NbAlertModule,
    NbAutocompleteModule,
    NbFormFieldModule
  ],
  declarations: [
    RagTabsComponent,
    RagSettingsComponent,
    RagExcludedComponent,
    RagSourcesBoardComponent,
    NewSourceComponent,
    SourceEntryComponent,
    SourceImportComponent,
    SourceNormalizationCsvComponent,
    SourceNormalizationJsonComponent,
    JsonIteratorComponent
  ],
  providers: [SourceManagementService, SourceManagementApiService]
})
export class RagModule {}
