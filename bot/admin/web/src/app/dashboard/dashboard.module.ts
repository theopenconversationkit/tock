/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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
  NbAlertModule,
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbSpinnerModule,
  NbTooltipModule
} from '@nebular/theme';
import { provideTranslocoScope, TranslocoModule } from '@jsverse/transloco';
import { NgxEchartsModule } from 'ngx-echarts';

import { BotSharedModule } from '../shared/bot-shared.module';
import { DashboardRoutingModule } from './dashboard-routing.module';
import { DashboardTabsComponent } from './dashboard-tabs.component';
import { DashboardComponent } from './dashboard.component';
import { DashboardService } from './services/dashboard.service';
import { DashboardStateService } from './services/dashboard-state.service';
import { DashboardWidgetComponent } from './widgets/dashboard-widget/dashboard-widget.component';
import { DashboardDeltaComponent } from './widgets/dashboard-delta/dashboard-delta.component';
import { QuestionsAnsweredComponent } from './widgets/questions-answered/questions-answered.component';
import { UserFeedbackComponent } from './widgets/user-feedback/user-feedback.component';
import { AnswerOutcomeComponent } from './widgets/answer-outcome/answer-outcome.component';
import { TopicsAnsweredComponent } from './widgets/topics-answered/topics-answered.component';
import { KnowledgeIndexComponent } from './widgets/knowledge-index/knowledge-index.component';
import { ContactsComponent } from './widgets/contacts/contacts.component';
import { LastEvaluationComponent } from './widgets/last-evaluation/last-evaluation.component';
import { GenAiConfigurationComponent } from './widgets/genai-configuration/genai-configuration.component';
import { ContactEditComponent } from './modals/contact-edit/contact-edit.component';
import { IngestionNotesComponent } from './modals/ingestion-notes/ingestion-notes.component';
import { BotIdentityComponent } from './widgets/bot-identity/bot-identity.component';
import { BotHistoryComponent } from './widgets/bot-history/bot-history.component';
import { BotIdentityEditComponent } from './modals/bot-identity-edit/bot-identity-edit.component';

@NgModule({
  declarations: [
    DashboardTabsComponent,
    DashboardComponent,
    DashboardWidgetComponent,
    DashboardDeltaComponent,
    QuestionsAnsweredComponent,
    UserFeedbackComponent,
    AnswerOutcomeComponent,
    TopicsAnsweredComponent,
    KnowledgeIndexComponent,
    ContactsComponent,
    BotIdentityComponent,
    BotHistoryComponent,
    LastEvaluationComponent,
    GenAiConfigurationComponent,
    ContactEditComponent,
    BotIdentityEditComponent,
    IngestionNotesComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BotSharedModule,
    NbRouteTabsetModule,
    NbCardModule,
    NbCheckboxModule,
    NbIconModule,
    NbButtonModule,
    NbInputModule,
    NbFormFieldModule,
    NbAutocompleteModule,
    NbTooltipModule,
    NbSpinnerModule,
    NbAlertModule,
    TranslocoModule,
    NgxEchartsModule.forRoot({
      echarts: () => import('echarts')
    }),
    DashboardRoutingModule
  ],
  providers: [DashboardService, DashboardStateService, provideTranslocoScope({ scope: 'dashboard', alias: 'dashboard' })]
})
export class DashboardModule {}
