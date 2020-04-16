/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {BotMessageComponent} from "./bot-message/bot-message.component";
import {SentenceElementComponent} from "./bot-message/sentence-element.component";
import {BotMessageSentenceComponent} from "./bot-message/bot-message-sentence";
import {BotMessageChoiceComponent} from "./bot-message/bot-message-choice.component";
import {BotMessageLocationComponent} from "./bot-message/bot-message-location";
import {BotMessageAttachmentComponent} from "./bot-message/bot-message-attachment";
import {SharedModule} from "../shared-nlp/shared.module";
import {BotSharedService} from "./bot-shared.service";
import {DisplayDialogComponent} from "./bot-dialog/display-dialog.component";
import {MomentModule} from "ngx-moment";
import {SelectBotComponent} from "./select-bot/select-bot.component";
import {NbCardModule, NbSelectModule, NbTooltipModule} from "@nebular/theme";
@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    MomentModule,
    NbCardModule,
    NbSelectModule,
    NbTooltipModule
  ],
  declarations: [
    BotMessageComponent,
    SentenceElementComponent,
    BotMessageSentenceComponent,
    BotMessageChoiceComponent,
    BotMessageLocationComponent,
    BotMessageAttachmentComponent,
    DisplayDialogComponent,
    SelectBotComponent
  ],
  exports: [BotMessageComponent, DisplayDialogComponent, SelectBotComponent],
  providers: [BotSharedService],
  entryComponents: []
})
export class BotSharedModule {
}
