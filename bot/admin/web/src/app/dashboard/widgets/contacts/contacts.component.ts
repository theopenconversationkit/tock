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
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { StateService } from '../../../core-nlp/state.service';
import { UserRole } from '../../../model/auth';
import { BotContact, WidgetState } from '../../models/dashboard.model';

@Component({
  selector: 'tock-bot-contacts',
  templateUrl: './contacts.component.html',
  styleUrls: ['./contacts.component.scss'],
  standalone: false
})
export class ContactsComponent {
  @Input() contacts: BotContact[] = [];
  @Input() state: WidgetState = WidgetState.loading;

  @Output() onAdd = new EventEmitter<void>();
  @Output() onEdit = new EventEmitter<BotContact>();

  WidgetState = WidgetState;

  constructor(public state$: StateService) {}

  get canEdit(): boolean {
    return this.state$.hasRole(UserRole.admin);
  }
}
