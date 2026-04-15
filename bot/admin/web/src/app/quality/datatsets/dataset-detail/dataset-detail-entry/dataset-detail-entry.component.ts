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
import { markedParserForDiff } from '../../../../shared/utils/markup.utils';
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
  truncateTitle: boolean = true;
  formatting: boolean = true;

  @Input() currentRunActions: DatasetRunAction[];
  @Input() comparisonRunActions: DatasetRunAction[];
  @Input() question: DatasetQuestion;

  currentAction: DatasetRunAction | null = null;
  comparisonAction: DatasetRunAction | null = null;

  currentActionAnswer: SafeHtml | null = null;
  comparisonActionAnswer: SafeHtml | null = null;

  // Computed once per ngOnChanges — null when no comparison or sources are not RAG
  sourceDiff: SourcesDiffResult | null = null;

  actionTypeTransition: string | null = null;

  // Display state derived from action state + action nullability
  currentActionDisplayState: DatasetRunActionDisplayState | null = null;
  comparisonActionDisplayState: DatasetRunActionDisplayState | null = null;

  currentFootnotesOrdered: SourceInfos[] = [];
  comparisonFootnotesOrdered: SourceInfos[] = [];

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

    const [currentOrdered, comparisonOrdered] = this._computeSourceOrder();
    this.currentFootnotesOrdered = currentOrdered;
    this.comparisonFootnotesOrdered = comparisonOrdered;

    this.actionTypeTransition = this._computeActionTypeTransition();

    this._generateAnswerDiff();
  }

  switchTruncateTitle(): void {
    this.truncateTitle = !this.truncateTitle;
  }

  switchFormatting(): void {
    this.formatting = !this.formatting;
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

  showSourceDetail(source): void {
    source._detail = !source._detail;
  }

  // ── Action type label ─────────────────────────────────────────────────────

  getActionTypeLabel(side: 'A' | 'B'): string {
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
        this.currentActionAnswer = this.sanitizer.bypassSecurityTrustHtml(markedParserForDiff.parse(result.textA) as string);
        this.comparisonActionAnswer = this.sanitizer.bypassSecurityTrustHtml(markedParserForDiff.parse(result.textB) as string);
      } else if (currentText) {
        this.currentActionAnswer = this.sanitizer.bypassSecurityTrustHtml(markedParserForDiff.parse(currentText) as string);
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

    // Content hash is the only stable identity for a chunk across ingestions.
    // If content is absent we cannot reliably compare — bail out.
    const allHaveContent = [...currentFootnotes, ...comparisonFootnotes].every((s) => !!s.content);
    if (!allHaveContent) return null;

    // key = document identity (url or title) + djb2 hash of content.
    // Stable across ingestions, discriminant across chunks of the same document.
    const chunkKey = (s: SourceInfos) => `${s.url ?? s.title}::${this._hashContent(s.content!)}`;

    // Document-level key — groups all chunks belonging to the same source document.
    const docKey = (s: SourceInfos) => s.url || s.title;

    const keysA = new Map<string, SourceInfos>(currentFootnotes.map((s) => [chunkKey(s), s]));
    const keysB = new Map<string, SourceInfos>(comparisonFootnotes.map((s) => [chunkKey(s), s]));

    // Chunks present in A (current/recent run) but absent from B (older run) → added in the recent run.
    const added: SourceInfos[] = [];
    for (const [key, src] of keysA) {
      if (!keysB.has(key)) added.push(src);
    }

    // Chunks present in B (older run) but absent from A (recent run) → removed in the recent run.
    const removed: SourceInfos[] = [];
    for (const [key, src] of keysB) {
      if (!keysA.has(key)) removed.push(src);
    }

    // Modified: documents that appear on both sides but whose set of content hashes
    // differs (at least one chunk changed, added, or removed within the document).
    // We report one representative SourceInfos per modified document (the first chunk
    // from the current run) so the template can display a single badge per document.
    const modified: SourceInfos[] = [];

    const groupByDoc = (footnotes: SourceInfos[]): Map<string, SourceInfos[]> =>
      footnotes.reduce((map, s) => {
        const k = docKey(s);
        map.set(k, [...(map.get(k) ?? []), s]);
        return map;
      }, new Map<string, SourceInfos[]>());

    const docGroupsA = groupByDoc(currentFootnotes);
    const docGroupsB = groupByDoc(comparisonFootnotes);

    for (const [dk, chunksA] of docGroupsA) {
      const chunksB = docGroupsB.get(dk);

      // Document entirely absent from B — already captured in `removed`, skip.
      if (!chunksB) continue;

      const hashSetA = new Set(chunksA.map(chunkKey));
      const hashSetB = new Set(chunksB.map(chunkKey));

      const hasChange = [...hashSetA].some((h) => !hashSetB.has(h)) || [...hashSetB].some((h) => !hashSetA.has(h));

      if (hasChange) {
        // Use the first chunk of the current run as the display representative.
        modified.push(chunksA[0]);
      }
    }

    return { added, modified, removed };
  }

  private _computeActionTypeTransition(): string | null {
    if (!this.comparisonAction || !this.currentAction) return null;

    const labelA = this.getActionTypeLabel('A');
    const labelB = this.getActionTypeLabel('B');

    if (labelA === labelB || labelA === '-' || labelB === '-') return null;

    return `${labelA} < ${labelB}`; // ex: "INTENT > RAG" ou "RAG > INTENT"
  }

  private _computeDisplayState(action: DatasetRunAction | null): DatasetRunActionDisplayState | null {
    if (!action) return null;
    if (action.state === DatasetRunActionState.FAILED) return DatasetRunActionDisplayState.FAILED;
    if (action.state === DatasetRunActionState.COMPLETED && !action.action) return DatasetRunActionDisplayState.PURGED;
    return DatasetRunActionDisplayState.SUCCESS;
  }

  private _getSourceKey(source: SourceInfos): string {
    // Prefer content hash as stable identity across ingestions.
    // Fall back to url or title if content is unavailable.
    if (source.content) return `${source.url ?? source.title}::${this._hashContent(source.content)}`;
    return source.url || source.title;
  }

  // djb2 hash — fast, no crypto overhead, sufficient for diff identity.
  private _hashContent(str: string): string {
    let h = 5381;
    for (let i = 0; i < str.length; i++) {
      h = ((h << 5) + h) ^ str.charCodeAt(i);
    }
    return (h >>> 0).toString(36);
  }

  // Builds a shared reference order for both sides:
  // 1. Sources present on both sides, in A's order of appearance
  // 2. Sources only in A (removed from B) — appended after
  // 3. Sources only in B (added in A) — appended after
  // Both returned arrays follow this same doc-level ordering,
  // with each document's chunks preserving their original relative order.
  private _computeSourceOrder(): [SourceInfos[], SourceInfos[]] {
    const currentFootnotes = this.currentAction?.action?.message?.footnotes ?? [];
    const comparisonFootnotes = this.comparisonAction?.action?.message?.footnotes ?? [];

    if (!currentFootnotes.length && !comparisonFootnotes.length) return [[], []];

    const docKey = (s: SourceInfos) => s.url || s.title;

    const docKeysA = [...new Set(currentFootnotes.map(docKey))];
    const docKeysB = new Set(comparisonFootnotes.map(docKey));

    // 1. Common docs — in A's order
    const commonKeys = docKeysA.filter((k) => docKeysB.has(k));

    // 2. Only in A (removed) — in A's order
    const onlyInA = docKeysA.filter((k) => !docKeysB.has(k));

    // 3. Only in B (added) — in B's order
    const docKeysB_ordered = [...new Set(comparisonFootnotes.map(docKey))];
    const onlyInB = docKeysB_ordered.filter((k) => !new Set(docKeysA).has(k));

    const referenceOrder = [...commonKeys, ...onlyInA, ...onlyInB];
    const rank = new Map<string, number>(referenceOrder.map((k, i) => [k, i] as [string, number]));

    const sort = (footnotes: SourceInfos[]) =>
      [...footnotes].sort((a, b) => {
        const ra = rank.get(docKey(a)) ?? Infinity;
        const rb = rank.get(docKey(b)) ?? Infinity;
        if (ra !== rb) return ra - rb;
        // Same document: preserve original chunk order
        return footnotes.indexOf(a) - footnotes.indexOf(b);
      });

    return [sort(currentFootnotes), sort(comparisonFootnotes)];
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
