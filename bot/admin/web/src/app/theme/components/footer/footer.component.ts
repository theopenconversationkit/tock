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

import { Component, OnInit } from '@angular/core';
import { tock_info } from '../../../../environments/manifest';
import { BotSharedService } from '../../../shared/bot-shared.service';
import { AdminConfiguration } from '../../../shared/model/conf';
import { take } from 'rxjs';

@Component({
  selector: 'tock-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {
  constructor(private botSharedService: BotSharedService) {}

  tock_info = tock_info;

  globalMessage = '';

  ngOnInit() {
    this.botSharedService
      .getConfiguration()
      .pipe(take(1))
      .subscribe((conf: AdminConfiguration) => {
        if (conf.globalMessage) {
          this.globalMessage = conf.globalMessage;
        }
      });
  }
}
