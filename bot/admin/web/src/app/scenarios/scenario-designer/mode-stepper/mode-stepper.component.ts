import { Component, EventEmitter, Input, Output } from '@angular/core';

import { DialogService } from '../../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../../shared/components';
import { isStepValid } from '../../commons/scenario-validation';
import { ScenarioVersion, SCENARIO_MODE } from '../../models';

@Component({
  selector: 'scenario-mode-stepper',
  templateUrl: './mode-stepper.component.html',
  styleUrls: ['./mode-stepper.component.scss']
})
export class ModeStepperComponent {
  @Input() mode: SCENARIO_MODE;
  @Input() scenario!: ScenarioVersion;
  @Output() modeSwitch = new EventEmitter();

  steps = [
    { mode: SCENARIO_MODE.writing, label: 'Writing', icon: 'edit-outline' },
    { mode: SCENARIO_MODE.casting, label: 'Casting', icon: 'people-outline' },
    { mode: SCENARIO_MODE.production, label: 'Production', icon: 'video-outline' },
    { mode: SCENARIO_MODE.publishing, label: 'Publishing', icon: 'film-outline' }
  ];

  constructor(private dialogService: DialogService) {}

  getStepErrorTooltip(mode: SCENARIO_MODE) {
    if (this.mode === mode) return 'Not all conditions are met to pass this stage';
    return 'Not all conditions are met to access this stage';
  }

  displayNotMetConditionToAccesNextStage(mode: SCENARIO_MODE) {
    const currentStageIndex = this.steps.findIndex((s) => s.mode === mode);
    const nextStage = this.steps[currentStageIndex + 1];
    let title;
    if (this.mode === mode) title = 'At least one condition is not met to pass this stage';

    this.displayFirstNotMetCondition(nextStage?.mode || mode, title);
  }

  displayFirstNotMetCondition(mode: SCENARIO_MODE, title = 'At least one condition is not met to access this stage') {
    let reason = this.getStepSequenceValidity(mode);
    this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: title,
        subtitle: reason,
        modalStatus: 'danger',
        actions: [{ actionName: 'ok', buttonStatus: 'default' }]
      }
    });
  }

  switchMode(mode: SCENARIO_MODE) {
    if (!this.isStepSequenceValid(mode)) {
      this.displayFirstNotMetCondition(mode);
    } else {
      this.modeSwitch.emit(mode);
    }
  }

  isStepPassed(mode: SCENARIO_MODE): boolean {
    const keys = Object.keys(SCENARIO_MODE);
    return keys.indexOf(mode) < keys.indexOf(this.mode);
  }

  getStepSequenceValidity(mode: SCENARIO_MODE): string {
    if (mode === SCENARIO_MODE.casting) {
      return isStepValid(this.scenario, SCENARIO_MODE.casting).reason;
    }
    if (mode === SCENARIO_MODE.production) {
      let castingValidity = isStepValid(this.scenario, SCENARIO_MODE.casting);
      if (!castingValidity.valid) return castingValidity.reason;
      let productionValidity = isStepValid(this.scenario, SCENARIO_MODE.production);
      if (!productionValidity.valid) return productionValidity.reason;
    }
    if (mode === SCENARIO_MODE.publishing) {
      let castingValidity = isStepValid(this.scenario, SCENARIO_MODE.casting);
      if (!castingValidity.valid) return castingValidity.reason;
      let productionValidity = isStepValid(this.scenario, SCENARIO_MODE.production);
      if (!productionValidity.valid) return productionValidity.reason;
      let publishingValidity = isStepValid(this.scenario, SCENARIO_MODE.publishing);
      if (!publishingValidity.valid) return publishingValidity.reason;
    }
  }

  isStepSequenceValid(mode: SCENARIO_MODE): boolean {
    switch (mode) {
      case SCENARIO_MODE.writing:
        return true;
      case SCENARIO_MODE.casting:
        return isStepValid(this.scenario, SCENARIO_MODE.casting).valid;
      case SCENARIO_MODE.production:
        return isStepValid(this.scenario, SCENARIO_MODE.casting).valid && isStepValid(this.scenario, SCENARIO_MODE.production).valid;
      case SCENARIO_MODE.publishing:
        return (
          isStepValid(this.scenario, SCENARIO_MODE.casting).valid &&
          isStepValid(this.scenario, SCENARIO_MODE.production).valid &&
          isStepValid(this.scenario, SCENARIO_MODE.publishing).valid
        );
    }
  }

  hasStepError(mode: SCENARIO_MODE): boolean {
    switch (mode) {
      case SCENARIO_MODE.writing:
        return isStepValid(this.scenario, SCENARIO_MODE.casting).valid;
      case SCENARIO_MODE.casting:
        return isStepValid(this.scenario, SCENARIO_MODE.casting).valid && isStepValid(this.scenario, SCENARIO_MODE.production).valid;
      case SCENARIO_MODE.production:
      case SCENARIO_MODE.publishing:
        return (
          isStepValid(this.scenario, SCENARIO_MODE.casting).valid &&
          isStepValid(this.scenario, SCENARIO_MODE.production).valid &&
          isStepValid(this.scenario, SCENARIO_MODE.publishing).valid
        );
    }
  }
}
