import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NamespacesComponent } from '../applications/namespace/namespaces.component';
import { UserLogsComponent } from '../applications/user/user-logs.component';
import { ApplicationsResolver } from '../applications/applications.resolver';
import { NewBotComponent } from './bot/new-bot.component';
import { ApplicationComponent } from '../applications/application/application.component';
import { ApplicationsComponent } from '../applications/applications/applications.component';
import { BotConfigurationsComponent } from './bot/bot-configurations.component';
import { ConfigurationTabsComponent } from './configuration-tabs.component';
import { AuthGuard } from '../core-nlp/auth/auth.guard';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: ConfigurationTabsComponent,
    resolve: {
      application: ApplicationsResolver
    },
    children: [
      {
        path: '',
        component: ApplicationsComponent,
        resolve: {
          application: ApplicationsResolver
        }
      },
      {
        path: 'bot',
        component: BotConfigurationsComponent,
        resolve: {
          application: ApplicationsResolver
        }
      },
      {
        path: 'create',
        component: ApplicationComponent
      },
      {
        path: 'nlp',
        children: [
          {
            path: '',
            component: ApplicationsComponent
          },
          {
            path: 'edit/:id',
            component: ApplicationComponent
          },
          {
            path: 'create',
            component: ApplicationComponent
          }
        ],
        resolve: {
          application: ApplicationsResolver
        }
      },
      {
        path: 'new',
        component: NewBotComponent,
        resolve: {
          application: ApplicationsResolver
        }
      },
      {
        path: 'users/logs',
        component: UserLogsComponent
      },
      {
        path: 'namespaces',
        component: NamespacesComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BotConfigurationRoutingModule {}
