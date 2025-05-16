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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { StateService } from '../../../../core-nlp/state.service';
import { IntentName, StoryDefinitionConfigurationSummary } from '../../../model/story';
import { NbDialogService } from '@nebular/theme';
import { Intent } from '../../../../model/nlp';
import { IntentStoryDetailsComponent } from '../../../../shared/components';

@Component({
  selector: 'tock-stories-list',
  templateUrl: './stories-list.component.html',
  styleUrls: ['./stories-list.component.scss']
})
export class StoriesListComponent {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() stories: StoryDefinitionConfigurationSummary[];

  @Output() onEditStory = new EventEmitter<StoryDefinitionConfigurationSummary>();
  @Output() onDownloadStory = new EventEmitter<StoryDefinitionConfigurationSummary>();
  @Output() onDeleteStory = new EventEmitter<StoryDefinitionConfigurationSummary>();

  dateFormat = 'dd/MM/yyyy HH:mm';

  constructor(public state: StateService, private nbDialogService: NbDialogService) {}

  editStory(story) {
    this.onEditStory.emit(story);
  }

  downloadStory(story) {
    this.onDownloadStory.emit(story);
  }

  deleteStory(story) {
    this.onDeleteStory.emit(story);
  }

  displayIntentStoryDetails(intent: IntentName) {
    this.nbDialogService.open(IntentStoryDetailsComponent, {
      context: {
        intentName: intent.name
      }
    });
  }
}
