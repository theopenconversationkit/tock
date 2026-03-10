import { Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { DatasetRun } from '../../models';
import { NbDialogRef } from '@nebular/theme';
import { markedParser } from '../../../../shared/utils/markup.utils';
import { SettingsService } from '../../../../core-nlp/settings.service';
import { MarkdownDiffService } from '../../services/markdown-diff.service';
import { ObjectDiffService } from '../../services/object-diff.service';

export enum SettingsDiffCurrentTabs {
  CondensingPrompt = 'condensing-prompt',
  AnsweringPrompt = 'answering-prompt',
  RagSettings = 'rag-settings'
}

@Component({
  selector: 'tock-dataset-detail-settings-diff',
  templateUrl: './settings-diff.component.html',
  styleUrl: './settings-diff.component.scss'
})
export class DatasetDetailSettingsDiffComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  loading: boolean = true;

  currentTabs = SettingsDiffCurrentTabs;

  @Input() currentRun: DatasetRun;
  @Input() comparisonRun: DatasetRun;
  @Input() currentTab: SettingsDiffCurrentTabs = SettingsDiffCurrentTabs.AnsweringPrompt;

  htmlRunA: string | null = null;
  htmlRunB: string | null = null;

  private readonly markdownDiffService = inject(MarkdownDiffService);
  private readonly objectDiffService = inject(ObjectDiffService);

  constructor(public dialogRef: NbDialogRef<DatasetDetailSettingsDiffComponent>, public settings: SettingsService) {}

  ngOnInit(): void {
    this.setMarkup();
  }

  switchCurrentTab(which: SettingsDiffCurrentTabs) {
    this.currentTab = which;
    if (this.currentTab !== SettingsDiffCurrentTabs.RagSettings) {
      this.setMarkup();
    }
  }

  async setMarkup(): Promise<void> {
    if (this.currentTab === SettingsDiffCurrentTabs.AnsweringPrompt) {
      if (this.comparisonRun) {
        const result = await this.markdownDiffService.diff(
          this.currentRun.settingsSnapshot.questionAnsweringPrompt?.template,
          this.comparisonRun.settingsSnapshot.questionAnsweringPrompt?.template
        );
        this.htmlRunA = markedParser.parse(result.textA) as string;
        this.htmlRunB = markedParser.parse(result.textB) as string;
      } else {
        this.htmlRunA = markedParser.parse(this.currentRun.settingsSnapshot.questionAnsweringPrompt?.template) as string;
      }
    }

    if (this.currentTab === SettingsDiffCurrentTabs.CondensingPrompt) {
      if (this.comparisonRun) {
        const result = await this.markdownDiffService.diff(
          this.currentRun.settingsSnapshot.questionCondensingPrompt?.template,
          this.comparisonRun.settingsSnapshot.questionCondensingPrompt?.template
        );
        this.htmlRunA = markedParser.parse(result.textA) as string;
        this.htmlRunB = markedParser.parse(result.textB) as string;
      } else {
        this.htmlRunA = markedParser.parse(this.currentRun.settingsSnapshot.questionCondensingPrompt?.template) as string;
      }
    }
  }

  getJson(run: 'current' | 'compare') {
    if (this.comparisonRun) {
      const result = this.objectDiffService.diff(this.currentRun.settingsSnapshot, this.comparisonRun.settingsSnapshot);
      if (run === 'current') return result.htmlA;
      if (run === 'compare') return result.htmlB;
    } else {
      return JSON.stringify(this.currentRun.settingsSnapshot, null, 2);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
