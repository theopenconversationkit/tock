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

import {NgModule, Optional, SkipSelf} from "@angular/core";
import {AuthModule} from "./auth/auth.module";
import {SettingsService} from "./settings.service";
import {CommonModule} from "@angular/common";
import {RestModule} from "./rest/rest.module";
import {StateService} from "./state.service";
import {ApplicationService} from "./applications.service";
@NgModule({
  imports: [CommonModule, RestModule, AuthModule],
  declarations: [],
  exports: [],
  providers: [StateService, SettingsService, ApplicationService]
})
export class CoreModule {

  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error(
        'CoreModule is already loaded. Import it in the AppModule only');
    }
  }

}
