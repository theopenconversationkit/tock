import { Component, OnDestroy, OnInit } from '@angular/core';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { distinctUntilChanged, Subject, take, takeUntil } from 'rxjs';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { ConfirmDialogComponent } from '../../shared-nlp/confirm-dialog/confirm-dialog.component';
import { getSourceMostRecentRunningIndexingSession } from './commons/utils';

import { IndexingSession, ProcessAdvancement, Source, SourceImportParams, SourceTypes } from './models';
import { NewSourceComponent } from './new-source/new-source.component';
import { SourceImportComponent } from './source-import/source-import.component';
import { SourceManagementService } from './source-management.service';
import { SourceNormalizationCsvComponent } from './source-normalization/csv/source-normalization-csv.component';
import { SourceNormalizationJsonComponent } from './source-normalization/json/source-normalization-json.component';

@Component({
  selector: 'tock-rag-sources-board',
  templateUrl: './rag-sources-board.component.html',
  styleUrls: ['./rag-sources-board.component.scss']
})
export class RagSourcesBoardComponent implements OnInit, OnDestroy {
  destroy$ = new Subject();

  configurations: BotApplicationConfiguration[];

  sourceTypes = SourceTypes;

  sources: Source[];

  constructor(
    private botConfiguration: BotConfigurationService,
    private nbDialogService: NbDialogService,
    private sourcesService: SourceManagementService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs: BotApplicationConfiguration[]) => {
      this.configurations = confs;
      if (confs.length) {
        this.loadSources();
      }
    });
  }

  runningSessionsWatcher: { source: Source; session: IndexingSession }[];

  loadSources(): void {
    this.sourcesService
      .getSources()
      .pipe(distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((sources: Source[]) => {
        this.sources = sources;
        this.listRunningSessions();
      });
  }

  listRunningSessions() {
    this.runningSessionsWatcher = [];
    this.sources.forEach((source) => {
      const runningSession = getSourceMostRecentRunningIndexingSession(source);
      if (runningSession) {
        this.runningSessionsWatcher.push({
          source: source,
          session: runningSession
        });
      }
    });

    if (this.runningSessionsWatcher.length) {
      this.watchRunningSessions();
    }
  }

  watchRunningSessions() {
    let RunningProcesses = false;
    this.runningSessionsWatcher.forEach((rs) => {
      this.sourcesService
        .getIndexingSession(rs.source, rs.session)
        .pipe(take(1))
        .subscribe((indexingSession) => {
          if ([ProcessAdvancement.pristine, ProcessAdvancement.running].includes(indexingSession.status)) {
            RunningProcesses = true;
          }
        });
    });

    if (RunningProcesses) {
      setTimeout(() => {
        this.watchRunningSessions();
      }, 200);
    }
  }

  hasSessionRunning(source: Source) {
    return getSourceMostRecentRunningIndexingSession(source);
  }

  addSource(): void {
    const modal = this.nbDialogService.open(NewSourceComponent);
    modal.componentRef.instance.onSave.subscribe((form) => {
      this.sourcesService.postSource(form).subscribe((newSource) => {
        this.toastrService.success(`New source succesfully created`, 'Success', {
          duration: 5000,
          status: 'success'
        });
      });
    });
  }

  editSource(source: Source): void {
    const modal = this.nbDialogService.open(NewSourceComponent, {
      context: {
        source: source
      }
    });
    modal.componentRef.instance.onSave.subscribe((form) => {
      this.sourcesService.updateSource(form).subscribe((modifiedSource) => {
        this.toastrService.success(`Source succesfully updated`, 'Success', {
          duration: 5000,
          status: 'success'
        });
      });
    });
  }

  deleteSource(source: Source): void {
    const actionLabel = 'Remove';
    const dialogRef = this.nbDialogService.open(ConfirmDialogComponent, {
      context: {
        title: `Remove the source '${source.name}'`,
        subtitle: 'Are you sure?',
        action: actionLabel
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === actionLabel.toLowerCase()) {
        this.sourcesService.deleteSource(source.id).subscribe((res) => {
          this.toastrService.success(`Source succesfully deleted`, 'Success', {
            duration: 5000,
            status: 'success'
          });
        });
      }
    });
  }

  toggleEnabledSource(source: Source): void {
    this.sourcesService.updateSource({ id: source.id, enabled: !source.enabled }).subscribe((modifiedSource) => {
      this.toastrService.success(`Source succesfully updated`, 'Success', {
        duration: 5000,
        status: 'success'
      });
    });
  }

  deleteIndexingSession(args: { source: Source; session: IndexingSession }): void {
    this.sourcesService.deleteIndexingSession(args.source, args.session).subscribe((res) => {
      this.toastrService.success(`Indexing session succesfully deleted`, 'Success', {
        duration: 5000,
        status: 'success'
      });
    });
  }

  setIndexingSessionAsCurrent(args: { source: Source; session: IndexingSession }): void {
    this.sourcesService.updateSource({ id: args.source.id, current_indexing_session_id: args.session.id }).subscribe((res) => {
      this.toastrService.success(`Indexing session succesfully deleted`, 'Success', {
        duration: 5000,
        status: 'success'
      });
    });
  }

  updateSource(source: Source): void {
    if (source.source_type === SourceTypes.remote) {
      this.confirmCrawlSource(source);
    }

    if (source.source_type === SourceTypes.file) {
      this.importSource(source);
    }
  }

  confirmCrawlSource(source: Source): void {
    const actionLabel = 'Update';
    const dialogRef = this.nbDialogService.open(ConfirmDialogComponent, {
      context: {
        title: `Update the source '${source.name}'`,
        subtitle: 'Are you sure?',
        action: actionLabel
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result?.toLowerCase() === actionLabel.toLowerCase()) {
        this.postIndexingSession(source);
      }
    });
  }

  postIndexingSession(source: Source, data?: SourceImportParams) {
    this.sourcesService.postIndexingSession(source).subscribe((indexingSession) => {
      this.listRunningSessions();
      this.toastrService.success(`Source update successfully launched`, 'Success', {
        duration: 5000,
        status: 'success'
      });
    });
  }

  importSource(source: Source): void {
    const modal = this.nbDialogService.open(SourceImportComponent, {
      context: {
        source: source
      }
    });
    modal.componentRef.instance.onImport.subscribe((result) => {
      source.source_parameters.file_format = result.fileFormat;
      source.rawData = result.data;
      this.normalizeSource(source);
    });
  }

  normalizeSource(source: Source): void {
    let modal;
    if (source.source_parameters.file_format === 'csv') {
      modal = this.nbDialogService.open(SourceNormalizationCsvComponent, {
        context: {
          source: source
        }
      });
    } else if (source.source_parameters.file_format === 'json') {
      modal = this.nbDialogService.open(SourceNormalizationJsonComponent, {
        context: {
          source: source
        }
      });
    }

    modal.componentRef.instance.onNormalize.subscribe((data: SourceImportParams) => {
      this.postIndexingSession(source, data);
      modal.close();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
