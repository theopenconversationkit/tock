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


import {Component, Input, OnInit} from '@angular/core';
import {Story} from './model/Story';
import {UserInteraction} from './model/UserInteraction';
import {BotInteraction} from './model/BotInteraction';
import {InteractionEntry} from './model/InteractionEntry';
import {InteractionDialog} from './model/InteractionDialog';

@Component({
  selector: 'tock-story-builder',
  templateUrl: './story-builder.component.html',
  styleUrls: ['./story-builder.component.css']
})
export class StoryBuilderComponent implements OnInit {

  @Input()
  story: Story;

  currentPath: InteractionDialog[];

  intents: string[] = ['selfcare_notrain_found', 'yes', 'no'];

  ngOnInit(): void {
    this.story = new Story('new user story');
    this.currentPath = this.story.getSelectedInteractionDialogs();
  }

  addUserSentence = (botInteraction: BotInteraction, sentence: string) => {
    botInteraction.userInteractions.forEach(value => value.selected = false);
    botInteraction.userInteractions.push(new UserInteraction(sentence, 'sentence'));
  }

  addUserAction = (botInteraction: BotInteraction, sentence: string) => {
    botInteraction.userInteractions.forEach(value => value.selected = false);
    botInteraction.userInteractions.push(new UserInteraction(sentence, 'action'));
  }

  addBotResponse = (userInteraction: UserInteraction, sentence) => {
    userInteraction.botInteraction.entries.push(new InteractionEntry(sentence));
  }

  save() {
    // TODO: transform to server model and save
    console.log('Saved the story : ' + this.story);
  }

  addFirstInteraction = (sentence: string) => {
    const userInteraction = new UserInteraction(sentence, 'sentence');
    this.story.setRootUserInteraction(userInteraction);
    this.currentPath = this.story.getSelectedInteractionDialogs();
  }
}
