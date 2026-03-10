import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { DialogService } from '../../../core-nlp/dialog.service';
import { BotApplicationConfiguration } from '../../../core/model/configuration';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { DatasetCreateComponent } from '../dataset-create/dataset-create.component';

import { Dataset } from '../models';
import { DatasetsService } from '../services/datasets.service';

@Component({
  selector: 'tock-datasets-board',
  templateUrl: './datasets-board.component.html',
  styleUrl: './datasets-board.component.scss'
})
export class DatasetsBoardComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();
  loading: boolean = true;

  configurations: BotApplicationConfiguration[];
  datasets: Dataset[];

  constructor(
    private botConfiguration: BotConfigurationService,
    private dialogService: DialogService,
    private datasetsService: DatasetsService
  ) {}

  ngOnInit(): void {
    this.datasetsService.datasets$.pipe(takeUntil(this.destroy$)).subscribe((datasets) => {
      this.datasets = datasets;
    });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      this.configurations = confs;
      if (confs.length) this.fetchDatasets();
    });
  }

  fetchDatasets(): void {
    const { namespace, applicationId: botId } = this.configurations[0];

    this.datasetsService
      .getDatasets()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => (this.loading = false));
  }

  trackById(_index: number, dataset: Dataset): string {
    return dataset.id;
  }

  createDataset(): void {
    this.dialogService.openDialog(DatasetCreateComponent);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
