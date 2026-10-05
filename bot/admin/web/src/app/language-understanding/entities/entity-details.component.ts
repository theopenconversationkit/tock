import { map } from 'rxjs/operators';
import { Component, Input } from '@angular/core';
import { StateService } from '../../core-nlp/state.service';
import { NlpService } from '../../core-nlp/nlp.service';
import { ApplicationService } from '../../core-nlp/applications.service';
import { EntityDefinition, EntityType } from '../../model/nlp';
import { NbToastrService } from '@nebular/theme';
import { DialogService } from '../../core-nlp/dialog.service';
import { getContrastYIQ } from '../../shared/utils';
import { ChoiceDialogComponent } from '../../shared/components';
import { TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'tock-entity-details',
  templateUrl: './entity-details.component.html',
  styleUrls: ['./entity-details.component.scss']
})
export class EntityDetailsComponent {
  @Input() entity: EntityDefinition;
  @Input() entityType: EntityType;

  getContrastYIQ = getContrastYIQ;

  constructor(
    public state: StateService,
    private nlp: NlpService,
    private toastrService: NbToastrService,
    private dialog: DialogService,
    private applicationService: ApplicationService,
    private transloco: TranslocoService
  ) {}

  findEntityType(): EntityType {
    return this.state.findEntityTypeByName(this.entity.entityTypeName);
  }

  update() {
    this.nlp
      .updateEntityDefinition(this.state.createUpdateEntityDefinitionQuery(this.entity))
      .pipe(map((_) => this.applicationService.reloadCurrentApplication()))
      .subscribe((_) =>
        this.toastrService.show(
          this.transloco.translate('lu.entity-details.entity_updated'),
          this.transloco.translate('lu.entity-details.update_title'),
          { duration: 2000 }
        )
      );
  }

  remove() {
    const action = this.transloco.translate('common.actions.remove');
    let dialogRef = this.dialog.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('lu.entity-details.remove_subentity_title', { entityType: this.entity.entityTypeName }),
        subtitle: this.transloco.translate('lu.entity-details.remove_subentity_confirmation'),
        actions: [
          { actionName: this.transloco.translate('common.actions.cancel'), buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === action.toLowerCase()) {
        this.nlp.removeSubEntity(this.state.currentApplication, this.entityType, this.entity).subscribe(
          (_) => {
            this.state.resetConfiguration();
            this.toastrService.show(
              this.transloco.translate('lu.entity-details.subentity_removed', { entityType: this.entity.entityTypeName }),
              this.transloco.translate('lu.entity-details.remove_subentity_success_title'),
              { duration: 2000 }
            );
          },
          (_) =>
            this.toastrService.show(
              this.transloco.translate('lu.entity-details.remove_subentity_failed', { entityType: this.entity.entityTypeName }),
              this.transloco.translate('lu.entity-details.error_title'),
              { duration: 5000 }
            )
        );
      }
    });
  }

  subEntities(): EntityDefinition[] {
    const entityType = this.findEntityType();
    if (!entityType) {
      return [];
    } else {
      //filter sub entities already seen (avoid direct recursive problem)
      return entityType.subEntities.filter((s) => s.entityTypeName !== entityType.name);
    }
  }
}
