/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {NgModule} from "@angular/core";
import {AppComponent} from "./app.component";
import {SharedModule} from "./shared/shared.module";
import {Routes, RouterModule} from "@angular/router";
import {BrowserModule} from "@angular/platform-browser";
import {CoreModule} from "./core/core.module";
import { SentencesScrollComponent } from './sentences-scroll/sentences-scroll.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

const routes: Routes = [
  {path: '', redirectTo: '/nlp/inbox', pathMatch: 'full'},
  {
    path: 'nlp',
    loadChildren: 'app/nlp-tabs/nlp.module#NlpModule'
  },
  {
    path: 'applications',
    loadChildren: 'app/applications/applications.module#ApplicationsModule'
  }
];
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CoreModule,
    SharedModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
