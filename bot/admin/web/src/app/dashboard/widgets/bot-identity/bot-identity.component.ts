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
import { Component, EventEmitter, inject, Input, Output } from '@angular/core';

import { StateService } from '../../../core-nlp/state.service';
import { UserRole } from '../../../model/auth';
import { BotIdentity, WidgetState } from '../../models/dashboard.model';

@Component({
  selector: 'tock-bot-identity',
  templateUrl: './bot-identity.component.html',
  styleUrls: ['./bot-identity.component.scss'],
  standalone: false
})
export class BotIdentityComponent {
  @Input() identity: BotIdentity;
  @Input() state: WidgetState = WidgetState.loading;

  @Output() onEdit = new EventEmitter<void>();

  readonly stateService = inject(StateService);

  WidgetState = WidgetState;

  get canEdit(): boolean {
    return this.stateService.hasRole(UserRole.admin);
  }

  /** The application name is the bot id on the backend side. */
  get technicalName(): string {
    return this.stateService.currentApplication?.name;
  }

  get hasNotes(): boolean {
    return !!this.identity?.notes?.trim().length;
  }
}
