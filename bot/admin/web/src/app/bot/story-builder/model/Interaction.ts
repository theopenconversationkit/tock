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


import {InteractionEntry} from './InteractionEntry';

export class Interaction {
  id: string;
  intent: string;
  label: string;
  entries: InteractionEntry[];

  constructor(id, intent, label, entries) {
    this.id = id;
    this.intent = intent;
    this.label = label;
    this.entries = entries;
  }

  public getActions(): string[] {
    return this.entries[0].interactions.map(item => item.label);
  }

  public getLabel(): string {
    return this.entries[0].content[0].text;
  }
}
