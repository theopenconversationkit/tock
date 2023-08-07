import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import {
  NbAlertModule,
  NbAutocompleteModule,
  NbBadgeModule,
  NbButtonModule,
  NbCardModule,
  NbChatModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTabsetModule,
  NbTagModule,
  NbTooltipModule
} from '@nebular/theme';
import { NlpModule } from '../nlp-tabs/nlp.module';

import { FaqManagementComponent } from './faq-management/faq-management.component';
import { FaqRoutingModule } from './faq-routing.module';
import { FaqTrainingComponent } from './faq-training/faq-training.component';
import { SharedModule } from '../shared-nlp/shared.module';
import { MomentModule } from 'ngx-moment';
import { FaqManagementFiltersComponent } from './faq-management/faq-management-filters/faq-management-filters.component';
import { FaqManagementListComponent } from './faq-management/faq-management-list/faq-management-list.component';
import { FaqManagementEditComponent } from './faq-management/faq-management-edit/faq-management-edit.component';
import { BotSharedModule } from '../shared/bot-shared.module';
import { FaqManagementSettingsComponent } from './faq-management/faq-management-settings/faq-management-settings.component';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { FaqService } from './services/faq.service';
import { BotAnalyticsModule } from '../analytics/analytics.module';
import { FaqTabsComponent } from './faq-tabs/faq-tabs.component';

@NgModule({
  imports: [
    BotSharedModule,
    CommonModule,
    FaqRoutingModule,
    NlpModule,
    MomentModule,
    SharedModule,
    ReactiveFormsModule,
    NbAutocompleteModule,
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
    NbTabsetModule,
    NbTooltipModule,
    NbAlertModule,
    NbChatModule,
    InfiniteScrollModule,
    BotAnalyticsModule,
    NbRouteTabsetModule
  ],
  declarations: [
    FaqManagementComponent,
    FaqTrainingComponent,
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
