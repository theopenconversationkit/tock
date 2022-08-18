import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DialogService } from '../../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../../shared/choice-dialog/choice-dialog.component';
import { isStepValid } from '../../commons/scenario-validation';
import { Scenario, SCENARIO_MODE } from '../../models';

@Component({
  selector: 'scenario-mode-stepper',
  templateUrl: './mode-stepper.component.html',
  styleUrls: ['./mode-stepper.component.scss']
})
export class ModeStepperComponent {
  readonly SCENARIO_MODE = SCENARIO_MODE;
  @Input() mode: SCENARIO_MODE;
  @Input() scenario!: Scenario;
  @Output() modeSwitch = new EventEmitter();

  constructor(private dialogService: DialogService) {}

  switchMode(mode: SCENARIO_MODE) {
    if (!this.isStepSequenceValid(mode)) {
      let reason = this.getStepSequenceValidity(mode, true);

      this.dialogService.openDialog(ChoiceDialogComponent, {
        context: {
          title: `At least one condition is not met to access this stage`,
          subtitle: reason,
          modalStatus: 'danger',
          actions: [{ actionName: 'ok', buttonStatus: 'default' }]
        }
      });
    } else {
      this.modeSwitch.emit(mode);
    }
  }

  isStepPassed(mode: SCENARIO_MODE): boolean {
    const keys = Object.keys(SCENARIO_MODE);
    return keys.indexOf(mode) < keys.indexOf(this.mode);
  }

  getStepSequenceValidity(mode: SCENARIO_MODE, isSwitchAction: boolean = false): string {
    if (mode === SCENARIO_MODE.casting) {
      return isStepValid(this.scenario, SCENARIO_MODE.casting, isSwitchAction).reason;
    }
    if (mode === SCENARIO_MODE.production) {
      let castingValidity = isStepValid(this.scenario, SCENARIO_MODE.casting, isSwitchAction);
      if (!castingValidity.valid) return castingValidity.reason;
      let productionValidity = isStepValid(this.scenario, SCENARIO_MODE.production, isSwitchAction);
      if (!productionValidity.valid) return productionValidity.reason;
    }
    if (mode === SCENARIO_MODE.publishing) {
      let castingValidity = isStepValid(this.scenario, SCENARIO_MODE.casting, isSwitchAction);
      if (!castingValidity.valid) return castingValidity.reason;
      let productionValidity = isStepValid(this.scenario, SCENARIO_MODE.production, isSwitchAction);
      if (!productionValidity.valid) return productionValidity.reason;
      let publishingValidity = isStepValid(this.scenario, SCENARIO_MODE.publishing, isSwitchAction);
      if (!publishingValidity.valid) return publishingValidity.reason;
    }
  }

  isStepSequenceValid(mode: SCENARIO_MODE): boolean {
    if (mode === SCENARIO_MODE.writing) return true;

    if (mode === SCENARIO_MODE.casting) {
      return isStepValid(this.scenario, SCENARIO_MODE.casting).valid;
    }
    if (mode === SCENARIO_MODE.production) {
      return (
        isStepValid(this.scenario, SCENARIO_MODE.casting).valid &&
        isStepValid(this.scenario, SCENARIO_MODE.production).valid
      );
    }
    if (mode === SCENARIO_MODE.publishing) {
      return (
        isStepValid(this.scenario, SCENARIO_MODE.casting).valid &&
        isStepValid(this.scenario, SCENARIO_MODE.production).valid &&
        isStepValid(this.scenario, SCENARIO_MODE.publishing).valid
      );
    }
  }
}
