import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SCENARIO_MODE } from '../../models';

@Component({
  selector: 'scenario-mode-stepper',
  templateUrl: './mode-stepper.component.html',
  styleUrls: ['./mode-stepper.component.scss']
})
export class ModeStepperComponent {
  readonly SCENARIO_MODE = SCENARIO_MODE;
  @Input() mode: SCENARIO_MODE;
  @Output() modeSwitch = new EventEmitter();

  switchMode(mode) {
    this.modeSwitch.emit(mode);
  }

  isStepPassed(mode) {
    const keys = Object.keys(SCENARIO_MODE);
    return keys.indexOf(mode) < keys.indexOf(this.mode);
  }
}
