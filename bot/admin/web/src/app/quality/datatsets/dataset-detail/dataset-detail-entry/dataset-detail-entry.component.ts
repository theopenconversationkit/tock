import { Component, ElementRef, inject, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import {
  DatasetQuestion,
  DatasetRunAction,
  DatasetRunActionState,
  DatasetRunActionDisplayState,
  SourcesDiffResult,
  SourceInfos
} from '../../models';
import { MarkdownDiffService } from '../../services/markdown-diff.service';
import { markedParser } from '../../../../shared/utils/markup.utils';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { EventType } from '../../../../core/model/configuration';

@Component({
  selector: 'tock-dataset-detail-entry',
  templateUrl: './dataset-detail-entry.component.html',
  styleUrl: './dataset-detail-entry.component.scss'
})
export class DatasetDetailEntryComponent implements OnChanges {
  readonly DatasetRunActionDisplayState = DatasetRunActionDisplayState;

  loading: boolean = true;
  folded: boolean = false;
  isCollapsed: boolean = true;
  showToggleButton: boolean = false;
  maxContentHeight = 300;

  @Input() currentRunActions: DatasetRunAction[];
  @Input() comparisonRunActions: DatasetRunAction[];
  @Input() question: DatasetQuestion;

  currentAction: DatasetRunAction | null = null;
  comparisonAction: DatasetRunAction | null = null;

  currentActionAnswer: SafeHtml | null = null;
  comparisonActionAnswer: SafeHtml | null = null;

  // Computed once per ngOnChanges — null when no comparison or sources are not RAG
  sourceDiff: SourcesDiffResult | null = null;

  // Display state derived from action state + action nullability
  currentActionDisplayState: DatasetRunActionDisplayState | null = null;
  comparisonActionDisplayState: DatasetRunActionDisplayState | null = null;

  @ViewChild('hideableContainer') hideableContainer!: ElementRef;

  private readonly diffService = inject(MarkdownDiffService);
  private readonly sanitizer = inject(DomSanitizer);

  ngOnChanges(_changes: SimpleChanges): void {
    this.loading = true;
    this.currentActionAnswer = null;
    this.comparisonActionAnswer = null;

    this.currentAction = this.currentRunActions?.find((a) => a.questionId === this.question.id) ?? null;
    this.comparisonAction = this.comparisonRunActions?.find((a) => a.questionId === this.question.id) ?? null;

    this.currentActionDisplayState = this._computeDisplayState(this.currentAction);
    this.comparisonActionDisplayState = this._computeDisplayState(this.comparisonAction);

    // Compute source diff synchronously — exposed as a property so the template
    // never triggers recalculation on its own during change detection cycles.
    this.sourceDiff = this._computeSourceDiff();

    this._generateAnswerDiff();
  }

  // ── Content height toggle ─────────────────────────────────────────────────

  toggleCollapse(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  checkContentHeight(): void {
    if (!this.hideableContainer?.nativeElement) return;
    this.showToggleButton = this.hideableContainer.nativeElement.scrollHeight > this.maxContentHeight;
  }

  // ── Source diff helpers (template-facing, read-only) ──────────────────────

  isSourceAdded(source: SourceInfos): boolean {
    return this.sourceDiff?.added.some((s) => this._getSourceKey(s) === this._getSourceKey(source)) ?? false;
  }

  isSourceChanged(source: SourceInfos): boolean {
    return this.sourceDiff?.modified.some((s) => this._getSourceKey(s) === this._getSourceKey(source)) ?? false;
  }

  isSourceRemoved(source: SourceInfos): boolean {
    return this.sourceDiff?.removed.some((s) => this._getSourceKey(s) === this._getSourceKey(source)) ?? false;
  }

  getSourceDiffTooltip(source: SourceInfos): string {
    const key = this._getSourceKey(source);
    if (this.sourceDiff?.added.some((s) => this._getSourceKey(s) === key)) return 'This source was added in the latest run.';
    if (this.sourceDiff?.modified.some((s) => this._getSourceKey(s) === key)) return 'This source was modified in the latest run.';
    if (this.sourceDiff?.removed.some((s) => this._getSourceKey(s) === key)) return 'This source was removed in the latest run.';
    return 'This source is unchanged.';
  }

  // ── Action type label ─────────────────────────────────────────────────────

  getActionTypeLabel(side: 'A' | 'B'): string {
    // TODO: use BotMessage.isSentence(), BotMessage.isSentenceWithFootnotes(), etc...
    const action = side === 'A' ? this.currentAction : this.comparisonAction;
    if (!action?.action) return '-';
    if (action.action.metadata?.isGenAiRagAnswer) return 'RAG';
    return this._eventTypeLabel(EventType[action.action.message.eventType as string]);
  }

  // ── Private ───────────────────────────────────────────────────────────────

  private async _generateAnswerDiff(): Promise<void> {
    try {
      const currentText = this.currentAction?.action?.message?.text;
      const comparisonText = this.comparisonAction?.action?.message?.text;

      if (currentText && comparisonText) {
        const result = await this.diffService.diff(currentText, comparisonText);
        this.currentActionAnswer = this.sanitizer.bypassSecurityTrustHtml(markedParser.parse(result.textA) as string);
        this.comparisonActionAnswer = this.sanitizer.bypassSecurityTrustHtml(markedParser.parse(result.textB) as string);
      } else if (currentText) {
        this.currentActionAnswer = this.sanitizer.bypassSecurityTrustHtml(markedParser.parse(currentText) as string);
      }
    } catch (err) {
      console.error('Failed to generate answer diff', err);
    } finally {
      this.loading = false;
      // Defer height check to the next tick so the DOM reflects the new content
      setTimeout(() => this.checkContentHeight());
    }
  }

  private _computeSourceDiff(): SourcesDiffResult | null {
    const currentFootnotes = this.currentAction?.action?.message?.footnotes;
    const comparisonFootnotes = this.comparisonAction?.action?.message?.footnotes;

    if (
      !this.comparisonAction ||
      !this.currentAction?.action?.metadata?.isGenAiRagAnswer ||
      !this.comparisonAction?.action?.metadata?.isGenAiRagAnswer ||
      !currentFootnotes ||
      !comparisonFootnotes
    ) {
      return null;
    }

    const mapB = new Map<string, SourceInfos>(comparisonFootnotes.map((s) => [this._getSourceKey(s), s]));

    const added: SourceInfos[] = [];
    const modified: SourceInfos[] = [];
    const removed: SourceInfos[] = [];

    for (const sourceA of currentFootnotes) {
      const key = this._getSourceKey(sourceA);
      const sourceB = mapB.get(key);
      if (!sourceB) {
        added.push(sourceA);
      } else if (sourceB.content !== sourceA.content) {
        modified.push(sourceA);
      }
    }

    for (const sourceB of comparisonFootnotes) {
      if (!currentFootnotes.some((s) => this._getSourceKey(s) === this._getSourceKey(sourceB))) {
        removed.push(sourceB);
      }
    }

    return { added, modified, removed };
  }

  private _computeDisplayState(action: DatasetRunAction | null): DatasetRunActionDisplayState | null {
    if (!action) return null;
    if (action.state === DatasetRunActionState.FAILED) return DatasetRunActionDisplayState.FAILED;
    if (action.state === DatasetRunActionState.COMPLETED && !action.action) return DatasetRunActionDisplayState.PURGED;
    return DatasetRunActionDisplayState.SUCCESS;
  }

  private _getSourceKey(source: SourceInfos): string {
    return source.url || source.title;
  }

  private _eventTypeLabel(eventType: EventType): string {
    switch (eventType) {
      case EventType.sentence:
      case EventType.sentenceWithFootnotes:
        return 'INTENT';
      case EventType.attachment:
        return 'ATTACHMENT';
      case EventType.choice:
        return 'CHOICE';
      case EventType.location:
        return 'LOCATION';
      default:
        return '-';
    }
  }
}
