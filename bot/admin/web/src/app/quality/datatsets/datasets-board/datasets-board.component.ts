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
import { TranslocoService } from '@jsverse/transloco';
import { saveAs } from 'file-saver-es';
import { buildCsvTemplate, csvToQuestions, CsvParseError } from '../dataset-csv';

export type DatasetSortField = 'name' | 'questions' | 'runs' | 'lastRun';
export type SortDirection = 'asc' | 'desc';

type ImportableDataset = Parameters<DatasetsService['createDataset']>[0];

@Component({
  selector: 'tock-datasets-board',
  templateUrl: './datasets-board.component.html',
  styleUrl: './datasets-board.component.scss',
  standalone: false
})
export class DatasetsBoardComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();
  loading: boolean = true;

  configurations: BotApplicationConfiguration[];
  datasets: Dataset[];

  sortField: DatasetSortField = 'lastRun';
  sortDirection: SortDirection = 'desc';

  @ViewChild('importModal') importModal: TemplateRef<any>;

  constructor(
    private botConfiguration: BotConfigurationService,
    private dialogService: DialogService,
    private datasetsService: DatasetsService,
    private nbDialogService: NbDialogService,
    private toastrService: NbToastrService,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    this.datasetsService.datasets$.pipe(takeUntil(this.destroy$)).subscribe((datasets) => {
      this.datasets = datasets;
    });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      this.configurations = confs;
      if (confs.length) this.fetchDatasets();
    });

    // CSV files carry no name/description: those become required inputs in the modal.
    this.fileSource.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this._syncImportMetadataValidators();
    });
  }

  fetchDatasets(): void {
    this.datasetsService
      .getDatasets()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => (this.loading = false));
  }

  get sortedDatasets(): Dataset[] {
    if (!this.datasets?.length) return [];

    return [...this.datasets].sort((a, b) => {
      let cmp = 0;

      switch (this.sortField) {
        case 'name':
          cmp = a.name.localeCompare(b.name);
          break;
        case 'questions':
          cmp = a.questions.length - b.questions.length;
          break;
        case 'runs':
          cmp = a.runs.length - b.runs.length;
          break;
        case 'lastRun': {
          const aTime = a.runs.length ? Math.max(...a.runs.map((r) => new Date(r.startTime).getTime())) : 0;
          const bTime = b.runs.length ? Math.max(...b.runs.map((r) => new Date(r.startTime).getTime())) : 0;
          cmp = aTime - bTime;
          break;
        }
      }

      return this.sortDirection === 'asc' ? cmp : -cmp;
    });
  }

  setSort(field: DatasetSortField): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = field === 'name' ? 'asc' : 'desc';
    }
  }

  trackById(_index: number, dataset: Dataset): string {
    return dataset.id;
  }

  createDataset(): void {
    this.dialogService.openDialog(DatasetCreateComponent);
  }

  // ---------------------------------------------------------------- Import

  importModalRef: NbDialogRef<any>;

  importForm: FormGroup = new FormGroup({
    fileSource: new FormControl<File[]>([], {
      nonNullable: true,
      validators: [Validators.required, FileValidators.extensionSupported(['json', 'csv'])]
    }),
    name: new FormControl<string>('', { nonNullable: true }),
    description: new FormControl<string>('', { nonNullable: true })
  });

  isImportSubmitted: boolean = false;

  get fileSource(): FormControl {
    return this.importForm.get('fileSource') as FormControl;
  }

  get importName(): FormControl {
    return this.importForm.get('name') as FormControl;
  }

  get importDescription(): FormControl {
    return this.importForm.get('description') as FormControl;
  }

  get importedFile(): File | undefined {
    return this.fileSource.value?.[0];
  }

  get isCsvImport(): boolean {
    return !!this.importedFile && /\.csv$/i.test(this.importedFile.name);
  }

  get canSaveImport(): boolean {
    return this.isImportSubmitted ? this.importForm.valid : this.importForm.dirty;
  }

  importDataset(): void {
    this.isImportSubmitted = false;
    this.importForm.reset();
    this._syncImportMetadataValidators();
    this.importModalRef = this.nbDialogService.open(this.importModal);
  }

  closeImportModal(): void {
    this.importModalRef.close();
  }

  downloadCsvTemplate(): void {
    const blob = new Blob([buildCsvTemplate()], { type: 'text/csv;charset=utf-8' });
    saveAs(blob, 'dataset-template.csv');
  }

  private _syncImportMetadataValidators(): void {
    if (this.isCsvImport) {
      this.importName.setValidators([Validators.required, Validators.minLength(5), Validators.maxLength(100)]);
      this.importDescription.setValidators([Validators.maxLength(750)]);

      if (!this.importName.value) {
        this.importName.setValue(this.importedFile.name.replace(/\.csv$/i, '').slice(0, 100), { emitEvent: false });
      }
    } else {
      this.importName.clearValidators();
      this.importDescription.clearValidators();
    }

    this.importName.updateValueAndValidity({ emitEvent: false });
    this.importDescription.updateValueAndValidity({ emitEvent: false });
  }

  submitImportDataset(): void {
    this.isImportSubmitted = true;
    if (!this.canSaveImport) return;

    this.loading = true;

    readFileAsText(this.importedFile).then((fileContent) => {
      try {
        const newDataset = this.isCsvImport ? this._parseCsvImport(fileContent.data) : this._parseJsonImport(fileContent.data);
        this._persistImportedDataset(newDataset);
      } catch (e) {
        this._toastImportError(e);
        this.loading = false;
      }
    });
  }

  private _parseJsonImport(raw: string): ImportableDataset {
    const importedData = JSON.parse(raw); // throws SyntaxError on malformed JSON

    if (
      !importedData.name ||
      typeof importedData.description !== 'string' ||
      !Array.isArray(importedData.questions) ||
      !importedData.questions.every(
        (q: any) => typeof q.question === 'string' && typeof q.groundTruth === 'string' && q.question.trim() !== ''
      )
    ) {
      throw new TypeError('INVALID_DATASET_FORMAT');
    }

    return {
      name: importedData.name,
      description: importedData.description,
      questions: importedData.questions.map((q: { question: string; groundTruth: string }) => ({
        question: q.question,
        groundTruth: q.groundTruth
      }))
    };
  }

  private _parseCsvImport(raw: string): ImportableDataset {
    return {
      name: this.importName.value.trim(),
      description: this.importDescription.value?.trim() ?? '',
      questions: csvToQuestions(raw) // throws CsvParseError
    };
  }

  private _persistImportedDataset(newDataset: ImportableDataset): void {
    this.datasetsService
      .createDataset(newDataset)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (createdDataset) => {
          this.toastrService.show(
            this.transloco.translate('quality.datasets-board.dataset_imported_success_message', { name: createdDataset.name }),
            this.transloco.translate('quality.datasets-board.success_title'),
            { duration: 4000, status: 'success' }
          );
          this.closeImportModal();
          this.loading = false;
        },
        error: (err) => {
          this.toastrService.show(
            this.transloco.translate('quality.datasets-board.dataset_import_failed_message', {
              error: err.message || this.transloco.translate('quality.datasets-board.unknown_error')
            }),
            this.transloco.translate('quality.datasets-board.error_title'),
            { duration: 6000, status: 'danger' }
          );
          this.loading = false;
        }
      });
  }

  private _toastImportError(e: unknown): void {
    let messageKey: string;
    let titleKey: string;

    if (e instanceof CsvParseError) {
      messageKey = `quality.datasets-board.csv_error_${e.code.toLowerCase()}`;
      titleKey = 'quality.datasets-board.invalid_dataset_format_title';
    } else if (e instanceof SyntaxError) {
      messageKey = 'quality.datasets-board.invalid_json_message';
      titleKey = 'quality.datasets-board.invalid_json_title';
    } else {
      messageKey = 'quality.datasets-board.invalid_dataset_format_message';
      titleKey = 'quality.datasets-board.invalid_dataset_format_title';
    }

    this.toastrService.show(this.transloco.translate(messageKey), this.transloco.translate(titleKey), {
      duration: 6000,
      status: 'danger'
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
