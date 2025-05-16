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
import { MediaFile, SimpleAnswer, StoryDefinitionConfiguration } from '../../model/story';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { RestService } from 'src/app/core-nlp/rest/rest.service';
import { MediaDialogComponent } from '../media/media-dialog.component';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { AttachmentType } from '../../../core/model/configuration';
import { FormControl, FormGroup } from '@angular/forms';
import { getStoryIcon } from '../../../shared/utils';

interface DocumentsFilterForm {
  searchString: FormControl<string>;
  fileType: FormControl<AttachmentType | 'link'>;
  fileSuffix: FormControl<string>;
}

@Component({
  selector: 'tock-documents-story',
  templateUrl: './documents-story.component.html',
  styleUrls: ['./documents-story.component.scss']
})
export class DocumentsStoryComponent implements OnInit, OnDestroy {
  private destroy: Subject<boolean> = new Subject();

  loading: boolean = false;

  attachmentType = AttachmentType;

  private stories: StoryDefinitionConfiguration[];

  fileList: MediaFile[];

  filteredFileList: MediaFile[];

  selectedStory: StoryDefinitionConfiguration;

  category: string = '';

  fileTypes: { label: string; type: AttachmentType | 'link' }[] = [
    {
      label: 'Image files',
      type: AttachmentType.image
    },
    {
      label: 'Audio files',
      type: AttachmentType.audio
    },
    {
      label: 'Video files',
      type: AttachmentType.video
    },
    {
      label: 'Other file types',
      type: AttachmentType.file
    },
    {
      label: 'External links',
      type: 'link'
    }
  ];

  getStoryIcon = getStoryIcon;

  constructor(public state: StateService, private bot: BotService, public rest: RestService, private dialog: DialogService) {}

  ngOnInit(): void {
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe(() => {
      this.refresh();
    });

    this.load();

    this.form.valueChanges.pipe(debounceTime(300)).subscribe(() => {
      this.filterList();
    });
  }

  form = new FormGroup<DocumentsFilterForm>({
    searchString: new FormControl(),
    fileType: new FormControl(),
    fileSuffix: new FormControl()
  });

  get searchString(): FormControl {
    return this.form.get('searchString') as FormControl;
  }

  get fileType(): FormControl {
    return this.form.get('fileType') as FormControl;
  }

  get fileSuffix(): FormControl {
    return this.form.get('fileSuffix') as FormControl;
  }

  filterList(): void {
    let results = this.fileList;

    if (this.fileSuffix.value !== null) {
      results = results.filter((file) => {
        return file.suffix === this.fileSuffix.value;
      });
    }

    if (this.fileType.value !== null) {
      if (this.fileType.value === 'link') {
        results = results.filter((file) => {
          return file.externalUrl;
        });
      } else {
        results = results.filter((file) => {
          return file.type === this.fileType.value && !file.externalUrl;
        });
      }
    }

    const searchString = this.searchString.value?.toLowerCase().trim();

    if (searchString) {
      results = results.filter((file) => {
        const story = this.getStoryWithThisMediaFile(file);
        const storyName = story?.name.toLowerCase();
        const storyDesc = story?.description.toLowerCase();

        const answer = this.getAnswerWithThisMediaFile(file);
        //@ts-ignore
        const answerTitle = answer?.mediaMessage?.title?.defaultLabel?.toLowerCase();
        //@ts-ignore
        const answerSubTitle = answer?.mediaMessage?.subTitle?.defaultLabel?.toLowerCase();

        return (
          file.name.toLowerCase().includes(searchString) ||
          storyName?.includes(searchString) ||
          storyDesc?.includes(searchString) ||
          answerTitle?.includes(searchString) ||
          answerSubTitle?.includes(searchString)
        );
      });
    }

    this.filteredFileList = results.sort((a, b) => a.name.localeCompare(b.name));
  }

  getFilesSuffixes(): string[] {
    const suffixes = new Set<string>();
    this.fileList.forEach((file) => {
      if (!file.externalUrl) suffixes.add(file.suffix);
    });
    return [...suffixes];
  }

  resetSearch(): void {
    this.searchString.reset();
  }

  refresh(): void {
    this.form.reset();
    this.load();
  }

  load(): void {
    this.loading = true;

    this.bot.findStoryDefinitionsByNamespaceAndBotIdWithFileAttached(this.state.currentApplication.name).subscribe((res: any[]) => {
      this.stories = res;

      this.fileList = res
        .flatMap((obj) => obj.answers)
        .flatMap((answer) => answer.answers)
        .filter((answer) => answer?.mediaMessage?.file)
        .map((answer) => answer.mediaMessage.file);

      this.filterList();

      this.loading = false;
    });
  }

  getStoryWithThisMediaFile(file: MediaFile): StoryDefinitionConfiguration {
    return this.stories.find((story) =>
      story.answers?.some((answer) =>
        //@ts-ignore
        answer.answers?.some((innerAnswer) => innerAnswer.mediaMessage?.file?.id === file.id)
      )
    );
  }

  getAnswerWithThisMediaFile(file: MediaFile): SimpleAnswer {
    const matchingStory = this.getStoryWithThisMediaFile(file);
    if (matchingStory) {
      return (
        matchingStory.answers
          //@ts-ignore
          .flatMap((answer) => answer.answers)
          .find((innerAnswer) => innerAnswer.mediaMessage?.file?.id === file.id)
      );
    }
    return undefined;
  }

  displayMediaMessage(file: MediaFile): void {
    this.selectedStory = this.getStoryWithThisMediaFile(file);

    let answer = this.getAnswerWithThisMediaFile(file);

    let dialogRef = this.dialog.openDialog(MediaDialogComponent, {
      context: {
        // @ts-ignore
        media: answer.mediaMessage,
        category: this.selectedStory.category
      }
    });

    dialogRef.onClose.subscribe((result) => {
      if (result && (result.removeMedia || result.media)) {
        answer.mediaMessage = result.removeMedia ? null : result.media;
        this.saveStory();
      }
    });
  }

  saveStory(): void {
    this.bot.saveStory(this.selectedStory).subscribe((res) => {
      this.load();
    });
  }

  getStoryType(story: StoryDefinitionConfiguration): string {
    if (story.isBuiltIn()) {
      return 'Built-in';
    }
    if (story.isSimpleAnswer()) {
      return 'Message ';
    }
    if (story.isScriptAnswer()) {
      return 'Script';
    }
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
