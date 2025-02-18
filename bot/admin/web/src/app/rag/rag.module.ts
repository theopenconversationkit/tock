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
  declarations: [RagTabsComponent, RagSettingsComponent, RagExcludedComponent],
  providers: []
})
export class RagModule {}
