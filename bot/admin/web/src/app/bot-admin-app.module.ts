import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {CoreModule} from "tock-nlp-admin/src/app/core/core.module";
import {SharedModule} from "tock-nlp-admin/src/app/shared/shared.module";
import {RouterModule, Routes} from "@angular/router";
import {BotAdminAppComponent} from "./bot-admin-app.component";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BotCoreModule} from "./core/bot-core.module";

const routes: Routes = [
  {path: '', redirectTo: '/nlp/inbox', pathMatch: 'full'},
  {
    path: 'nlp',
    loadChildren: 'app/nlp/nlp.module#BotNlpModule'
  },
  {
    path: 'configuration',
    loadChildren: 'app/configuration/configuration.module#BotConfigurationModule'
  },
  {
    path: 'test',
    loadChildren: 'app/test/test.module#BotTestModule'
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
    BotCoreModule,
    SharedModule,
    BotAdminAppRoutingModule
  ],
  providers: [],
  bootstrap: [BotAdminAppComponent]
})
export class BotAdminAppModule {
}
