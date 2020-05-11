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

import {Component, OnInit} from "@angular/core";
import {BotService} from "../bot-service";
import {StateService} from "../../core-nlp/state.service";
import {StoryDefinitionConfiguration, StoryFeature, StorySearchQuery} from "../model/story";
import {flatMap} from "../../model/commons";
import {BotConfigurationService} from "../../core/bot-configuration.service";

@Component({
  selector: 'tock-story-rule',
  templateUrl: './story-rule.component.html',
  styleUrls: ['./story-rule.component.css']
})
export class StoryRuleComponent implements OnInit {

  private currentApplicationUnsuscriber: any;
  create: boolean = false;
  loadingStoryRules: boolean = false;
  stories: StoryDefinitionConfiguration[] = [];
//   features: StoryFeature[] = [];
  disabledFeatures: StoryFeature[] = [];
  redirectedFeatures: StoryFeature[] = [];
  feature: StoryFeature;

  constructor(private state: StateService,
              private botService: BotService,
              private configurationService: BotConfigurationService) {
  }

  ngOnInit(): void {
    this.initNewFeature();
    this.currentApplicationUnsuscriber = this.state.currentApplicationEmitter.subscribe(a => this.refresh());
    this.refresh();
  }

  prepareCreate() {
    this.create = true;
  }

  cancelCreate() {
    this.initNewFeature();
    this.create = false;
  }

  toggleNew(newState: boolean) {
    this.feature.enabled = newState;
  }

  private initNewFeature() {
    this.feature = new StoryFeature(null, true, null);
    if (this.stories.length !== 0) {
      this.feature.story = this.stories[0];
    }
  }

  onSwitchStoryChange() {
    this.feature.switchToStory = this.feature.switchToStoryId
      ? this.stories.find(st => st.storyId == this.feature.switchToStoryId)
      : null;
  }

  addFeature() {
    const f = this.feature;
    const s = f.story;
    s.features.push(f);
    this.botService.saveStory(s).subscribe(_ => {
      f.conf = f.botApplicationConfigurationId ?
        this.configurationService.findApplicationConfigurationById(f.botApplicationConfigurationId)
        : null;
      f.switchToStory = f.switchToStoryId ? this.stories.find(st => st.storyId == f.switchToStoryId) : null;
      if (f.switchToStoryId) {
        this.redirectedFeatures.push(f);
      } else {
        this.disabledFeatures.push(f);
      }

      this.cancelCreate();
    });
  }

  refresh() {
    if (this.state.currentApplication) {
      this.loadingStoryRules = true;
      this.botService.getStories(
        new StorySearchQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          0,
          10000
        )).subscribe(s => {
        this.stories = s;
        if (s.length !== 0) {
          this.feature.story = s[0];
        }
        var features = flatMap(s, story => {
          story.features.forEach(f => {
            f.story = story;
            f.conf = f.botApplicationConfigurationId ?
              this.configurationService.findApplicationConfigurationById(f.botApplicationConfigurationId)
              : null;
            f.switchToStory = f.switchToStoryId ? this.stories.find(st => st.storyId == f.switchToStoryId) : null;
          });
          return story.features;
        });
        this.disabledFeatures = features.filter(feat => feat.switchToStoryId == null);
        this.redirectedFeatures = features.filter(feat => feat.switchToStoryId != null);
        this.loadingStoryRules = false;
      });
    }
  }

  toggle(f: StoryFeature, newState) {
    f.enabled = newState;
    this.botService.saveStory(f.story).subscribe();
  }

  deleteFeature(f: StoryFeature) {
    if (f.switchToStoryId) {
      this.redirectedFeatures.splice(this.redirectedFeatures.indexOf(f), 1); // TODO
    } else {
      this.disabledFeatures.splice(this.disabledFeatures.indexOf(f), 1); // TODO
    }
    const s = f.story;
    s.features.splice(s.features.indexOf(f), 1);
    this.botService.saveStory(s).subscribe();
  }

}
