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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { BotService } from '../../bot-service';
import { StateService } from '../../../core-nlp/state.service';
import { RuleType, StoryFeature, StorySearchQuery } from '../../model/story';
import { flatMap } from '../../../model/commons';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'tock-story-rule',
  templateUrl: './story-rule.component.html',
  styleUrls: ['./story-rule.component.css']
})
export class StoryRuleComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  loading: boolean = false;

  ruleType = RuleType;

  disabledFeatures: StoryFeature[] = [];

  redirectedFeatures: StoryFeature[] = [];

  endingFeatures: StoryFeature[] = [];

  constructor(private state: StateService, private botService: BotService, private configurationService: BotConfigurationService) {}

  ngOnInit(): void {
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((res) => {
      this.refresh();
    });

    this.refresh();
  }

  refresh(): void {
    if (this.state.currentApplication) {
      this.loading = true;

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

          this.loading = false;
        });
    }
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
