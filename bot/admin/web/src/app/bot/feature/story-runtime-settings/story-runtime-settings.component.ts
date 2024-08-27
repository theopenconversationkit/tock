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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { BotService } from '../../bot-service';
import { StoryDefinitionConfiguration } from '../../model/story';
import { StateService } from '../../../core-nlp/state.service';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { getStoryIcon } from '../../../shared/utils';

@Component({
  selector: 'tock-story-runtime-settings',
  templateUrl: './story-runtime-settings.component.html',
  styleUrls: ['./story-runtime-settings.component.css']
})
export class StoryRuntimeSettingsComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  disableStories: StoryDefinitionConfiguration[];
  enableStories: StoryDefinitionConfiguration[];
  checkOnlySubEntitiesForStorySelection: StoryDefinitionConfiguration[];
  checkSubEntitiesOnlyWithStoryIntents: StoryDefinitionConfiguration[];
  taggedStoriesCount: number = 0;

  loading: boolean;

  getStoryIcon = getStoryIcon;

  constructor(private state: StateService, private botService: BotService, private router: Router) {}

  ngOnInit(): void {
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((res) => {
      this.refresh();
    });

    this.refresh();
  }

  refresh() {
    if (this.state.currentApplication) {
      this.loading = true;

      this.botService.findRuntimeStorySettings(this.state.currentApplication.name).subscribe((stories) => {
        this.disableStories = stories.filter((story) => story.tags.some((tag) => tag === 'DISABLE'));

        this.enableStories = stories.filter((story) => story.tags.some((tag) => tag === 'ENABLE'));

        this.checkOnlySubEntitiesForStorySelection = stories.filter((story) => story.tags.some((tag) => tag === 'CHECK_ONLY_SUB_STEPS'));

        this.checkSubEntitiesOnlyWithStoryIntents = stories.filter((story) =>
          story.tags.some((tag) => tag === 'CHECK_ONLY_SUB_STEPS_WITH_STORY_INTENT')
        );

        this.taggedStoriesCount =
          this.disableStories.length +
          this.enableStories.length +
          this.checkOnlySubEntitiesForStorySelection.length +
          this.checkSubEntitiesOnlyWithStoryIntents.length;

        this.loading = false;
      });
    }
  }

  editStory(story: StoryDefinitionConfiguration) {
    this.router.navigateByUrl('/build/story-edit/' + story._id);
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
