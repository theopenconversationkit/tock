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

import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { StateService } from '../../../../core-nlp/state.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { FileValidators } from '../../../../shared/validators';
import { readFileAsText } from '../../../../shared/utils';
import { RestService } from '../../../../core-nlp/rest/rest.service';
import { DialogService } from '../../../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../../../shared/components';
import { RagSettings } from '../../../../rag/rag-settings/models';
import { StoryDefinitionConfigurationSummary } from '../../../model/story';

export const unknownIntentName = 'unknown';

export enum storiesImportRagConflictMode {
  DEFAULT = 'DEFAULT',
  RAG_ON = 'RAG_ON',
  RAG_OFF = 'RAG_OFF'
}

@Component({
  selector: 'tock-stories-upload',
  templateUrl: './stories-upload.component.html',
  styleUrls: ['./stories-upload.component.scss']
})
export class StoriesUploadComponent implements OnInit {
  @Output() onImportComplete = new EventEmitter();

  uploading: boolean = false;
  loading: boolean; //
  isImportSubmitted: boolean = false;
  ragSettings: RagSettings;

  constructor(
    private toastrService: NbToastrService,
    public state: StateService,
    public dialogRef: NbDialogRef<StoriesUploadComponent>,
    private dialogService: DialogService,
    private rest: RestService
  ) {}

  ngOnInit(): void {
    this.loadRagSettings();
  }

  private loadRagSettings() {
    this.loading = true;
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/rag`;
    this.rest
      .get<RagSettings>(url, (settings: RagSettings) => settings)
      .subscribe((settings: RagSettings) => {
        this.ragSettings = settings;
        this.loading = false;
      });
  }

  form: FormGroup = new FormGroup({
    file: new FormControl<File[]>([], [Validators.required, FileValidators.mimeTypeSupported(['application/json'])])
  });

  get file(): FormControl {
    return this.form.get('file') as FormControl;
  }

  get canSaveImport(): boolean {
    return this.isImportSubmitted ? this.form.valid : this.form.dirty;
  }

  fileFormatErrorMessage: string;

  import(): void {
    this.isImportSubmitted = true;
    this.fileFormatErrorMessage = undefined;

    if (this.canSaveImport) {
      const file = this.file.value[0];

      if (file.type === 'application/json') {
        readFileAsText(file).then((fileContent) => {
          const data = JSON.parse(fileContent.data);

          if (!Array.isArray(data)) {
            this.fileFormatErrorMessage =
              'The file supplied does not seem to correspond to the expected format. Please provide a stories dump file.';
          } else {
            if (
              this.ragSettings.enabled &&
              data.some((story) => {
                return story.intent.name === unknownIntentName;
              })
            ) {
              this.askConflictMode(data);
            } else {
              this.postData(data, storiesImportRagConflictMode.DEFAULT);
            }
          }
        });
      }
    }
  }

  askConflictMode(stories: StoryDefinitionConfigurationSummary[]): void {
    const story = stories.find((story) => {
      return story.intent.name === unknownIntentName;
    });

    const ragOff = 'Deactivate the RAG';
    const ragOn = "Deactivate the 'unknown' story";
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: `Unknown story detected : ${story.name}`,
        subtitle: `
        The batch of stories provided contains a story (${story.name}) whose intent is 'unknown' while Retrieval Augmented Generation (RAG) is active.
        When RAG is active, the 'unkown' story is replaced by the RAG. It is therefore not possible to import the indicated story and keep the RAG active.

        What do you want to do:
        - Deactivate the RAG and activate the 'unknown' story
        - Deactivate the 'unknown' story and keep the RAG active`,
        actions: [{ actionName: ragOff }, { actionName: ragOn }]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result) {
        let mode = storiesImportRagConflictMode.RAG_ON;
        if (result.toLocaleLowerCase() === ragOff.toLocaleLowerCase()) {
          mode = storiesImportRagConflictMode.RAG_OFF;
        }
        this.postData(stories, mode);
      }
    });
  }

  postData(stories: StoryDefinitionConfigurationSummary[], mode: storiesImportRagConflictMode): void {
    this.uploading = true;
    const url = `/bot/story/${this.state.currentApplication.name}/${this.state.currentLocale}/import`;

    const payload = {
      mode: mode,
      stories: stories
    };

    this.rest.post<any, any>(url, payload).subscribe((res) => {
      this.toastrService.show(`Stories dump successfully imported`, 'Stories imported', {
        duration: 5000,
        status: 'success'
      });
      this.uploading = false;
      this.onImportComplete.emit();
      this.cancel();
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
