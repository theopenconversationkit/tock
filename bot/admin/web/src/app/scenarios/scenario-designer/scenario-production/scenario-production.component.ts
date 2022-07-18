import {
  Component,
  ElementRef,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DialogService } from '../../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../../shared/choice-dialog/choice-dialog.component';
import { Scenario, SCENARIO_ITEM_FROM_BOT, SCENARIO_ITEM_FROM_CLIENT } from '../../models';
import { ScenarioProductionService } from './scenario-production.service';
import { SVG } from '@svgdotjs/svg.js';
import { revertTransformMatrix } from '../../commons/utils';

const CANVAS_TRANSITION_TIMING = 300;
const TRANSITION_COLOR = '#ccc';
@Component({
  selector: 'scenario-production',
  templateUrl: './scenario-production.component.html',
  styleUrls: ['./scenario-production.component.scss'],
  providers: [ScenarioProductionService]
})
export class ScenarioProductionComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @Input() scenario: Scenario;

  @ViewChild('canvasWrapperElem') canvasWrapperElem: ElementRef;
  @ViewChild('canvasElem') canvasElem: ElementRef;

  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  constructor(
    private scenarioProductionService: ScenarioProductionService,
    private dialogService: DialogService
  ) {
    this.scenarioProductionService.scenarioProductionItemsCommunication
      .pipe(takeUntil(this.destroy))
      .subscribe((evt) => {
        if (evt.type == 'itemDropped') {
          this.itemDropped(evt);
        }
        if (evt.type == 'addStateGroup') {
          this.addStateGroup(evt);
        }
        if (evt.type == 'removeState') {
          this.removeState(evt);
        }
        if (evt.type == 'redrawPaths') {
          this.drawPaths();
        }
      });
  }

  ngOnInit(): void {
    if (!this.scenario.data.stateMachine) this.initStateMachine();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.drawPaths();
    }, 100);
  }

  svgCanvas;
  svgCanvasArrowMarker;
  svgCanvasGroup;
  initSvgCanvas() {
    this.svgCanvas = SVG().addTo(this.canvasElem.nativeElement).size('100%', '100%');
    this.svgCanvas.attr('style', 'position:absolute;top:0;left:0;pointer-events: none;');
    this.svgCanvasArrowMarker = this.svgCanvas.marker(3.5, 3.5, function (add) {
      add.polygon('0 0, 3.5 1.75, 0 3.5').fill(TRANSITION_COLOR);
    });
    this.svgCanvasGroup = this.svgCanvas.group();
  }

  drawPaths() {
    if (!this.svgCanvas) this.initSvgCanvas();
    this.svgCanvasGroup.clear();

    const canvasPos = revertTransformMatrix(this.svgCanvas.node, this.canvasElem.nativeElement);
    const canvasLeft = canvasPos.left;
    const canvasTop = canvasPos.top;

    let transitions = this.getAllTransitions(this.scenario.data.stateMachine);
    for (let transitionName in transitions) {
      const transitionComponent =
        this.scenarioProductionService.scenarioProductionTransitionsComponents[transitionName];
      if (!transitionComponent) return;

      const transitionElem = transitionComponent.elementRef.nativeElement;
      const transitionElemPos = revertTransformMatrix(
        transitionElem,
        this.canvasElem.nativeElement
      );

      const stateComponent =
        this.scenarioProductionService.scenarioProductionStateComponents[
          transitions[transitionName]
        ];
      const stateElem = stateComponent.elementRef.nativeElement;
      const stateElemPos = revertTransformMatrix(stateElem, this.canvasElem.nativeElement);
      const startLeft = transitionElemPos.left + transitionElemPos.width - canvasLeft;
      const startTop = transitionElemPos.top + transitionElemPos.height / 2 - canvasTop;
      const endLeft = stateElemPos.left - canvasLeft - 5;
      const endTop = stateElemPos.top + stateElemPos.height / 2 - canvasTop;

      this.svgCanvasGroup
        .path(`M${startLeft} ${startTop} L${endLeft} ${endTop}`)
        .stroke({ color: TRANSITION_COLOR, width: 3, linecap: 'round', linejoin: 'round' })
        .marker('end', this.svgCanvasArrowMarker);
    }
  }

  getAllTransitions(group, result = {}) {
    if (group.on) Object.assign(result, group.on);
    if (group.states) {
      for (let name in group.states) {
        this.getAllTransitions(group.states[name], result);
      }
    }
    return result;
  }

  getScenarioIntents() {
    let intents = [];
    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_CLIENT && item.intentDefinition) {
        intents.push(item.intentDefinition);
      }
    });
    return intents.sort((a, b) => {
      const aIsUsed = this.isIntentInUse(a);
      if (aIsUsed) return 1;
      const bIsUsed = this.isIntentInUse(b);
      if (bIsUsed) return -1;
      return 0;
    });
  }

  getIntentTooltip(intent) {
    return intent.label ? intent.label : intent.name;
  }

  getScenarioActions() {
    let actions = [];
    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_BOT && item.tickActionDefinition) {
        actions.push(item.tickActionDefinition);
      }
    });
    return actions.sort((a, b) => {
      const aIsUsed = this.isActionInUse(a);
      if (aIsUsed) return 1;
      const bIsUsed = this.isActionInUse(b);
      if (bIsUsed) return -1;
      return 0;
    });
  }

  collectAllUsedeNames() {
    let names = new Set();
    this.getScenarioActions().forEach((a) => names.add(a.name));
    this.getScenarioIntents().forEach((a) => names.add(a.name));
    this.getAllActionGroupNames(this.scenario.data.stateMachine).forEach((n) => names.add(n));
    return [...names];
  }

  getAllActionGroupNames(group, result = []) {
    if (group.id) result.push(group.id);
    if (group.states) {
      for (let name in group.states) {
        this.getAllActionGroupNames(group.states[name], result);
      }
    }
    return result;
  }

  isActionInUse(action) {
    return this.getActionById(action.name, this.scenario.data.stateMachine);
  }

  isIntentInUse(intent) {
    return this.getIntentByName(intent.name, this.scenario.data.stateMachine);
  }

  getActionTooltip(action) {
    if (action.description) return action.description;
    if (action.answer) return action.answer;
    return action.name;
  }

  initStateMachine() {
    this.scenario.data.stateMachine = {
      id: 'root',
      type: 'parallel',
      states: {
        Global: { id: 'Global', states: {}, on: {} }
      },
      initial: 'Global',
      on: {}
    };
  }

  resetStateMachine() {
    const cancelAction = 'cancel';
    const confirmAction = 'reset';
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: `Reset state machine`,
        subtitle: 'Are you sure you want to completely reset this state machine?',
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'default' },
          { actionName: confirmAction }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result) {
        if (result == confirmAction) {
          this.initStateMachine();
        }
      }
    });
  }

  itemDropped(event) {
    if (event.dropped.type === 'action') {
      let target = this.getActionById(event.stateId, this.scenario.data.stateMachine);
      if (target) {
        target.states[event.dropped.name] = { id: event.dropped.name };
      }
    }
    if (event.dropped.type === 'intent') {
      let parent = this.getActionParentById(event.stateId, this.scenario.data.stateMachine);
      if (parent) {
        parent.on[event.dropped.name] = event.stateId;
      }
    }
    this.scenarioProductionService.updateLayout();
  }

  addStateGroup(event) {
    let target = this.getActionById(event.stateId, this.scenario.data.stateMachine);
    if (target) {
      const alreadyExists = this.getActionById(event.groupName, this.scenario.data.stateMachine);
      if (alreadyExists) {
        return;
      }
      target.states[event.groupName] = { id: event.groupName, states: {}, on: {} };
    }
  }

  removeState(event) {
    let targetingIntentParent = this.getIntentParentByTarget(
      event.stateId,
      this.scenario.data.stateMachine
    );
    if (targetingIntentParent) {
      delete targetingIntentParent.parent.on[targetingIntentParent.intent];
    }

    let parent = this.getActionParentById(event.stateId, this.scenario.data.stateMachine);
    if (parent) {
      delete parent.states[event.stateId];
    }
  }

  getIntentParentByTarget(targetName, group): { parent: { on: object }; intent: string } | null {
    let result: { parent: { on: object }; intent: string } | null = null;
    if (group.on) {
      for (let transitionName in group.on) {
        if (group.on[transitionName] === targetName) {
          result = { parent: group, intent: transitionName };
          break;
        }
      }

      if (!result) {
        for (let action in group.states) {
          result = this.getIntentParentByTarget(targetName, group.states[action]);
          if (result) break;
        }
      }
    }

    return result;
  }

  getIntentByName(name, group) {
    let result = null;
    if (group.on) {
      for (let transitionName in group.on) {
        if (transitionName === name) {
          result = group.on[transitionName];
          break;
        }
      }

      if (!result) {
        for (let action in group.states) {
          result = this.getIntentByName(name, group.states[action]);
          if (result) break;
        }
      }
    }

    return result;
  }

  getActionById(id, group) {
    let result = null;

    if (group.id === id) return group;
    else {
      if (group.states) {
        for (let name in group.states) {
          result = this.getActionById(id, group.states[name]);
          if (result) break;
        }
      }
    }

    return result;
  }

  getActionParentById(id, group) {
    let result = null;
    if (group.states) {
      for (let name in group.states) {
        if (group.states[name].id === id) {
          result = group;
          break;
        }
      }

      if (!result) {
        for (let name in group.states) {
          result = this.getActionParentById(id, group.states[name]);
          if (result) break;
        }
      }
    }

    return result;
  }

  @ViewChild('tickStoryJsonTempModal') tickStoryJsonTempModal: TemplateRef<any>;
  displayStateMachineCode() {
    const tickStoryJson = JSON.stringify(this.scenario.data.stateMachine, null, 4);

    this.dialogService.openDialog(this.tickStoryJsonTempModal, { context: tickStoryJson });
  }

  mouseWheel(event: WheelEvent): void {
    event.preventDefault();
    this.zoomCanvas(event);
  }

  canvasPos = { x: 0, y: 0 };
  canvasPosOffset = { x: 0, y: 0 };
  pointer = { x: 0, y: 0 };
  canvasScale: number = 1;
  zoomSpeed: number = 0.5;
  isDragingCanvas;

  zoomCanvas(event: WheelEvent): void {
    let wrapper = this.canvasWrapperElem.nativeElement;
    let canvas = this.canvasElem.nativeElement;

    this.pointer.x = event.clientX - wrapper.offsetLeft;
    this.pointer.y = event.clientY - wrapper.offsetTop;
    this.canvasPosOffset.x = (this.pointer.x - this.canvasPos.x) / this.canvasScale;
    this.canvasPosOffset.y = (this.pointer.y - this.canvasPos.y) / this.canvasScale;

    this.canvasScale +=
      -1 * Math.max(-1, Math.min(1, event.deltaY)) * this.zoomSpeed * this.canvasScale;
    const max_scale = 1;
    const min_scale = 0.2;
    this.canvasScale = Math.max(min_scale, Math.min(max_scale, this.canvasScale));

    this.canvasPos.x = -this.canvasPosOffset.x * this.canvasScale + this.pointer.x;
    this.canvasPos.y = -this.canvasPosOffset.y * this.canvasScale + this.pointer.y;

    canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
  }

  onMouseDownCanvas(event: MouseEvent): void {
    if (event.button == 0) {
      this.isDragingCanvas = {
        left: this.canvasPos.x,
        top: this.canvasPos.y,
        x: event.clientX,
        y: event.clientY
      };
      let canvas = this.canvasElem.nativeElement;
      canvas.style.transition = 'unset';
    }
  }

  onMouseUpCanvas(event: MouseEvent): void {
    if (event.button == 0) {
      this.isDragingCanvas = undefined;
      let canvas = this.canvasElem.nativeElement;
      canvas.style.transition = `transform .${CANVAS_TRANSITION_TIMING}s`;
    }
  }

  onMouseMoveCanvas(event: MouseEvent): void {
    if (this.isDragingCanvas && event.button == 0) {
      let canvas = this.canvasElem.nativeElement;
      const dx = event.clientX - this.isDragingCanvas.x;
      const dy = event.clientY - this.isDragingCanvas.y;
      this.canvasPos.x = this.isDragingCanvas.left + dx;
      this.canvasPos.y = this.isDragingCanvas.top + dy;
      canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
    }
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
