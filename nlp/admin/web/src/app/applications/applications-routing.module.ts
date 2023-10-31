import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NamespacesComponent } from './namespace/namespaces.component';
import { UserLogsComponent } from './user/user-logs.component';
import { ApplicationsResolver } from './applications.resolver';
import { ApplicationComponent } from './application/application.component';
import { ApplicationsComponent } from './applications/applications.component';
import { ConfigurationTabsComponent } from './configuration-tabs.component';
import { AuthGuard } from '../core-nlp/auth/auth.guard';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: ConfigurationTabsComponent,
    resolve: {
      applications: ApplicationsResolver
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
        path: 'create',
        component: ApplicationComponent
      },
      {
        path: 'nlu',
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
export class ApplicationsRoutingModule {}
