/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import { Component, OnInit } from '@angular/core';
import { BotService } from '../bot-service';
import { StateService } from '../../core-nlp/state.service';
import { RuleType, ruleTypeValues, StoryDefinitionConfiguration, StoryFeature, StorySearchQuery } from '../model/story';
import { flatMap } from '../../model/commons';
import { BotConfigurationService } from '../../core/bot-configuration.service';

@Component({
  selector: 'tock-story-rule',
  templateUrl: './story-rule.component.html',
  styleUrls: ['./story-rule.component.css']
})
export class StoryRuleComponent implements OnInit {
  private currentApplicationSubscription: any;
  create = false;
  loadingStoryRules = false;
  stories: StoryDefinitionConfiguration[] = [];
  configuredStories: StoryDefinitionConfiguration[] = [];
  storiesToDisplay: StoryDefinitionConfiguration[] = [];
  disabledFeatures: StoryFeature[] = [];
  redirectedFeatures: StoryFeature[] = [];
  endingFeatures: StoryFeature[] = [];
  feature: StoryFeature;
  ruleTypes: RuleType[] = ruleTypeValues();
  selectedRuleType: RuleType = RuleType.Activation;
  rulesPluralMapping = {
    rule: {
      '=0': '0 rules',
      '=1': '1 rule',
      other: '# rules'
    }
  };

  constructor(private state: StateService, private botService: BotService, private configurationService: BotConfigurationService) {}

  ngOnInit(): void {
    this.initNewFeature();
    this.currentApplicationSubscription = this.state.currentApplicationEmitter.subscribe((a) => this.refresh());
    this.refresh();
  }

  updateStoriesToDisplay() {
    this.storiesToDisplay = this.isEndingRule() ? this.configuredStories : this.stories;
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
    this.feature = new StoryFeature(null, true, null, null);
    if (this.stories.length !== 0) {
      this.feature.story = this.stories[0];
    }
  }

  onSwitchStoryChange(switchToStory: StoryDefinitionConfiguration) {
    this.feature.switchToStoryId = switchToStory ? switchToStory.storyId : null;
  }

  onSwitchStoryEnding(endWithStory: StoryDefinitionConfiguration) {
    this.feature.endWithStoryId = endWithStory ? endWithStory.storyId : null;
  }

  addFeature() {
    const newFeature = this.feature;
    const s = newFeature.story;
    s.features.push(newFeature);
    this.botService.saveStory(s).subscribe((_) => {
      newFeature.conf = newFeature.botApplicationConfigurationId
        ? this.configurationService.findApplicationConfigurationById(newFeature.botApplicationConfigurationId)
        : null;
      switch (newFeature.getRuleType()) {
        case RuleType.Redirection:
          newFeature.switchToStory = newFeature.switchToStoryId
            ? this.stories.find((st) => st.storyId === newFeature.switchToStoryId)
            : null;
          this.redirectedFeatures.push(newFeature);
          break;
        case RuleType.Ending:
          newFeature.endWithStory = newFeature.endWithStoryId ? this.stories.find((st) => st.storyId === newFeature.endWithStoryId) : null;
          this.endingFeatures.push(newFeature);
          break;
        case RuleType.Activation:
        default:
          this.disabledFeatures.push(newFeature);
      }
      this.cancelCreate();
    });
  }

  refresh() {
    if (this.state.currentApplication) {
      this.loadingStoryRules = true;
      this.botService
        .getStories(
          new StorySearchQuery(
            this.state.currentApplication.namespace,
            this.state.currentApplication.name,
            this.state.currentLocale,
            0,
            10000
          )
        )
        .subscribe((result) => {
          this.stories = result;
          this.configuredStories = result.filter((story) => story.isConfiguredAnswer());
          this.updateStoriesToDisplay();
          if (result.length !== 0) {
            this.feature.story = result[0];
          }
          const features = flatMap(result, (story) => {
            story.features.forEach((f) => {
              f.story = story;
              f.conf = f.botApplicationConfigurationId
                ? this.configurationService.findApplicationConfigurationById(f.botApplicationConfigurationId)
                : null;
              f.switchToStory = f.switchToStoryId ? result.find((st) => st.storyId === f.switchToStoryId) : null;
              f.endWithStory = f.endWithStoryId ? result.find((st) => st.storyId === f.endWithStoryId) : null;
            });
            return story.features;
          });
          this.disabledFeatures = features.filter((feat) => !feat.switchToStoryId && !feat.endWithStoryId);
          this.redirectedFeatures = features.filter((feat) => feat.switchToStoryId);
          this.endingFeatures = features.filter((feat) => feat.endWithStoryId);
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
      this.redirectedFeatures.splice(this.redirectedFeatures.indexOf(f), 1);
    } else if (f.endWithStoryId) {
      this.endingFeatures.splice(this.endingFeatures.indexOf(f), 1);
    } else {
      this.disabledFeatures.splice(this.disabledFeatures.indexOf(f), 1);
    }
    const s = f.story;
    s.features.splice(s.features.indexOf(f), 1);
    this.botService.saveStory(s).subscribe();
  }

  isRedirectionRule(): boolean {
    return this.selectedRuleType === RuleType.Redirection;
  }

  isEndingRule() {
    return this.selectedRuleType === RuleType.Ending;
  }

  changeTypeTo(type: RuleType) {
    switch (type) {
      case RuleType.Ending: {
        this.feature.switchToStory = null;
        this.feature.switchToStoryId = null;
        this.feature.endWithStory = null;
        this.feature.endWithStoryId = null;
        if (this.configuredStories.length !== 0) {
          this.feature.story = this.configuredStories[0];
        }
        break;
      }
      case RuleType.Redirection: {
        this.feature.switchToStory = null;
        this.feature.switchToStoryId = null;
        break;
      }
      case RuleType.Activation:
      default: {
        this.feature.endWithStory = null;
        this.feature.endWithStoryId = null;
        break;
      }
    }

    this.updateStoriesToDisplay();
  }
}
