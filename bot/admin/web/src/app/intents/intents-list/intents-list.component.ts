import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { StateService } from '../../core-nlp/state.service';
import { EntityDefinition, Intent } from '../../model/nlp';
import { UserRole } from '../../model/auth';
import { NbDialogService } from '@nebular/theme';
import { IntentStoryDetailsComponent } from '../../shared/components';
import { getContrastYIQ } from '../../shared/utils';

@Component({
  selector: 'tock-intents-list',
  templateUrl: './intents-list.component.html',
  styleUrls: ['./intents-list.component.scss']
})
export class IntentsListComponent implements OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  UserRole = UserRole;

  @Input() intents: Intent[];

  @Output() onRemoveEntity = new EventEmitter();
  @Output() onRemoveSharedIntent = new EventEmitter();
  @Output() onDisplayAddSharedIntentDialog = new EventEmitter();
  @Output() onRemoveState = new EventEmitter();
  @Output() onAddState = new EventEmitter();
  @Output() onUpdateIntent = new EventEmitter();
  @Output() onDownloadSentencesDump = new EventEmitter();
  @Output() onDeleteIntent = new EventEmitter();

  getContrastYIQ = getContrastYIQ;

  constructor(public state: StateService, private nbDialogService: NbDialogService) {}

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  removeEntity(intent: Intent, entity: EntityDefinition): void {
    this.onRemoveEntity.emit({ intent, entity });
  }

  removeSharedIntent(intent: Intent, intentId: string): void {
    this.onRemoveSharedIntent.emit({ intent, intentId });
  }

  displayAddSharedIntentDialog(intent: Intent): void {
    this.onDisplayAddSharedIntentDialog.emit(intent);
  }

  removeState(intent: Intent, state: string): void {
    this.onRemoveState.emit({ intent, state });
  }

  addState(intent: Intent): void {
    this.onAddState.emit(intent);
  }

  updateIntent(intent: Intent): void {
    this.onUpdateIntent.emit(intent);
  }

  downloadSentencesDump(intent: Intent): void {
    this.onDownloadSentencesDump.emit(intent);
  }

  deleteIntent(intent: Intent): void {
    this.onDeleteIntent.emit(intent);
  }

  displayIntentStoryDetails(intent: Intent) {
    const modal = this.nbDialogService.open(IntentStoryDetailsComponent, {
      context: {
        intentId: intent._id
      }
    });
  }
}
