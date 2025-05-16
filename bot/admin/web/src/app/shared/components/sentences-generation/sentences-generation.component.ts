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

import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { CompletionRequest, CompletionResponse, GeneratedSentence, SentencesGenerationOptions } from './models';

import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { SentencesGenerationListComponent } from './sentences-generation-list/sentences-generation-list.component';
import { UserRole } from '../../../model/auth';
import { StateService } from '../../../core-nlp/state.service';
import { RestService } from '../../../core-nlp/rest/rest.service';

@Component({
  selector: 'tock-sentences-generation',
  templateUrl: './sentences-generation.component.html',
  styleUrls: ['./sentences-generation.component.scss']
})
export class SentencesGenerationComponent implements OnInit {
  @Input() sentences: string[] = [];

  @Output() onValidateSelection = new EventEmitter<string[]>();

  @ViewChild('sentencesGenerationListComp') sentencesGenerationListComp: SentencesGenerationListComponent;

  loading: boolean = true;
  initialized: boolean = false;

  llmNotEnabled: boolean = false;

  UserRole = UserRole;

  generatedSentences: GeneratedSentence[] = [];

  informNoResult: boolean = false;

  options: Partial<SentencesGenerationOptions> = {
    abbreviatedLanguage: false,
    sentencesExample: [],
    spellingMistakes: false,
    smsLanguage: false,
    llmTemperature: 0.5
  };

  constructor(
    public dialogRef: NbDialogRef<SentencesGenerationComponent>,
    public state: StateService,
    private restService: RestService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkLlmSettingsConfiguration();
  }

  checkLlmSettingsConfiguration(): void {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/sentence-generation/info`;
    this.restService
      .get(url, (settings) => settings)
      .subscribe((settings) => {
        if (!settings?.enabled) {
          this.llmNotEnabled = true;
        } else {
          this.options.llmTemperature = parseFloat(settings.llmTemperature);
        }
        this.loading = false;
        this.initialized = true;
      });
  }

  jumpToLlmSettings(): void {
    this.router.navigateByUrl('configuration/sentence-generation-settings');
    this.dialogRef.close();
  }

  handleLoading(loading: boolean): void {
    this.loading = loading;
  }

  handleGeneratedSentences(generatedSentences: string[]): void {
    this.onValidateSelection.emit(generatedSentences);
  }

  updateOptions(options: Partial<SentencesGenerationOptions>): void {
    this.options = { ...options };
  }

  generate(options: Partial<SentencesGenerationOptions> = this.options): void {
    this.informNoResult = false;

    const { abbreviatedLanguage, sentencesExample, smsLanguage, spellingMistakes, llmTemperature } = options;

    const completionRequest: CompletionRequest = {
      sentences: sentencesExample,
      locale: this.state.currentLocale,
      llmTemperature: llmTemperature,
      options: {
        abbreviatedLanguage,
        smsLanguage,
        spellingMistakes
      }
    };

    this.loading = true;

    this.generateSentences(completionRequest).subscribe({
      next: (completionResponse: CompletionResponse) => {
        this.generatedSentences = this.feedGeneratedSentences(completionResponse.sentences);
        if (!this.generatedSentences.length) {
          this.informNoResult = true;
        }
        this.loading = false;
      },
      error: (e) => {
        this.loading = false;
      }
    });
  }

  generateSentences(body: CompletionRequest): Observable<CompletionResponse> {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/completion/sentence-generation`;

    return this.restService.post<CompletionRequest, CompletionResponse>(url, body);
  }

  private feedGeneratedSentences(generatedSentences: string[]): GeneratedSentence[] {
    return generatedSentences.map((sentence: string) => ({
      distinct: this.isDistinct(sentence),
      selected: false,
      sentence
    }));
  }

  private isDistinct(sentence: string): boolean {
    return !!!this.sentences.find((s) => s === sentence);
  }

  backOptions(): void {
    this.generatedSentences = [];
  }

  get generatedSentencesSelection() {
    if (this.sentencesGenerationListComp) {
      return this.sentencesGenerationListComp.generatedSentences.filter((gs) => gs.selected);
    }
  }

  validateSelection(): void {
    const selection = this.generatedSentencesSelection.map((gs) => gs.sentence);
    this.onValidateSelection.emit(selection);
  }
}
