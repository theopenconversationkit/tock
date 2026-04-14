import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { DialogService } from '../../../core-nlp/dialog.service';
import { BotApplicationConfiguration } from '../../../core/model/configuration';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { DatasetCreateComponent } from '../dataset-create/dataset-create.component';

import { Dataset } from '../models';
import { DatasetsService } from '../services/datasets.service';
import { NbDialogRef, NbDialogService, NbToastrService } from '@nebular/theme';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { FileValidators } from '../../../shared/validators';
import { readFileAsText } from '../../../shared/utils';

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

  @ViewChild('importModal') importModal: TemplateRef<any>;

  constructor(
    private botConfiguration: BotConfigurationService,
    private dialogService: DialogService,
    private datasetsService: DatasetsService,
    private nbDialogService: NbDialogService,
    private toastrService: NbToastrService
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

  importModalRef: NbDialogRef<any>;

  importDataset(): void {
    this.isImportSubmitted = false;
    this.importForm.reset();
    this.importModalRef = this.nbDialogService.open(this.importModal);
  }

  closeImportModal(): void {
    this.importModalRef.close();
  }

  importForm: FormGroup = new FormGroup({
    fileSource: new FormControl<File[]>([], {
      nonNullable: true,
      validators: [Validators.required, FileValidators.mimeTypeSupported(['application/json'])]
    })
  });

  isImportSubmitted: boolean = false;

  get fileSource(): FormControl {
    return this.importForm.get('fileSource') as FormControl;
  }

  get canSaveImport(): boolean {
    return this.isImportSubmitted ? this.importForm.valid : this.importForm.dirty;
  }

  submitImportDataset(): void {
    this.isImportSubmitted = true;
    if (this.canSaveImport) {
      this.loading = true;

      const file = this.fileSource.value[0];

      readFileAsText(file).then((fileContent) => {
        try {
          const importedData = JSON.parse(fileContent.data);

          // JSON structure validation
          if (
            !importedData.name ||
            !importedData.description ||
            !Array.isArray(importedData.questions) ||
            !importedData.questions.every((q: any) => typeof q.question === 'string' && typeof q.groundTruth === 'string')
          ) {
            this.toastrService.show(
              `The file must contain a 'name', 'description', and an array of 'questions' with 'question' and 'groundTruth' fields.`,
              'Invalid dataset format',
              {
                duration: 8000,
                status: 'danger'
              }
            );
            this.loading = false;
            return;
          }

          const newDataset: Pick<Dataset, 'name' | 'description' | 'questions'> = {
            name: importedData.name,
            description: importedData.description,
            questions: importedData.questions.map((q: { question: string; groundTruth: string }) => ({
              question: q.question,
              groundTruth: q.groundTruth
            }))
          };

          this.datasetsService
            .createDataset(newDataset)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (createdDataset) => {
                this.toastrService.show(`Dataset "${createdDataset.name}" imported successfully!`, 'Success', {
                  duration: 4000,
                  status: 'success'
                });
                this.closeImportModal();
                this.loading = false;
              },
              error: (err) => {
                this.toastrService.show(`Failed to import dataset: ${err.message || 'Unknown error'}`, 'Error', {
                  duration: 6000,
                  status: 'danger'
                });
                this.loading = false;
              }
            });
        } catch (e) {
          this.toastrService.show('The file is not a valid JSON.', 'Invalid JSON', { duration: 6000, status: 'danger' });
          this.loading = false;
        }
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
