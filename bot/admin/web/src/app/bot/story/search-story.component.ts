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
import {NlpService} from "../../nlp-tabs/nlp.service";
import {StateService} from "../../core-nlp/state.service";
import {StoryDefinitionConfiguration, StorySearchQuery} from "../model/story";

@Component({
  selector: 'tock-search-story',
  templateUrl: './search-story.component.html',
  styleUrls: ['./search-story.component.css']
})
export class SearchStoryComponent implements OnInit {

  loadedStories: StoryDefinitionConfiguration[];
  stories: StoryDefinitionConfiguration[];
  categories: string[] = [];

  filter: string = "";
  category: string = "";

  constructor(private nlp: NlpService,
              private state: StateService,
              private bot: BotService) {
  }

  ngOnInit(): void {
    this.bot.getStories(
      new StorySearchQuery(
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.state.currentLocale,
        0,
        10000
      )).subscribe(s => {
      this.stories = s;
      this.loadedStories = s;
      s.forEach(story => {
        story.hideDetails = true;
        if (this.categories.indexOf(story.category) === -1) {
          this.categories.push(story.category);
        }
      });
      this.categories.sort();
    })
  }

  delete(storyDefinitionId: string) {
    this.stories = this.stories.filter(s => s._id !== storyDefinitionId);
    this.loadedStories = this.loadedStories.filter(s => s._id !== storyDefinitionId);
  }

  search(story?:StoryDefinitionConfiguration) {
    if(story && this.categories.indexOf(story.category) === -1) {
      this.categories.push(story.category);
      this.categories.sort();
    }
    this.stories = this.loadedStories.filter(s =>
      (s.name.toLowerCase().indexOf(this.filter.toLowerCase()) !== -1
        || s.intent.name.toLowerCase().indexOf(this.filter.toLowerCase()) !== -1)
      && (this.category.length === 0 || this.category === s.category)
    );
  }
}
