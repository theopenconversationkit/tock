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

import {Component, OnInit} from "@angular/core";
import {BotService} from "../bot-service";
import {MatSnackBar} from "@angular/material";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {StateService} from "../../core-nlp/state.service";
import {StoryDefinitionConfiguration, StorySearchQuery} from "../model/story";

@Component({
  selector: 'tock-search-story',
  templateUrl: './search-story.component.html',
  styleUrls: ['./search-story.component.css']
})
export class SearchStoryComponent implements OnInit {

  stories: StoryDefinitionConfiguration[];

  constructor(private nlp: NlpService,
              private state: StateService,
              private bot: BotService,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.bot.getStories(
      new StorySearchQuery(
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.state.currentLocale,
        0,
        1000
      )).subscribe(s => {
      this.stories = s;
    })
  }

  delete(storyDefinitionId: string) {
    this.stories = this.stories.filter(s => s._id !== storyDefinitionId);
  }
}
