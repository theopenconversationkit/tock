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

import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { Subject, take } from 'rxjs';
import { RuleType, StoryDefinitionConfiguration, StoryFeature } from '../../../model/story';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { CreateRuleComponent } from '../create-rule/create-rule.component';
import { ChoiceDialogComponent } from '../../../../shared/components';
import { BotService } from '../../../bot-service';
import { getStoryIcon } from '../../../../shared/utils';

@Component({
  selector: 'tock-story-rules-table',
  templateUrl: './story-rules-table.component.html',
  styleUrls: ['./story-rules-table.component.scss']
})
export class StoryRulesTableComponent implements OnChanges, OnDestroy {
  destroy = new Subject();

  ruleType = RuleType;

  filteredFeatures: StoryFeature[] = [];

  searchFilterString: string;

  getStoryIcon = getStoryIcon;

  @Input() type: RuleType;

  @Input() features: StoryFeature[] = [];

  @Output() onRefresh = new EventEmitter<boolean>();

  constructor(private nbDialogService: NbDialogService, private botService: BotService, private toastrService: NbToastrService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.features?.currentValue) {
      this.resetSearch();
    }
  }

  hasTarget(): boolean {
    return [RuleType.Redirection, RuleType.Ending].includes(this.type);
  }

  newRule(): void {
    this.nbDialogService
      .open(CreateRuleComponent, {
        context: {
          type: this.type
        }
      })
      .componentRef.instance.onSave.pipe(take(1))
      .subscribe((res) => {
        this.onRefresh.emit(true);
      });
  }

  toggle(feature: StoryFeature, newState: boolean): void {
    feature.enabled = newState;
    this.botService.saveStory(feature.story).subscribe(() => {
      this.toastrService.show(`${this.type} rule ${newState ? 'activated' : 'deactivated'}`, 'Rule update', { duration: 3000 });
    });
  }

  askDeleteFeature(feature: StoryFeature): void {
    const confirmAction = 'Delete';
    const cancelAction = 'Cancel';

    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `Delete ${this.type} rule`,
        subtitle: `Are you sure you want to delete the ${this.type} rule applied to the story "${feature.story.name}"`,
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'basic' },
          { actionName: confirmAction, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result?.toLowerCase() === confirmAction.toLowerCase()) {
        this.deleteFeature(feature);
      }
    });
  }

  deleteFeature(feature: StoryFeature): void {
    const story = feature.story;

    story.features.splice(story.features.indexOf(feature), 1);

    this.botService.saveStory(story).subscribe(() => {
      this.onRefresh.emit(true);
      this.toastrService.show(`${this.type} rule deleted`, 'Rule deletion', { duration: 3000 });
    });
  }

  filterFeatures(): void {
    this.filteredFeatures = this.features.filter((feature) => {
      return feature.story.name.toLowerCase().trim().includes(this.searchFilterString.toLowerCase().trim());
    });
  }

  resetSearch(): void {
    this.searchFilterString = undefined;
    this.filteredFeatures = this.features;
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
