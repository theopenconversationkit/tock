import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  QueryList,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { StateService } from '../../../../core-nlp/state.service';
import { ChoiceDialogComponent } from '../../../../shared/components';
import { getSmStateParentById } from '../../../commons/utils';
import { ScenarioIntentDefinition, MachineState, ScenarioActionDefinition, Transition } from '../../../models';
import { ScenarioProductionService } from '../scenario-production.service';
import { ScenarioProductionStateGroupAddComponent } from './state-group-add/state-group-add.component';
import { ScenarioTransitionComponent } from './transition/transition.component';

@Component({
  selector: 'tock-scenario-state-group',
  templateUrl: './state-group.component.html',
  styleUrls: ['./state-group.component.scss']
})
export class ScenarioStateGroupComponent implements OnDestroy, AfterViewInit {
  destroy = new Subject();
  @Input() state: MachineState;
  @Input() stateMachine: MachineState;
  @Input() usedNames: string[];
  @Input() intents: ScenarioIntentDefinition[];
  @Input() actions: ScenarioActionDefinition[];
  @Input() isReadonly: boolean = false;

  @ViewChild('stateWrapper') stateWrapper: ElementRef;

  @ViewChildren(ScenarioTransitionComponent)
  childTransitionsComponents: QueryList<ScenarioTransitionComponent>;

  constructor(
    private scenarioProductionService: ScenarioProductionService,
    private nbDialogService: NbDialogService,
    private cd: ChangeDetectorRef,
    private stateService: StateService
  ) {
    this.scenarioProductionService.scenarioProductionItemsCommunication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type == 'redrawActions') {
        this.updateTransitionWrapperWidth();
      }
    });
  }

  viewInited: boolean = false;

  ngAfterViewInit(): void {
    this.viewInited = true;
    this.scenarioProductionService.registerStateComponent(this);
    setTimeout(() => {
      this.updateTransitionWrapperWidth();
    });
  }

  getActionTooltip(): string {
    const action = this.actions.find((a) => {
      return a.name === this.state.id;
    });
    if (action) {
      if (action.description) return action.description;

      if (action.answers?.length) {
        for (let index = 0; index < action.answers.length; index++) {
          if (action.answers[index].locale === this.stateService.currentLocale) {
            return action.answers[index].answer;
          }
        }
        return action.answers[0].answer;
      }
      return action.name;
    }
    return this.state.id;
  }

  isInitialParentState(): boolean {
    let parent = getSmStateParentById(this.state.id, this.stateMachine);
    if (parent?.initial === this.state.id) return true;
    return false;
  }

  setActionInitial(): void {
    let parent = getSmStateParentById(this.state.id, this.stateMachine);
    if (parent) {
      parent.initial = this.state.id;
    }
  }

  hasOutgoingTransitions(): boolean {
    return this.state.on && Object.keys(this.state.on).length > 0;
  }

  transitionWrapperWidth: number = 0;

  private updateTransitionWrapperWidth(): void {
    this.transitionWrapperWidth = this.getMaxTransitionWidth();
    this.cd.detectChanges();
  }

  private getMaxTransitionWidth(): number {
    let width = 0;
    if (this.childTransitionsComponents) {
      this.childTransitionsComponents.forEach((t) => {
        if (t.elementRef.nativeElement.offsetWidth > width) width = t.elementRef.nativeElement.offsetWidth + 50;
      });
    }
    return width;
  }

  addStateGroup(): void {
    const modal = this.nbDialogService.open(ScenarioProductionStateGroupAddComponent, {
      context: { usedNames: this.usedNames }
    });
    const validate = modal.componentRef.instance.validate.pipe(takeUntil(this.destroy)).subscribe((result) => {
      this.scenarioProductionService.addStateGroup(this.state.id, result.name);
      validate.unsubscribe();
      modal.close();
    });
  }

  removeState(): void {
    const cancelAction = 'cancel';
    const confirmAction = 'delete';
    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `Delete action`,
        subtitle: 'Are you sure you want to delete this action?',
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'basic', ghost: true },
          { actionName: confirmAction, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result) {
        if (result == confirmAction) {
          this.scenarioProductionService.removeState(this.state.id);
          this.scenarioProductionService.updateLayout();
        }
      }
    });
  }

  removeTransition(transition: Transition): void {
    const cancelAction = 'cancel';
    const confirmAction = 'delete';
    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `Delete transition`,
        subtitle: 'Are you sure you want to delete this transition?',
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'basic', ghost: true },
          { actionName: confirmAction, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result) {
        if (result == confirmAction) {
          delete this.state.on[transition.name];
          this.scenarioProductionService.updateLayout();
        }
      }
    });
  }

  getDraggableTypes(): string[] {
    if (this.state.id.toLowerCase() == 'global') return ['action'];

    let parent = getSmStateParentById(this.state.id, this.stateMachine);
    if (parent?.id.toLowerCase() === 'global') {
      if (this.state.states) {
        return ['primaryIntent', 'action'];
      }
      return ['primaryIntent'];
    }

    if (this.state.states) {
      return ['intent', 'action'];
    }
    return ['intent'];
  }

  onDrop(event): void {
    if (event.data.source === this.state.id) return;

    this.scenarioProductionService.itemDropped(this.state.id, event.data);
  }

  getNextActionError(): string | null {
    if (this.state.states) {
      let states = Object.keys(this.state.states);
      if (!states.length) return 'An action group cannot be empty';
    }

    if (this.state.states && !this.state.initial) {
      return 'This action group lacks an initial state';
    }

    return null;
  }

  ngOnDestroy(): void {
    this.scenarioProductionService.unRegisterStateComponent(this.state.id);
    this.destroy.next(true);
    this.destroy.complete();
  }
}
