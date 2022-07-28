import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DialogService } from '../../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../../shared/choice-dialog/choice-dialog.component';
import { Scenario, SCENARIO_MODE } from '../../models';
import { ScenarioDesignerService } from '../scenario-designer.service';

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

  constructor(
    private scenarioDesignerService: ScenarioDesignerService,
    private dialogService: DialogService
  ) {}

  switchMode(mode) {
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

  isStepPassed(mode) {
    const keys = Object.keys(SCENARIO_MODE);
    return keys.indexOf(mode) < keys.indexOf(this.mode);
  }

  getStepSequenceValidity(mode, isSwitchAction: boolean = false) {
    if (mode === SCENARIO_MODE.casting) {
      return this.scenarioDesignerService.isStepValid(
        this.scenario,
        SCENARIO_MODE.casting,
        isSwitchAction
      ).reason;
    }
    if (mode === SCENARIO_MODE.production) {
      let castingValidity = this.scenarioDesignerService.isStepValid(
        this.scenario,
        SCENARIO_MODE.casting,
        isSwitchAction
      );
      if (!castingValidity.valid) return castingValidity.reason;
      let productionValidity = this.scenarioDesignerService.isStepValid(
        this.scenario,
        SCENARIO_MODE.production,
        isSwitchAction
      );
      if (!productionValidity.valid) return productionValidity.reason;
    }
    if (mode === SCENARIO_MODE.publishing) {
      let castingValidity = this.scenarioDesignerService.isStepValid(
        this.scenario,
        SCENARIO_MODE.casting,
        isSwitchAction
      );
      if (!castingValidity.valid) return castingValidity.reason;
      let productionValidity = this.scenarioDesignerService.isStepValid(
        this.scenario,
        SCENARIO_MODE.production,
        isSwitchAction
      );
      if (!productionValidity.valid) return productionValidity.reason;
      let publishingValidity = this.scenarioDesignerService.isStepValid(
        this.scenario,
        SCENARIO_MODE.publishing,
        isSwitchAction
      );
      if (!publishingValidity.valid) return publishingValidity.reason;
    }
  }

  isStepSequenceValid(mode) {
    if (mode === SCENARIO_MODE.writing) return true;

    if (mode === SCENARIO_MODE.casting) {
      return this.scenarioDesignerService.isStepValid(this.scenario, SCENARIO_MODE.casting).valid;
    }
    if (mode === SCENARIO_MODE.production) {
      return (
        this.scenarioDesignerService.isStepValid(this.scenario, SCENARIO_MODE.casting).valid &&
        this.scenarioDesignerService.isStepValid(this.scenario, SCENARIO_MODE.production).valid
      );
    }
    if (mode === SCENARIO_MODE.publishing) {
      return (
        this.scenarioDesignerService.isStepValid(this.scenario, SCENARIO_MODE.casting).valid &&
        this.scenarioDesignerService.isStepValid(this.scenario, SCENARIO_MODE.production).valid &&
        this.scenarioDesignerService.isStepValid(this.scenario, SCENARIO_MODE.publishing).valid
      );
    }
  }
}
