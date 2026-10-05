import { saveAs } from 'file-saver-es';
import { Component, OnInit } from '@angular/core';
import { AddStateDialogComponent } from './add-state/add-state-dialog.component';
import { AddSharedIntentDialogComponent } from './add-shared-intent/add-shared-intent-dialog.component';
import { IntentsFilter } from './intents-filters/intents-filters.component';
import { StateService } from '../../core-nlp/state.service';
import { UserRole } from '../../model/auth';
import { EntityDefinition, Intent, IntentsCategory } from '../../model/nlp';
import { NlpService } from '../../core-nlp/nlp.service';
import { DialogService } from '../../core-nlp/dialog.service';
import { ApplicationService } from '../../core-nlp/applications.service';

import { ChoiceDialogComponent, IntentDialogComponent } from '../../shared/components';
import { getExportFileName } from '../../shared/utils';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'tock-intents',
    templateUrl: './intents.component.html',
    styleUrls: ['./intents.component.scss'],
    standalone: false
})
export class IntentsComponent implements OnInit {
  UserRole = UserRole;
  selectedIntent: Intent;

  intentsCategories: IntentsCategory[];

  constructor(
    public state: StateService,
    private nlp: NlpService,
    private dialog: DialogService,
    private applicationService: ApplicationService,
    private transloco: TranslocoService
  ) {}

  ngOnInit() {
    this.state.currentNamespaceIntentsCategories.subscribe((it) => {
      this.intentsCategories = it;
    });
  }

  filters: IntentsFilter;

  filteredIntents: Intent[];

  filterIntents(filters: IntentsFilter) {
    this.filters = filters;
    this.updateFilteredIntents();
  }

  updateFilteredIntents(): void {
    if (this.filters?.search?.trim().length) {
      let allIntents = [];
      this.intentsCategories.forEach((cat) => {
        allIntents = [...allIntents, ...cat.intents];
      });
      const searchStr = this.filters.search.toLowerCase();
      this.filteredIntents = allIntents.filter((intent) => {
        return intent.label?.toLowerCase().search(searchStr) > -1 || intent.name?.toLowerCase().search(searchStr) > -1;
      });
    } else {
      this.filteredIntents = undefined;
    }
  }

  updateIntent(intent: Intent): void {
    const dialogRef = this.dialog.openDialog(IntentDialogComponent, {
      context: {
        name: intent.name,
        label: intent.label,
        description: intent.description,
        category: intent.category
      }
    });

    dialogRef.onClose.subscribe((result) => {
      if (result?.name) {
        this.nlp
          .saveIntent(
            new Intent(
              intent.name,
              this.state.user.organization,
              [],
              [this.state.currentApplication._id],
              [],
              [],
              result.label,
              result.description,
              result.category,
              intent._id
            )
          )
          .subscribe((_intent) => {
            this.state.updateIntent(_intent);
          });
      }
    });
  }

