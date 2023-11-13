import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LogsComponent } from '../logs/logs.component';
import { SearchComponent } from '../search/search.component';
import { EntitiesComponent } from '../entities/entities.component';
import { IntentsComponent } from '../intents/intents.component';
import { ArchiveComponent } from '../archive/archive.component';
import { InboxComponent } from '../inbox/inbox.component';
import { TryComponent } from '../try/try.component';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { NlpTabsComponent } from './nlp-tabs.component';
import { AuthGuard } from '../core-nlp/auth/auth.guard';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: NlpTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        redirectTo: 'inbox',
        pathMatch: 'full'
      },
      {
        path: 'try',
        component: TryComponent
      },
      {
        path: 'inbox',
        component: InboxComponent
      },
      {
        path: 'unknown',
        component: ArchiveComponent
      },
      {
        path: 'intents',
        component: IntentsComponent
      },
      {
        path: 'entities',
        component: EntitiesComponent
      },
      {
        path: 'search',
        component: SearchComponent
      },
      {
        path: 'logs',
        component: LogsComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NlpRoutingModule {}
