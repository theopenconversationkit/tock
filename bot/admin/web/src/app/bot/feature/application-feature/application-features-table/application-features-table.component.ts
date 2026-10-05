import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Feature } from '../../../model/feature';
import { BotService } from '../../../bot-service';
import { BotConfigurationService } from '../../../../core/bot-configuration.service';
import { StateService } from '../../../../core-nlp/state.service';
import { NbDialogService } from '@nebular/theme';
import { CreateFeatureComponent } from '../create-feature/create-feature.component';
import { take } from 'rxjs';
import { ChoiceDialogComponent } from '../../../../shared/components';
import { TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'tock-application-features-table',
  templateUrl: './application-features-table.component.html',
  styleUrls: ['./application-features-table.component.scss']
})
export class ApplicationFeaturesTableComponent {
  @Input() type: 'tock' | 'application';

  @Input() features: Feature[] = [];

  @Output() onRefresh = new EventEmitter<boolean>();

  constructor(
    private state: StateService,
    private botService: BotService,
    private configurationService: BotConfigurationService,
    private nbDialogService: NbDialogService,
    private transloco: TranslocoService
  ) {}

  newFeature(): void {
    this.nbDialogService
      .open(CreateFeatureComponent, {
        context: {
          type: this.type
        }
      })
      .componentRef.instance.onSave.pipe(take(1))
      .subscribe((res) => {
        this.createFeature(res);
      });
  }

  createFeature(feature): void {
    const conf = this.configurationService.findApplicationConfigurationById(feature.botApplicationConfigurationId);

    if (conf) {
      feature.applicationId = conf.applicationId;
    }

    this.botService.addFeature(this.state.currentApplication.name, feature).subscribe((_) => {
      this.onRefresh.emit(true);
    });
  }

  changeStartDate(feature: Feature, newState): void {
    feature.startDate = newState;
    this.update(feature);
  }

  changeEndDate(feature: Feature, newState): void {
    feature.endDate = newState;
    this.update(feature);
  }

  changeGraduation(feature: Feature, event: FocusEvent): void {
    const graduation = (event.target as HTMLInputElement).value;
    if (graduation === '') feature.graduation = undefined;
    else feature.graduation = Number(graduation);
    this.update(feature);
  }

  toggle(feature: Feature, newState): void {
    feature.enabled = newState;
    if (!newState) {
      feature.startDate = null;
      feature.endDate = null;
      feature.graduation = null;
    }
    this.botService.toggleFeature(this.state.currentApplication.name, feature).subscribe();
  }

  update(f: Feature): void {
    this.botService.updateDateAndEnableFeature(this.state.currentApplication.name, f).subscribe((_) => this.onRefresh.emit(true));
  }

  askDeleteFeature(feature: Feature): void {
    const confirmAction = this.transloco.translate('bot.application-features-table.confirmAction');
    const cancelAction = this.transloco.translate('bot.application-features-table.cancelAction');

    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('bot.application-features-table.deleteFeatureTitle'),
        subtitle: this.transloco.translate('bot.application-features-table.deleteFeatureSubtitle', { featureName: feature.name }),
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'basic' },
          { actionName: confirmAction, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result?.toLowerCase() === confirmAction.toLowerCase()) {
        this.deleteFeature(feature);
      }
    });
  }

  deleteFeature(feature: Feature): void {
    this.botService
      .deleteFeature(this.state.currentApplication.name, feature.category, feature.name, feature.applicationId)
      .subscribe((_) => this.onRefresh.emit(true));
  }
}
