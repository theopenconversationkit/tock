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
    private toastrService: NbToastrService
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
          this.toastrService.danger('An error occured', 'Error', {
            duration: 5000,
            status: 'danger'
          });

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
        return { text: 'In Progress', status: 'text-info' };
      case EvaluationSampleStatus.VALIDATED:
        return { text: 'Validated', status: 'text-success' };
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
    const action = 'delete';
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: 'Delete an evaluation sample',
        subtitle: `Are you sure you want to delete the sample "${sample.name}" ?`,
        modalStatus: 'danger',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) {
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
          this.toastrService.success('Sample deleted', 'Success', {
            duration: 5000,
            status: 'success'
          });
          this.loading = false;
        },
        error: () => {
          this.toastrService.danger('An error occured', 'Error', {
            duration: 5000,
            status: 'danger'
          });
          this.loading = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
