import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { CompletionRequest, CompletionResponse, GeneratedSentence, GeneratedSentenceError, SentencesGenerationOptions } from './models';

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
    const url = `/configuration/bots/${this.state.currentApplication.name}/sentence-generation/info`;
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
    const url = `/gen-ai/bot/${this.state.currentApplication.name}/sentence-generation`;

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
