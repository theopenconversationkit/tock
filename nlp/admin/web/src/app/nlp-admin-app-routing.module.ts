import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: '/nlp/inbox', pathMatch: 'full' },
  {
    path: 'nlp',
    loadChildren: () => import('./nlp-tabs/nlp.module').then((m) => m.NlpModule)
  },
  {
    path: 'applications',
    loadChildren: () => import('./applications/applications.module').then((m) => m.ApplicationsModule)
  },
  {
    path: 'quality',
    loadChildren: () => import('./quality-nlp/quality.module').then((m) => m.QualityModule)
  },
  { path: '**', redirectTo: '/nlp/inbox' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class NlpAdminAppRoutingModule {}
