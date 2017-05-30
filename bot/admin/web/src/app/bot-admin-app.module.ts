import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {CoreModule} from "tock-nlp-admin/src/app/core/core.module";
import {SharedModule} from "tock-nlp-admin/src/app/shared/shared.module";
import {RouterModule, Routes} from "@angular/router";
import {BotAdminAppComponent} from "./bot-admin-app.component";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

const routes: Routes = [
  {path: '', redirectTo: '/nlp/inbox', pathMatch: 'full'},
  {
    path: 'nlp',
    loadChildren: 'app/nlp/nlp.module#BotNlpModule'
  },
  {
    path: 'applications',
    loadChildren: 'app/admin/admin.module#BotAdminModule'
  },
  {
    path: 'monitoring',
    loadChildren: 'app/monitoring/monitoring.module#BotMonitoringModule'
  }
];
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class BotAdminAppRoutingModule {
}

@NgModule({
  declarations: [BotAdminAppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CoreModule,
    SharedModule,
    BotAdminAppRoutingModule
  ],
  providers: [],
  bootstrap: [BotAdminAppComponent]
})
export class BotAdminAppModule {
}
