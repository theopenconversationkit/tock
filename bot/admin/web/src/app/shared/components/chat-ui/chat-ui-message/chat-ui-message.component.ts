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

import { Component, EventEmitter, HostBinding, Input, OnInit, Output } from '@angular/core';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';
import { BotMessage } from '../../../model/dialog-data';
import { BotApplicationConfiguration } from '../../../../core/model/configuration';
import { BotConfigurationService } from '../../../../core/bot-configuration.service';
import { take } from 'rxjs';
import { copyToClipboard } from '../../../utils';

@Component({
  selector: 'tock-chat-ui-message',
  templateUrl: './chat-ui-message.component.html',
  styleUrls: ['./chat-ui-message.component.scss']
})
export class ChatUiMessageComponent implements OnInit {
  @Input() message: BotMessage;
  @Input() replay: boolean = false;
  @Input() sender?: string;
  @Input() date?: Date;
  @Input() applicationId?: string;
  @Input() switchFormattingPos: 'afterSender' | 'afterAvatar' = 'afterSender';

  @Input()
  set avatar(value: string) {
    this.avatarStyle = value ? this.domSanitizer.bypassSecurityTrustStyle(`url(${value})`) : null;
  }

  allConfigurations: BotApplicationConfiguration[];

  showCopyButton: boolean = false;

  isMessageEmpty() {
    //@ts-ignore
    return !this.message.text?.trim().length && !this.message.messages?.length;
  }

  avatarStyle: SafeStyle;

  @HostBinding('class.not-reply')
  get notReply() {
    return !this.reply;
  }

  @Input()
  @HostBinding('class.reply')
  get reply(): boolean {
    return this._reply;
  }
  set reply(value: boolean) {
    this._reply = value;
  }
  protected _reply: boolean = false;

  formatting: boolean = true;

  @Output() sendMessage: EventEmitter<BotMessage> = new EventEmitter();

  constructor(protected domSanitizer: DomSanitizer, private botConfiguration: BotConfigurationService) {}

  ngOnInit() {
    this.botConfiguration.configurations.pipe(take(1)).subscribe((conf) => {
      this.allConfigurations = conf;
    });
  }

  getApplicationConfigurationName(short: boolean = true) {
    if (!this.allConfigurations || !this.applicationId) return;
    const configuration = this.allConfigurations.find((conf) => conf.applicationId === this.applicationId);
    if (configuration) {
      if (short) {
        return `${configuration.applicationId}`;
      } else {
        return `${configuration.name} > ${configuration.connectorType.label()} (${configuration.applicationId})`;
      }
    }

    return '';
  }

  replyMessage(message: BotMessage) {
    this.sendMessage.emit(message);
  }

  switchFormatting() {
    this.formatting = !this.formatting;
  }

  canBeCopied() {
    return (this.message as any).text;
  }

  copyToClipboard() {
    copyToClipboard((this.message as any).text);
  }
}