  deleteIntent(intent: Intent): void {
    const action = this.transloco.translate('common.actions.delete');
    const dialogRef = this.dialog.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('lu.intents.dialog.remove-intent.title', { name: intent.name }),
        subtitle: this.transloco.translate('lu.intents.dialog.remove-intent.subtitle'),
        actions: [
          { actionName: this.transloco.translate('common.actions.cancel'), buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === action.toLowerCase()) {
        this.nlp.removeIntent(this.state.currentApplication, intent).subscribe(
          (_) => {
            this.state.removeIntent(intent);
            this.dialog.notify(
              this.transloco.translate('lu.intents.dialog.remove-intent.success', { name: intent.name }),
              this.transloco.translate('lu.intents.dialog.remove-intent.success-title')
            );
            this.updateFilteredIntents();
          },
          (_) => this.dialog.notify(this.transloco.translate('lu.intents.dialog.remove-intent.failure', { name: intent.name }))
        );
      }
    });
  }

  removeState(event: { intent: Intent; state: string }): void {
    this.nlp.removeState(this.state.currentApplication, event.intent, event.state).subscribe(
      (_) => {
        event.intent.mandatoryStates.splice(event.intent.mandatoryStates.indexOf(event.state), 1);
        this.dialog.notify(
          this.transloco.translate('lu.intents.dialog.remove-state.success', {
            state: event.state,
            intentName: event.intent.name
          }),
          this.transloco.translate('lu.intents.dialog.remove-state.success-title')
        );
      },
      (_) => {
        this.dialog.notify(this.transloco.translate('lu.intents.dialog.remove-state.failure'));
      }
    );
  }

  addState(intent: Intent): void {
    const dialogRef = this.dialog.openDialog(AddStateDialogComponent, {
      context: {
        title: this.transloco.translate('lu.intents.dialog.add-state.title', { intentName: intent.name })
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result && result !== 'cancel') {
        intent.mandatoryStates.push(result.name);
        this.nlp.saveIntent(intent).subscribe(
          (response) => {
            this.dialog.notify(
              this.transloco.translate('lu.intents.dialog.add-state.success', {
                state: response.name,
                intentName: intent.name
              }),
              this.transloco.translate('lu.intents.dialog.add-state.success-title')
            );
          },
          (_) => {
            intent.mandatoryStates.splice(intent.mandatoryStates.length - 1, 1);
            this.dialog.notify(this.transloco.translate('lu.intents.dialog.add-state.failure'));
          }
        );
      }
    });
  }

  removeEntity(event: { intent: Intent; entity: EntityDefinition }): void {
    const entityName = event.entity.qualifiedName(this.state.user);
    const action = this.transloco.translate('common.actions.delete');
    const dialogRef = this.dialog.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('lu.intents.dialog.remove-entity.title', { entityName }),
        subtitle: this.transloco.translate('lu.intents.dialog.remove-entity.subtitle'),
        actions: [
          { actionName: this.transloco.translate('common.actions.cancel'), buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === action.toLowerCase()) {
        this.nlp.removeEntity(this.state.currentApplication, event.intent, event.entity).subscribe((deleted) => {
          this.state.currentApplication.intentById(event.intent._id).removeEntity(event.entity);
          if (deleted) {
            this.state.removeEntityTypeByName(event.entity.entityTypeName);
          }
          this.dialog.notify(
            this.transloco.translate('lu.intents.dialog.remove-entity.success', { entityName }),
            this.transloco.translate('lu.intents.dialog.remove-entity.success-title')
          );
        });
      }
    });
  }

  removeSharedIntent(event: { intent: Intent; intentId: string }): void {
    this.selectedIntent = null;
    this.nlp.removeSharedIntent(this.state.currentApplication, event.intent, event.intentId).subscribe(
      (_) => {
        event.intent.sharedIntents.splice(event.intent.sharedIntents.indexOf(event.intentId), 1);
        this.dialog.notify(
          this.transloco.translate('lu.intents.dialog.remove-shared-intent.success', { intentName: event.intent.name }),
          this.transloco.translate('lu.intents.dialog.remove-shared-intent.success-title')
        );
      },
      (_) => {
        this.dialog.notify(this.transloco.translate('lu.intents.dialog.remove-shared-intent.failure'));
      }
    );
  }

  displayAddSharedIntentDialog(intent: Intent): void {
    this.selectedIntent = intent;
    const dialogRef = this.dialog.openDialog(AddSharedIntentDialogComponent, {
      context: {
        title: this.transloco.translate('lu.intents.dialog.add-shared-intent.title', { intentName: intent.name })
      }
    });

    dialogRef.onClose.subscribe((result) => {
      if (result && result !== 'cancel') {
        this.addSharedIntent(this.selectedIntent, result.intent);
      } else {
        this.selectedIntent = null;
      }
    });
  }

  addSharedIntent(intent: Intent, intentId: string): void {
    if (intent.sharedIntents.indexOf(intentId) === -1) {
      this.selectedIntent = null;
      intent.sharedIntents.push(intentId);
      this.nlp.saveIntent(intent).subscribe(
        (_) => {
          this.dialog.notify(
            this.transloco.translate('lu.intents.dialog.add-shared-intent.success', { intentName: intent.name }),
            this.transloco.translate('lu.intents.dialog.add-shared-intent.success-title')
          );
        },
        (_) => {
          intent.sharedIntents.splice(intent.sharedIntents.length - 1, 1);
          this.dialog.notify(this.transloco.translate('lu.intents.dialog.add-shared-intent.failure'));
        }
      );
    }
  }

  downloadSentencesDump(intent: Intent): void {
    this.applicationService
      .getSentencesDumpForIntent(
        this.state.currentApplication,
        intent,
        this.state.currentLocale,
        this.state.hasRole(UserRole.technicalAdmin)
      )
      .subscribe((blob) => {
        const exportFileName = getExportFileName(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          'Sentences',
          'json',
          intent.name
        );
        saveAs(blob, exportFileName);
        this.dialog.notify(
          this.transloco.translate('lu.intents.dialog.download-sentences-dump.success'),
          this.transloco.translate('lu.intents.dialog.download-sentences-dump.success-title')
        );
      });
  }

  expandedCategory: string = 'default';

  isCategoryExpanded(category: IntentsCategory): boolean {
    return category.category.toLowerCase() === this.expandedCategory.toLowerCase();
  }

  collapsedChange(category: IntentsCategory): void {
    this.expandedCategory = category.category;
  }
}
