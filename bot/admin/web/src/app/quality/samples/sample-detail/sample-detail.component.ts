import { Component, Inject, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EvaluationStatus, EvaluationSampleDefinition, EvaluationSampleStatus, EvaluationSampleDataDefinition } from '../models';
import { ActionReport, DialogReport } from '../../../shared/model/dialog-data';
import { Pagination } from '../../../shared/components';
import { Subject, take, takeUntil } from 'rxjs';
import { StateService } from '../../../core-nlp/state.service';
import { AnalyticsService } from '../../../analytics/analytics.service';
import { DatePipe, DOCUMENT } from '@angular/common';
import { scrollToPageTop } from '../../../shared/utils';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { getEvaluationBaseUrl, getEvaluationRate, getSampleCoverage } from '../utils';
import { generateSampleReport } from '../generate-sample-report';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { BotConfigurationService } from '../../../core/bot-configuration.service';

@Component({
  selector: 'tock-sample-detail',
  templateUrl: './sample-detail.component.html',
  styleUrl: './sample-detail.component.scss'
})
export class SampleDetailComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  loading: boolean = true;
  detailsVisible: boolean = false;
  private missingDialogIds = new Set<string>();
  missingDialogsCount: number = 0;
  missingDialogsWarning: boolean = false;
  validationComment: string = '';

  sample: EvaluationSampleDefinition;
  data: EvaluationSampleDataDefinition;
  evaluationSampleStatus = EvaluationSampleStatus;

  pagination: Pagination = {
    start: 0,
    end: undefined,
    size: 10,
    total: undefined
  };

  private validationModalRef: any;

  @ViewChild('sampleValidationModal') sampleValidationModal: TemplateRef<any>;

  constructor(
    private botConfiguration: BotConfigurationService,
    private route: ActivatedRoute,
    private router: Router,
    private stateService: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private nbDialogService: NbDialogService,
    private datePipe: DatePipe,
    @Inject(DOCUMENT) private document: Document
  ) {}

  // ─── Lifecycle ───────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      // if configuration change and we already have a sample it is not the initial load of the component but a change of the current application, so we need to route back to the sample board as the current sample is related to the previous application
      if (confs.length) {
        if (this.sample) {
          this.router.navigate(['/quality/samples']);
        } else {
          this.route.params.subscribe((params) => {
            this.fetchEvaluation(params['id']);
          });
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  // ─── Fetch ───────────────────────────────────────────────────────────────────

  fetchEvaluation(sampleId: string): void {
    this.loading = true;
    const url = `${this.evaluationBaseUrl}/${sampleId}`;

    this.rest
      .get(url, (evaluations: any) => evaluations)
      .subscribe({
        next: (res) => {
          if (!res._id) {
            this.showError('Sample not found');
            return;
          }
          this.sample = res;
          this.loading = false;
          this.fetchActionRefs();
        },
        error: () => this.showError()
      });
  }

  fetchActionRefs(): void {
    this.data = null;
    this.search(this.pagination.start, this.pagination.size, false, false, true);
  }

  actionRefsObservable(start: number, size: number) {
    const url = `${this.evaluationBaseUrl}/${this.sample._id}/action-refs`;

    return this.rest.post(url, { start, size });
  }

  search(
    start: number = 0,
    size: number = this.pagination.size,
    add: boolean = false,
    scrollToTop: boolean = false,
    initialLoad: boolean = false
  ): void {
    this.actionRefsObservable(start, size)
      .pipe(take(1))
      .subscribe({
        next: (result: any) => {
          this.updatePagination(result);
          const filteredResult = this.handleMissingDialogs(result);

          const { dialogs, evaluations } = this.mapResultToData(filteredResult);

          this.data = add
            ? { dialogs: [...this.data.dialogs, ...dialogs], evaluations: [...this.data.evaluations, ...evaluations] }
            : { dialogs, evaluations };

          if (!add) this.pagination.start = result.start;
          if (scrollToTop) scrollToPageTop(this.document);
          if (initialLoad) this.handleInitialLoadScroll();
        },
        error: () => this.showError()
      });
  }

  // ─── Evaluation ──────────────────────────────────────────────────────────────

  evaluateBotAction(evaluatedAction: { action: ActionReport; evaluation: EvaluationStatus; reason?: string }): void {
    const { action, evaluation, reason } = evaluatedAction;

    if (action._evaluation.status === evaluation) return;

    const url = `${this.evaluationBaseUrl}/${this.sample._id}/evaluations/${action._evaluation._id}`;
    const payload = { status: evaluation, ...(reason && { reason }) };

    this.rest.put(url, payload).subscribe({
      next: () => this.applyEvaluationResult(action, evaluation, reason),
      error: () => this.showError()
    });
  }

  validateSample(): void {
    const url = `${this.evaluationBaseUrl}/${this.sample._id}/change-status`;
    const payload = { targetStatus: EvaluationSampleStatus.VALIDATED, comment: this.validationComment };

    this.rest.post(url, payload).subscribe({
      next: () => {
        this.sample.status = EvaluationSampleStatus.VALIDATED;
        this.sample.statusComment = this.validationComment;
        this.closeValidationModal();
      },
      error: () => this.showError()
    });
  }

  // ─── Computed / Helpers ──────────────────────────────────────────────────────

  getCoverage(): number {
    return this.sample ? getSampleCoverage(this.sample) : 0;
  }

  getEvaluationRate(type: 'positive' | 'negative'): number {
    return getEvaluationRate(this.sample, type);
  }

  isSampleCompleted(): boolean {
    if (!this.sample) return false;
    const { positiveCount, negativeCount } = this.sample.evaluationsResult;
    return positiveCount + negativeCount >= this.sample.botActionCount;
  }

  getCompletionRate(): number {
    if (!this.sample) return 0;
    const { positiveCount, negativeCount } = this.sample.evaluationsResult;
    const total = positiveCount + negativeCount;
    if (total === 0) return 0;
    return Math.min(100, Math.max(0, Math.round((total / this.sample.botActionCount) * 100)));
  }

  isDialogEvaluated(dialogId: string): boolean {
    const dialog = this.data.dialogs.find((d) => d.id === dialogId);
    return dialog ? !dialog.actions.some((a) => a._evaluation?.status === EvaluationStatus.UNSET) : false;
  }

  // ─── UI Actions ──────────────────────────────────────────────────────────────

  switchDetails(): void {
    this.detailsVisible = !this.detailsVisible;
  }

  paginationChange(): void {
    this.search(this.pagination.start, this.pagination.size, false, true);
  }

  onScroll(): void {
    if (this.loading || this.pagination.end >= this.pagination.total) return;
    this.search(this.pagination.end, this.pagination.size, true);
  }

  validationModal(): void {
    this.validationModalRef = this.nbDialogService.open(this.sampleValidationModal);
  }

  closeValidationModal(): void {
    this.validationModalRef?.close();
  }

  onMissingDialogsWarningAlertClose(): void {
    this.missingDialogsWarning = false;
  }

  exportSample(): void {
    this.loading = true;
    this.actionRefsObservable(0, 1000)
      .pipe(take(1))
      .subscribe({
        next: (result: any) => {
          const filteredResult = this.handleMissingDialogs(result);
          const { dialogs, evaluations } = this.mapResultToData(filteredResult);
          const data = { dialogs, evaluations };
          this.generateReport(data);
          this.loading = false;
        },
        error: () => {
          this.showError();
          this.loading = false;
        }
      });
  }

  async generateReport(data): Promise<void> {
    await generateSampleReport(
      this.stateService.currentApplication.namespace,
      this.stateService.currentApplication.name,
      this.datePipe,
      this.sample,
      data
    );
  }

  // ─── Private helpers ─────────────────────────────────────────────────────────

  private get evaluationBaseUrl(): string {
    return getEvaluationBaseUrl(this.stateService.currentApplication.name);
  }

  private showError(message: string = 'An error occured'): void {
    this.toastrService.danger(message, 'Error', { duration: 5000, status: 'danger' });
  }

  private updatePagination(result: any): void {
    this.pagination.total = result.total;
    this.pagination.end = result.end;
  }

  private handleMissingDialogs(result: any): any {
    result.dialogs.forEach((d) => {
      if (!d.dialog) {
        this.missingDialogIds.add(d.dialogId);
      }
    });

    const filteredDialogs = result.dialogs.filter((d) => d.dialog);
    const filteredResult = {
      actionRefs: [...result.actionRefs],
      dialogs: filteredDialogs
    };

    this.missingDialogsCount = this.missingDialogIds.size;
    this.missingDialogsWarning = this.missingDialogIds.size > 0 ? true : false;

    return filteredResult;
  }

  private mapResultToData(result: any): { dialogs: DialogReport[]; evaluations: any[] } {
    const retrievedEvaluations = [];

    let dialogs = result.dialogs
      .map((report) => DialogReport.fromJSON(report.dialog))
      .map((dialog: DialogReport) => {
        dialog.actions.forEach((action) => {
          if (action.isBotAnswerWithContent()) {
            const evaluation = result.actionRefs.find((e) => e.actionId === action.id);
            if (evaluation) {
              action._evaluation = evaluation.evaluation;
              retrievedEvaluations.push(evaluation.evaluation);
            }
          }
        });
        return dialog;
      });

    return { dialogs, evaluations: retrievedEvaluations };
  }

  // Distinguishes between a first-time evaluation (UNSET → UP/DOWN) and a change of mind (UP ↔ DOWN)
  // because the counter updates differ: a new eval increments one counter, a change swaps the two.
  private applyEvaluationResult(action: ActionReport, evaluation: EvaluationStatus, reason?: string): void {
    const isNewEval = action._evaluation.status === EvaluationStatus.UNSET;
    const wasPositive = action._evaluation.status === EvaluationStatus.UP;

    action._evaluation.status = evaluation;
    if (reason) action._evaluation.reason = reason;

    if (isNewEval) {
      this.updateCountsForNewEval(evaluation);
      this.handlePostEvalScroll(action._evaluation.dialogId);
    } else {
      this.updateCountsForChangedEval(evaluation, wasPositive);
    }
  }

  private updateCountsForNewEval(evaluation: EvaluationStatus): void {
    if (evaluation === EvaluationStatus.UP) this.sample.evaluationsResult.positiveCount++;
    if (evaluation === EvaluationStatus.DOWN) this.sample.evaluationsResult.negativeCount++;
  }

  private updateCountsForChangedEval(evaluation: EvaluationStatus, wasPositive: boolean): void {
    if (evaluation === EvaluationStatus.UP) {
      this.sample.evaluationsResult.positiveCount++;
      this.sample.evaluationsResult.negativeCount--;
    } else if (evaluation === EvaluationStatus.DOWN) {
      this.sample.evaluationsResult.negativeCount++;
      this.sample.evaluationsResult.positiveCount--;
    }
  }

  private handlePostEvalScroll(dialogId: string): void {
    if (!this.isDialogEvaluated(dialogId)) {
      this.scrollToNextUnevaluatedAction(dialogId);
      return;
    }

    if (this.isSampleCompleted()) {
      scrollToPageTop(this.document);
      const message = 'All responses in the sample have been evaluated. You can validate the sample.';
      this.toastrService.success(message, 'All responses evaluated', { duration: 5000, status: 'success' });
    } else {
      setTimeout(() => this.scrollToNextUnCompleteDialog(), 300);
    }
  }

  // On initial load, if the first page contains fewer than 15 dialogs and all are already evaluated,
  // automatically fetch the next page so the user isn't stuck on a nearly-empty screen.
  private handleInitialLoadScroll(): void {
    const hasEnoughDialogs = this.data.dialogs.length >= 15;
    const allPagesLoaded = this.pagination.end >= this.pagination.total;
    if (hasEnoughDialogs || allPagesLoaded) return;

    const allEvaluated = this.data.dialogs.every((d) => this.isDialogEvaluated(d.id));
    if (allEvaluated) {
      this.search(this.pagination.end, this.pagination.size, true, false, true);
    }
  }

  private scrollToNextUnevaluatedAction(dialogId: string) {
    const dialog = this.data.dialogs.find((d) => d.id === dialogId);
    if (dialog) {
      const nextUnevaluatedAction = dialog.actions.find((a) => {
        return a._evaluation?.status === EvaluationStatus.UNSET;
      });

      if (nextUnevaluatedAction) {
        const element = this.document.getElementById(`action-anchor-${nextUnevaluatedAction.id}`);
        if (!element) return;
        window.scrollTo({
          top: element.getBoundingClientRect().top + window.pageYOffset - 20 * 16,
          behavior: 'smooth'
        });
      }
    }
  }

  // Offsets the scroll position by 12rem to account for the sticky menu height,
  // ensuring the target dialog isn't hidden behind it.
  private scrollToNextUnCompleteDialog(): void {
    const dialog = this.data.dialogs.find((d) => d.actions.some((a) => a.isBot() && a._evaluation?.status === EvaluationStatus.UNSET));
    if (!dialog) return;

    const element = this.document.getElementById(`dialog-wrapper-${dialog.id}`);
    if (!element) return;

    window.scrollTo({
      top: element.getBoundingClientRect().top + window.pageYOffset - 12 * 16,
      behavior: 'smooth'
    });
  }
}
