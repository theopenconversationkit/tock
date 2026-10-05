import { Component, OnDestroy, OnInit } from '@angular/core';
import { catchError, of, Subject, take, takeUntil } from 'rxjs';
import { Router } from '@angular/router';
import { DialogService } from '../../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../../shared/components';
import { SampleCreateComponent } from '../sample-create/sample-create.component';
import { EvaluationSampleDefinition, EvaluationSampleStatus } from '../models';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { StateService } from '../../../core-nlp/state.service';
import { getEvaluationBaseUrl } from '../utils';
import { NbToastrService } from '@nebular/theme';
import { BotApplicationConfiguration } from '../../../core/model/configuration';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'tock-samples-board',
  templateUrl: './samples-board.component.html',
  styleUrl: './samples-board.component.scss'
})
export class SamplesBoardComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();
  loading: boolean = false;
  samples: EvaluationSampleDefinition[];
  evaluationSampleStatus = EvaluationSampleStatus;

  configurations: BotApplicationConfiguration[];

  constructor(
    private botConfiguration: BotConfigurationService,
    private router: Router,
    private dialogService: DialogService,
    private rest: RestService,
    private stateService: StateService,
    private toastrService: NbToastrService,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      this.configurations = confs;
      if (confs.length) {
        this.fetchEvaluations();
      }
    });
  }

  fetchEvaluations(): void {
    this.loading = true;
    const url = getEvaluationBaseUrl(this.stateService.currentApplication.name);
    this.rest
      .get(url, (evaluations: any) => evaluations)
      .pipe(
        catchError(() => {
          this.loading = false;
          this.toastrService.danger(
            this.transloco.translate('quality.samples-board.an_error_occurred'),
            this.transloco.translate('quality.samples-board.error_title'),
            {
              duration: 5000,
              status: 'danger'
            }
          );

          return of([]);
        })
      )
      .subscribe((res) => {
        this.samples = res;
        this.loading = false;
      });
  }

  getStatusInfo(status: EvaluationSampleStatus): { text: string; status: 'text-info' | 'text-success' } {
    switch (status) {
      case EvaluationSampleStatus.IN_PROGRESS:
        return { text: this.transloco.translate('quality.samples-board.in_progress_status'), status: 'text-info' };
      case EvaluationSampleStatus.VALIDATED:
        return { text: this.transloco.translate('quality.samples-board.validated_status'), status: 'text-success' };
    }
  }

  getScore(sample: EvaluationSampleDefinition): { ok: number; percentage: number } {
    return {
      ok: sample.evaluationsResult.positiveCount,
      percentage: sample.botActionCount > 0 ? Math.round((sample.evaluationsResult.positiveCount / sample.botActionCount) * 100) : 0
    };
  }

  createSample(): void {
    const dialogRef = this.dialogService.openDialog(SampleCreateComponent, {
      context: {}
    });
  }

  navigateToDetail(id: string): void {
    this.router.navigate(['/quality/samples/detail', id]);
  }

  confirmDeleteSample(sample: EvaluationSampleDefinition): void {
    const action = this.transloco.translate('common.actions.delete');
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('quality.samples-board.delete_sample_dialog_title'),
        subtitle: this.transloco.translate('quality.samples-board.delete_sample_dialog_message', { sampleName: sample.name }),
        modalStatus: 'danger',
        actions: [
          { actionName: this.transloco.translate('common.actions.cancel'), buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === action.toLowerCase()) {
        this.deleteSample(sample);
      }
    });
  }

  deleteSample(sample: EvaluationSampleDefinition): void {
    const url = `${getEvaluationBaseUrl(this.stateService.currentApplication.name)}/${sample._id}`;
    this.loading = true;
    this.rest
      .delete(url)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.samples = this.samples.filter((s) => s._id !== sample._id);
          this.toastrService.success(
            this.transloco.translate('quality.samples-board.sample_deleted_message'),
            this.transloco.translate('quality.samples-board.success_title'),
            {
              duration: 5000,
              status: 'success'
            }
          );
          this.loading = false;
        },
        error: () => {
          this.toastrService.danger(
            this.transloco.translate('quality.samples-board.an_error_occurred'),
            this.transloco.translate('quality.samples-board.error_title'),
            {
              duration: 5000,
              status: 'danger'
            }
          );
          this.loading = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
