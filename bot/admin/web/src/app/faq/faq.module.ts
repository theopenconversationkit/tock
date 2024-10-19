import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  NbAlertModule,
  NbAutocompleteModule,
  NbBadgeModule,
  NbButtonGroupModule,
  NbButtonModule,
  NbCardModule,
  NbChatModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRadioModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTabsetModule,
  NbTagModule,
  NbToggleModule,
  NbTooltipModule
} from '@nebular/theme';

import { FaqManagementComponent } from './faq-management/faq-management.component';
import { FaqRoutingModule } from './faq-routing.module';
import { MomentModule } from 'ngx-moment';
import { FaqManagementFiltersComponent } from './faq-management/faq-management-filters/faq-management-filters.component';
import { FaqManagementListComponent } from './faq-management/faq-management-list/faq-management-list.component';
import { FaqManagementEditComponent } from './faq-management/faq-management-edit/faq-management-edit.component';
import { BotSharedModule } from '../shared/bot-shared.module';
import { FaqManagementSettingsComponent } from './faq-management/faq-management-settings/faq-management-settings.component';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { FaqService } from './services/faq.service';
import { BotAnalyticsModule } from '../analytics/analytics.module';
import { FaqTabsComponent } from './faq-tabs.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    BotSharedModule,
    FaqRoutingModule,
    MomentModule,
    ReactiveFormsModule,
    NbAutocompleteModule,
    NbBadgeModule,
    NbButtonModule,
    NbButtonGroupModule,
    NbCardModule,
    NbCheckboxModule,
    NbFormFieldModule,
    NbIconModule,
    NbInputModule,
    NbSelectModule,
    NbSpinnerModule,
    NbTagModule,
    NbTabsetModule,
    NbTooltipModule,
    NbAlertModule,
    NbChatModule,
    InfiniteScrollModule,
    BotAnalyticsModule,
    NbRouteTabsetModule,
    NbToggleModule,
    NbRadioModule
  ],
  declarations: [
    FaqManagementComponent,
    FaqManagementFiltersComponent,
    FaqManagementListComponent,
    FaqManagementEditComponent,
    FaqManagementSettingsComponent,
    FaqTabsComponent
  ],
  exports: [],
  providers: [FaqService]
})
export class FaqModule {}
