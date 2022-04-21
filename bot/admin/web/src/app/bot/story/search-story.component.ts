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

import { saveAs } from 'file-saver';
import { Component, Injectable, OnDestroy, OnInit } from '@angular/core';
import { BotService } from '../bot-service';
import { NlpService } from '../../nlp-tabs/nlp.service';
import { StateService } from '../../core-nlp/state.service';
import {
  StoryDefinitionConfiguration,
  StoryDefinitionConfigurationSummary,
  StorySearchQuery
} from '../model/story';
import { Subscription } from 'rxjs';
import { FileItem, FileUploader, ParsedResponseHeaders } from 'ng2-file-upload';
import { ConfirmDialogComponent } from '../../shared-nlp/confirm-dialog/confirm-dialog.component';
import { CanDeactivate } from '@angular/router';
import { LocationStrategy } from '@angular/common';
import { NbToastrService } from '@nebular/theme';
import { DialogService } from 'src/app/core-nlp/dialog.service';

interface TreeNode<T> {
  data: T;
  children?: TreeNode<T>[];
  expanded?: boolean;
}

@Component({
  selector: 'tock-search-story',
  templateUrl: './search-story.component.html',
  styleUrls: ['./search-story.component.css']
})
export class SearchStoryComponent implements OnInit, OnDestroy {
  stories: StoryDefinitionConfigurationSummary[];
  categories: string[] = [];
  selectedStory: StoryDefinitionConfiguration;

  categoryColumn = 'Story';
  intentColumn = 'Main Intent';
  descriptionColumn = 'Description';
  actionsColumn = 'Actions';
  allColumns = [this.categoryColumn, this.intentColumn, this.descriptionColumn, this.actionsColumn];
  nodes: TreeNode<any>[];
  private lastExpandableState: Map<string, boolean> = new Map<string, boolean>();

  filter: string = '';
  category: string = '';
  onlyConfigured: boolean = true;
  loading: boolean = false;

  displayUpload: boolean = false;
  uploader: FileUploader;

  private subscription: Subscription;

  constructor(
    private nlp: NlpService,
    public state: StateService,
    private bot: BotService,
    private dialogService: DialogService,
    private toastrService: NbToastrService,
    private location: LocationStrategy,
    private backButtonHolder: BackButtonHolder
  ) {}

  ngOnInit(): void {
    // check if back or forward button is pressed.
    this.location.onPopState(() => {
      this.backButtonHolder.backButton = true;
      return false;
    });
    this.search();
    this.subscription = this.state.configurationChange.subscribe((_) => this.search());
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  editStory(story: StoryDefinitionConfigurationSummary) {
    this.bot.findStory(story._id).subscribe((s) => {
      s.selected = true;
      this.selectedStory = s;
    });
  }

  downloadStory(story: StoryDefinitionConfigurationSummary) {
    setTimeout((_) => {
      this.bot.exportStory(this.state.currentApplication.name, story.storyId).subscribe((blob) => {
        saveAs(blob, this.state.currentApplication.name + '_' + story.storyId + '.json');
        this.toastrService.show(`Dump provided`, 'Dump', { duration: 3000, status: 'success' });
      });
    }, 1);
  }

  deleteStory(story: StoryDefinitionConfigurationSummary) {
    const dialogRef = this.dialogService.openDialog(ConfirmDialogComponent, {
      context: {
        title: `Remove the story '${story.name}'`,
        subtitle: 'Are you sure?',
        action: 'Remove'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === 'remove') {
        this.bot.deleteStory(story._id).subscribe((_) => {
          this.delete(story.storyId);
          this.toastrService.show(`Story deleted`, 'Delete', { duration: 3000, status: 'success' });
        });
      }
    });
  }

  private keepExpandableState() {
    if (this.selectedStory) {
      this.lastExpandableState = new Map<string, boolean>();
      this.lastExpandableState.set(this.selectedStory.category, true);
    }
  }

  delete(storyDefinitionId: string) {
    this.selectedStory = null;
    this.keepExpandableStateAndSearch();
  }

  keepExpandableStateAndSearch() {
    this.keepExpandableState();
    this.search();
  }

  search() {
    if (this.category === '_all_') this.category = '';
    this.loading = true;
    this.bot
      .searchStories(
        new StorySearchQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          0,
          10000,
          this.category,
          this.filter,
          this.onlyConfigured
        )
      )
      .subscribe((s) => {
        this.selectedStory = null;
        const storyByCategories = new Map<string, StoryDefinitionConfigurationSummary[]>();
        s.forEach((story) => {
          let a = storyByCategories.get(story.category);
          if (!a) {
            a = [];
            storyByCategories.set(story.category, a);
          }
          a.push(story);
        });

        if (this.category === '') {
          const sortStringKeys = (a, b) => (a.toLowerCase() > b.toLowerCase() ? 1 : -1);
          this.categories = Array.from(storyByCategories.keys()).sort(sortStringKeys);
        }
        const sortedMap = new Map<string, StoryDefinitionConfigurationSummary[]>();
        this.categories.forEach((c) => {
          const stories = storyByCategories.get(c);
          if (stories) {
            sortedMap.set(c, stories);
          }
        });

        this.stories = s;
        this.nodes = Array.from(sortedMap, ([key, value]) => {
          return {
            expanded:
              this.categories.length < 2 ||
              this.category != '' ||
              this.filter !== '' ||
              this.lastExpandableState.get(key) === true,
            data: {
              category: key,
              expandable: true
            },
            children: value.map((s) => {
              return {
                data: s
              };
            })
          };
        });
        this.lastExpandableState = new Map();
        this.loading = false;
      });
  }

  download() {
    setTimeout((_) => {
      this.bot.exportStories(this.state.currentApplication.name).subscribe((blob) => {
        saveAs(blob, this.state.currentApplication.name + '_stories.json');
        this.toastrService.show(`Dump provided`, 'Dump', { duration: 3000, status: 'success' });
      });
    }, 1);
  }

  prepareUpload() {
    this.uploader = new FileUploader({ removeAfterUpload: true });
    this.uploader.onCompleteItem = (
      item: FileItem,
      response: string,
      status: number,
      headers: ParsedResponseHeaders
    ) => {
      this.toastrService.show(`Dump uploaded`, 'Dump', { duration: 3000, status: 'success' });
      this.state.resetConfiguration();
    };
    this.displayUpload = true;
  }

  upload() {
    this.bot.prepareStoryDumpUploader(
      this.uploader,
      this.state.currentApplication.name,
      this.state.currentLocale
    );
    this.uploader.uploadAll();
    this.displayUpload = false;
  }
}

@Injectable()
export class BackButtonHolder {
  backButton: boolean = false;
}

@Injectable()
export class SearchStoryNavigationGuard implements CanDeactivate<any> {
  constructor(private backButtonHolder: BackButtonHolder) {}

  canDeactivate(component: any) {
    // will prevent user from going back
    try {
      if (
        this.backButtonHolder.backButton &&
        component instanceof SearchStoryComponent &&
        (component as SearchStoryComponent).selectedStory != null
      ) {
        (component as SearchStoryComponent).keepExpandableStateAndSearch();
        return false;
      }
      return true;
    } finally {
      this.backButtonHolder.backButton = false;
    }
  }
}
