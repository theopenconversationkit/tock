import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: '/nlp/inbox', pathMatch: 'full' },
  {
    path: 'nlp',
    loadChildren: () => import('./nlp/nlp.module').then((m) => m.BotNlpModule)
  },
  {
    path: 'quality',
    loadChildren: () => import('./quality/quality.module').then((m) => m.BotQualityModule)
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
  { path: '**', redirectTo: '/nlp/inbox' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule],
  declarations: []
})
export class BotAdminAppRoutingModule {}
