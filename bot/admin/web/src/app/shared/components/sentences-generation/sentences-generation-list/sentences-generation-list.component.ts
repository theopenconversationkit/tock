import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { GeneratedSentence } from './../models';

@Component({
  selector: 'tock-sentences-generation-list',
  templateUrl: './sentences-generation-list.component.html',
  styleUrls: ['./sentences-generation-list.component.scss']
})
export class SentencesGenerationListComponent implements OnChanges {
  @Input() generatedSentences: GeneratedSentence[] = [];

  @Output() onBackOptions = new EventEmitter();
  @Output() onGenerate = new EventEmitter();

  distinctGeneratedSentencesLength: number = 0;

  ngOnChanges(changes: SimpleChanges) {
    if (changes.generatedSentences.currentValue) {
      this.distinctGeneratedSentencesLength = this.feedDistinctGeneratedSentencesLength();
    }
  }

  get isSelectedGeneratedSentences(): boolean {
    const generatedSentencesLength = this.generatedSentences.filter(
      (generatedSentence: GeneratedSentence) => generatedSentence.selected
    ).length;

    return generatedSentencesLength > 0 && generatedSentencesLength < this.distinctGeneratedSentencesLength;
  }

  get isAllSelectedGeneratedSentences(): boolean {
    const generatedSentencesLength = this.generatedSentences.filter(
      (generatedSentence: GeneratedSentence) => generatedSentence.selected
    ).length;

    return this.distinctGeneratedSentencesLength > 0 && this.distinctGeneratedSentencesLength === generatedSentencesLength;
  }

  private feedDistinctGeneratedSentencesLength(): number {
    return this.generatedSentences.reduce((acc: number, curr: GeneratedSentence) => {
      if (curr.distinct && !curr.errorMessage) acc++;

      return acc;
    }, 0);
  }

  toggleAllSelectGeneratedSentences(value: boolean): void {
    if (!value) {
      this.generatedSentences = this.generatedSentences.map((generatedSentence: GeneratedSentence) => {
        if (generatedSentence.distinct && !generatedSentence.errorMessage) return { ...generatedSentence, selected: false };

        return generatedSentence;
      });
    } else {
      this.generatedSentences = this.generatedSentences.map((generatedSentence: GeneratedSentence) => {
        if (generatedSentence.distinct && !generatedSentence.errorMessage) return { ...generatedSentence, selected: true };

        return generatedSentence;
      });
    }
  }

  toggleSelectGeneratedSentence(sentence: GeneratedSentence): void {
    this.generatedSentences = this.generatedSentences.map((generatedSentence: GeneratedSentence) => {
      if (generatedSentence === sentence) return { ...generatedSentence, selected: !generatedSentence.selected };

      return generatedSentence;
    });
  }

  backOptions(): void {
    this.onBackOptions.emit();
  }

  generate(): void {
    this.onGenerate.emit();
  }
}
