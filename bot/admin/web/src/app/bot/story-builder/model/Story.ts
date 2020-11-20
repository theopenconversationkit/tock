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


import {UserInteraction} from './UserInteraction';
import {InteractionDialog} from './InteractionDialog';

export class Story {
  name: string;
  rootUserInteraction: UserInteraction;

  constructor(name: string) {
    this.name = name;
  }

  setRootUserInteraction: (rootUserInteraction: UserInteraction) => void = (rootUserInteraction: UserInteraction) => {
    this.rootUserInteraction = rootUserInteraction;
  }

  getIntent: () => string = () => {
    if (!this.rootUserInteraction) {
      return 'unknown';
    }
    return this.rootUserInteraction.intent;
  }

  getSelectedInteractionDialogs: () => InteractionDialog[] = () => {
    const dialogs: InteractionDialog[] = [];
    if (this.rootUserInteraction) {
      let userInteraction = this.rootUserInteraction;
      let botInteraction = userInteraction.botInteraction;
      dialogs.push(new InteractionDialog(userInteraction, botInteraction));
      while (botInteraction.hasSelectedUserInteraction()) {
        userInteraction = botInteraction.getSelectedUserInteraction();
        botInteraction = userInteraction.botInteraction;
        dialogs.push(new InteractionDialog(userInteraction, botInteraction));
      }
    }
    return dialogs;
  }
}
