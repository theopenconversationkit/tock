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

import { saveAs } from 'file-saver-es';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { BotService } from '../../bot-service';
import { StateService } from '../../../core-nlp/state.service';
import { StoryDefinitionConfigurationSummary, StorySearchQuery } from '../../model/story';
import { Subject, takeUntil } from 'rxjs';
import { Router } from '@angular/router';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { StoriesFilters } from './stories-filter/stories-filter.component';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../../core/model/configuration';
import { StoriesUploadComponent } from './stories-upload/stories-upload.component';
import { getExportFileName, normalize } from '../../../shared/utils';
import { ChoiceDialogComponent } from '../../../shared/components';

export type StoriesByCategory = { category: string; stories: StoryDefinitionConfigurationSummary[] };

@Component({
  selector: 'tock-search-story',
  templateUrl: './search-story.component.html',
  styleUrls: ['./search-story.component.scss']
})
export class SearchStoryComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  configurations: BotApplicationConfiguration[];

  loading: boolean = false;

  expandedCategory: string = 'default';

  stories: StoryDefinitionConfigurationSummary[];

  storiesFilters: StoriesFilters = { configuredStoriesOnly: true };
  filteredStories: StoryDefinitionConfigurationSummary[];

  displayStoriesByCategory: boolean = true;
  categories: string[] = [];
  storyCategories: StoriesByCategory[];

  constructor(
    public state: StateService,
    private bot: BotService,
    private dialogService: DialogService,
    private toastrService: NbToastrService,
    private router: Router,
    private location: Location,
    private botConfiguration: BotConfigurationService,
    private nbDialogService: NbDialogService
  ) {
    const cat = (this.location.getState() as any)?.category;
    if (cat) this.expandedCategory = cat;
  }

  ngOnInit(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy)).subscribe((confs) => {
      this.configurations = confs;

      if (confs.length) {
        this.search();
      } else {
        this.stories = undefined;
        this.filteredStories = undefined;
        this.storyCategories = undefined;
      }
    });
  }

  search() {
    this.loading = true;
    this.bot
      .searchStories(
        new StorySearchQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          0,
          10000,
          '',
          '',
          false
        )
      )
      .subscribe((stories) => {
        this.stories = stories;
        this.filterStories();
        this.computeStoriesCategories();
        this.initExpandedCategory();

        this.loading = false;
      });
  }

  onFilterChange(filters) {
    this.storiesFilters = filters;
    this.filterStories();

    if (this.storiesFilters.search?.length || this.storiesFilters.categories?.length) {
      this.displayStoriesByCategory = false;
    } else {
      this.computeStoriesCategories();
      this.displayStoriesByCategory = true;
    }
  }

  filterStories() {
    this.filteredStories = this.stories.filter((story) => {
      if (this.storiesFilters?.configuredStoriesOnly) {
        if (story.isBuiltIn()) {
          return false;
        }
      }

      if (this.storiesFilters?.search) {
        const normalizedKeywords = normalize(this.storiesFilters.search).toLocaleLowerCase().split(' ');
        const normalizedStoryName = normalize(story.name).toLocaleLowerCase();
        const normalizedStoryDescription = normalize(story.description).toLocaleLowerCase();
        if (
          !normalizedKeywords.every((keyword) => normalizedStoryName.includes(keyword)) &&
          !normalizedKeywords.every((keyword) => normalizedStoryDescription.includes(keyword))
        ) {
          return false;
        }
      }

      if (this.storiesFilters?.categories?.length) {
        if (!this.storiesFilters.categories.includes(story.category)) {
          return false;
        }
      }

      return true;
    });

    if (this.storiesFilters?.sortStoriesByModificationDate) {
      this.filteredStories.sort((a, b) => {
        return new Date(b.lastEdited).valueOf() - new Date(a.lastEdited).valueOf();
      });
    } else {
      this.filteredStories.sort((a, b) => (a.name.toLocaleLowerCase() > b.name.toLocaleLowerCase() ? 1 : -1));
    }
  }

  computeStoriesCategories() {
    const storyCategoriesMap = new Map<string, StoryDefinitionConfigurationSummary[]>();
    this.filteredStories.forEach((story) => {
      let a = storyCategoriesMap.get(story.category);
      if (!a) {
        a = [];
        storyCategoriesMap.set(story.category, a);
      }
      a.push(story);
    });

    this.categories = Array.from(storyCategoriesMap.keys()).sort((a, b) => (a.toLocaleLowerCase() > b.toLocaleLowerCase() ? 1 : -1));

    const storyCategories = [];
    storyCategoriesMap.forEach((strs, cat) => {
      storyCategories.push({
        category: cat,
        stories: strs
      });
    });

    this.storyCategories = storyCategories.sort((a, b) => (a.category.toLocaleLowerCase() > b.category.toLocaleLowerCase() ? 1 : -1));
  }

  initExpandedCategory() {
    if (!this.storyCategories.find((cat) => cat.category === this.expandedCategory)) {
      this.expandedCategory = this.storyCategories[0]?.category;
    }
  }

  isCategoryExpanded(category): boolean {
    return category.category.toLocaleLowerCase() === this.expandedCategory.toLocaleLowerCase();
  }

  collapsedChange(category): void {
    this.expandedCategory = category.category;
  }

  editStory(story: StoryDefinitionConfigurationSummary) {
    this.router.navigateByUrl('/build/story-edit/' + story._id);
  }

  downloadStory(story: StoryDefinitionConfigurationSummary) {
    setTimeout((_) => {
      this.bot.exportStory(this.state.currentApplication.name, story.storyId).subscribe((blob) => {
        const exportFileName = getExportFileName(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          'Story',
          'json',
          story.storyId
        );
        saveAs(blob, exportFileName);
        this.toastrService.show(`Dump provided`, 'Dump', { duration: 3000, status: 'success' });
      });
    }, 1);
  }

  deleteStory(story: StoryDefinitionConfigurationSummary) {
    const action = 'remove';
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: `Remove the story "${story.name}"`,
        subtitle: 'Are you sure?',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ],
        modalStatus: 'danger'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) {
        this.bot.deleteStory(story._id).subscribe((_) => {
          this.stories = this.stories.filter((str) => story != str);
          this.filterStories();
          this.computeStoriesCategories();
          this.toastrService.show(`Story deleted`, 'Delete', { duration: 3000, status: 'success' });
        });
      }
    });
  }

  download() {
    setTimeout((_) => {
      this.bot.exportStories(this.state.currentApplication.name).subscribe((blob) => {
        const exportFileName = getExportFileName(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          'Stories',
          'json'
        );
        saveAs(blob, exportFileName);
        this.toastrService.show(`Dump provided`, 'Dump', { duration: 3000, status: 'success' });
      });
    }, 1);
  }

  prepareUpload() {
    const modal = this.nbDialogService.open(StoriesUploadComponent);
    modal.componentRef.instance.onImportComplete.subscribe((res) => {
      this.search();
    });
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
