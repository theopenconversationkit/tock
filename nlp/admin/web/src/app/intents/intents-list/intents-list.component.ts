import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { StateService } from '../../core-nlp/state.service';
import { EntityDefinition, Intent } from '../../model/nlp';
import { UserRole } from '../../model/auth';
import { NbDialogService } from '@nebular/theme';

@Component({
  selector: 'tock-intents-list',
  templateUrl: './intents-list.component.html',
  styleUrls: ['./intents-list.component.scss']
})
export class IntentsListComponent implements OnInit, OnDestroy {
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

  constructor(public state: StateService, private nbDialogService: NbDialogService) {}

  ngOnInit(): void {}

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

  // To share with Scenario's version after merge
  getContrastYIQ(hexcolor: string): '' | 'black' | 'white' {
    if (!hexcolor) return '';
    hexcolor = hexcolor.replace('#', '');
    let r = parseInt(hexcolor.substring(0, 2), 16);
    let g = parseInt(hexcolor.substring(2, 4), 16);
    let b = parseInt(hexcolor.substring(4, 6), 16);
    let yiq = (r * 299 + g * 587 + b * 114) / 1000;
    return yiq >= 128 ? 'black' : 'white';
  }

  // To be restored after bot/nlp merge
  // displayIntentStoryDetails(intent: Intent) {
  //   const modal = this.nbDialogService.open(IntentStoryDetailsComponent, {
  //     context: {
  //       intentId: intent._id
  //     }
  //   });
  // }
}
