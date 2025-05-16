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
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: '/language-understanding/inbox', pathMatch: 'full' },
  {
    path: 'language-understanding',
    loadChildren: () => import('./language-understanding/language-understanding.module').then((m) => m.LanguageUnderstandingModule)
  },
  {
    path: 'model-quality',
    loadChildren: () => import('./model-quality/model-quality.module').then((m) => m.ModelQualityModule)
  },
  {
    path: 'configuration',
    loadChildren: () => import('./configuration/configuration.module').then((m) => m.BotConfigurationModule)
  },
  {
    path: 'build',
    loadChildren: () => import('./bot/bot.module').then((m) => m.BotModule)
  },
  {
    path: 'test',
    loadChildren: () => import('./test/test.module').then((m) => m.BotTestModule)
  },
  {
    path: 'analytics',
    loadChildren: () => import('./analytics/analytics.module').then((m) => m.BotAnalyticsModule)
  },
  {
    path: 'business-metrics',
    loadChildren: () => import('./metrics/metrics.module').then((m) => m.MetricsModule)
  },
  {
    path: 'faq',
    loadChildren: () => import('./faq/faq.module').then((m) => m.FaqModule)
  },
  {
    path: 'rag',
    loadChildren: () => import('./rag/rag.module').then((m) => m.RagModule)
  },
  {
    path: 'playground',
    loadChildren: () => import('./playground/playground.module').then((m) => m.PlaygroundModule)
  },
  { path: '**', redirectTo: '/language-understanding/inbox' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {})],
  exports: [RouterModule],
  declarations: []
})
export class BotAdminAppRoutingModule {}
