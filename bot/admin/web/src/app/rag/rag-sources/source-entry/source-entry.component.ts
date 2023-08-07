import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { ConfirmDialogComponent } from '../../../shared-nlp/confirm-dialog/confirm-dialog.component';
import { getSourceMostRecentRunningIndexingSession } from '../commons/utils';
import { IndexingSession, IndexingSessionTaskTypes, ProcessAdvancement, Source, SourceTypes } from '../models';

export interface TaskDefinition {
  type: IndexingSessionTaskTypes;
  label: string;
  icon: string;
}

@Component({
  selector: 'tock-source-entry',
  templateUrl: './source-entry.component.html',
  styleUrls: ['./source-entry.component.scss']
})
export class SourceEntryComponent implements OnDestroy {
  destroy$ = new Subject();

  @Input() source: Source;

  @Output() onEdit = new EventEmitter<Source>();
  @Output() onDelete = new EventEmitter<Source>();
  @Output() onUpdate = new EventEmitter<Source>();
  @Output() onToggleEnabled = new EventEmitter<Source>();
  @Output() onDeleteIndexingSession = new EventEmitter<{ source: Source; session: IndexingSession }>();
  @Output() onSetIndexingSessionAsCurrent = new EventEmitter<{ source: Source; session: IndexingSession }>();

  sourceTypes = SourceTypes;

  ProcessAdvancement = ProcessAdvancement;

  constructor(private nbDialogService: NbDialogService) {}

  initTaskDefinition = { type: IndexingSessionTaskTypes.initialization, label: 'Initialization', icon: 'clock-outline' };

  tasksDefinitions = [
    { type: IndexingSessionTaskTypes.crawling, label: 'Source exploration', icon: 'compass-outline' },
    { type: IndexingSessionTaskTypes.fetching, label: 'Data extraction', icon: 'code-download-outline' },
    { type: IndexingSessionTaskTypes.chunking, label: 'Text processing', icon: 'scissors-outline' },
    { type: IndexingSessionTaskTypes.embeddings, label: 'Word embedding', icon: 'menu-arrow-outline' }
  ];

  getRunningIndexingSessionTasks(): TaskDefinition[] {
    const indexingSession = getSourceMostRecentRunningIndexingSession(this.source);
    const tasksDescriptions = [this.initTaskDefinition];
    indexingSession.tasks?.forEach((task) => {
      tasksDescriptions.push(this.tasksDefinitions.find((td) => td.type === task.type));
    });
    return tasksDescriptions;
  }

  isCurrentIndexingSession(session: IndexingSession): boolean {
    return session.id === this.source.current_indexing_session_id;
  }

  hasIndexingSessionRunning(): boolean {
    return getSourceMostRecentRunningIndexingSession(this.source) != undefined;
  }

  isSessionComplete(session: IndexingSession) {
    return session.status === ProcessAdvancement.complete;
  }

  getCurrentIndexingSession(): IndexingSession {
    return this.source.indexing_sessions?.find((session) => session.id === this.source.current_indexing_session_id);
  }

  isStepComplete(type: IndexingSessionTaskTypes): boolean {
    const currentSession = getSourceMostRecentRunningIndexingSession(this.source);
    if (type === IndexingSessionTaskTypes.initialization) {
      return currentSession.tasks?.some((task) => task.status === ProcessAdvancement.running);
    }
    return currentSession.tasks.find((task) => task.type === type).status === ProcessAdvancement.complete;
  }

  isStepRunning(type: IndexingSessionTaskTypes): boolean {
    const currentSession = getSourceMostRecentRunningIndexingSession(this.source);
    if (type === IndexingSessionTaskTypes.initialization) {
      return !currentSession.tasks?.some((task) => task.status === ProcessAdvancement.running);
    }
    return currentSession.tasks.find((task) => task.type === type).status === ProcessAdvancement.running;
  }

  setSessionAsCurrent(session: IndexingSession): void {
    const confirmLabel = 'Set as current';
    const dialogRef = this.nbDialogService.open(ConfirmDialogComponent, {
      context: {
        title: `Set session "${session.id}" as current data source`,
        subtitle: 'Are you sure?',
        action: confirmLabel
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result?.toLowerCase() === confirmLabel.toLowerCase()) {
        this.onSetIndexingSessionAsCurrent.emit({ source: this.source, session });
      }
    });
  }

  deleteSession(session: IndexingSession): void {
    const confirmLabel = 'Remove';
    const dialogRef = this.nbDialogService.open(ConfirmDialogComponent, {
      context: {
        title: `Remove the session "${session.id}"`,
        subtitle: 'Are you sure?',
        action: confirmLabel
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result?.toLowerCase() === confirmLabel.toLowerCase()) {
        this.onDeleteIndexingSession.emit({ source: this.source, session });
      }
    });
  }

  editSource(event: PointerEvent): void {
    event.stopPropagation();
    this.onEdit.emit(this.source);
  }

  deleteSource(event: PointerEvent): void {
    event.stopPropagation();
    this.onDelete.emit(this.source);
  }

  updateSource(event: PointerEvent): void {
    event.stopPropagation();
    this.onUpdate.emit(this.source);
  }

  toggleSourceEnabled(): void {
    this.onToggleEnabled.emit(this.source);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
