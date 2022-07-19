import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  OnInit,
  QueryList,
  ViewChildren
} from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DialogService } from '../../../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../../../shared/choice-dialog/choice-dialog.component';
import { getStateMachineActionParentById } from '../../../commons/utils';
import { intentDefinition, TickActionDefinition } from '../../../models';
import { ScenarioProductionService } from '../scenario-production.service';
import { ScenarioProductionStateGroupAddComponent } from './state-group-add/state-group-add.component';
import { ScenarioTransitionComponent } from './transition/transition.component';

@Component({
  selector: 'scenario-state-group',
  templateUrl: './state-group.component.html',
  styleUrls: ['./state-group.component.scss']
})
export class ScenarioStateGroupComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @Input() state;
  @Input() stateMachine;
  @Input() usedNames: string[];
  @Input() intents: intentDefinition[];
  @Input() actions: TickActionDefinition[];

  @ViewChildren(ScenarioTransitionComponent)
  childTransitionsComponents: QueryList<ScenarioTransitionComponent>;

  constructor(
    private scenarioProductionService: ScenarioProductionService,
    private dialogService: DialogService,
    public elementRef: ElementRef,
    private cd: ChangeDetectorRef
  ) {
    this.scenarioProductionService.scenarioProductionItemsCommunication
      .pipe(takeUntil(this.destroy))
      .subscribe((evt) => {
        if (evt.type == 'redrawActions') {
          this.updateTransitionWrapperWidth();
        }
      });
  }

  ngOnInit(): void {}

  viewInited = false;
  ngAfterViewInit(): void {
    this.viewInited = true;
    this.scenarioProductionService.registerStateComponent(this);
    setTimeout(() => {
      this.updateTransitionWrapperWidth();
    });
  }

  isInitialParentState(): boolean {
    let parent = getStateMachineActionParentById(this.state.id, this.stateMachine);
    if (parent?.initial === this.state.id) return true;
    return false;
  }

  setActionInitial() {
    let parent = getStateMachineActionParentById(this.state.id, this.stateMachine);
    if (parent) {
      parent.initial = this.state.id;
    }
  }

  transitionWrapperWidth = 0;
  updateTransitionWrapperWidth() {
    this.transitionWrapperWidth = this.getMaxTransitionWidth();
    this.cd.detectChanges();
  }

  getMaxTransitionWidth() {
    let width = 0;
    if (this.childTransitionsComponents) {
      this.childTransitionsComponents.forEach((t) => {
        if (t.elementRef.nativeElement.offsetWidth > width)
          width = t.elementRef.nativeElement.offsetWidth + 20;
      });
    }
    return width;
  }

  addStateGroup() {
    const modal = this.dialogService.openDialog(ScenarioProductionStateGroupAddComponent, {
      context: { usedNames: this.usedNames }
    });
    const validate = modal.componentRef.instance.validate
      .pipe(takeUntil(this.destroy))
      .subscribe((result) => {
        this.scenarioProductionService.addStateGroup(this.state.id, result.name);
        validate.unsubscribe();
        modal.close();
      });
  }

  removeState() {
    const cancelAction = 'cancel';
    const confirmAction = 'delete';
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: `Delete action`,
        subtitle: 'Are you sure you want to delete this action?',
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'default' },
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

  removeTransition(transition) {
    const cancelAction = 'cancel';
    const confirmAction = 'delete';
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: `Delete transition`,
        subtitle: 'Are you sure you want to delete this transition?',
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'default' },
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

  getDraggableTypes() {
    if (this.state.id.toLowerCase() == 'global') return ['action'];

    let parent = getStateMachineActionParentById(this.state.id, this.stateMachine);
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

  onDrop(event) {
    this.scenarioProductionService.itemDropped(this.state.id, event.data);
  }

  ngOnDestroy(): void {
    this.scenarioProductionService.unRegisterStateComponent(this.state.id);
    this.destroy.next();
    this.destroy.complete();
  }
}
