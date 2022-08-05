import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DialogService } from '../../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../../shared/choice-dialog/choice-dialog.component';
import {
  intentDefinition,
  machineState,
  Scenario,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  TickActionDefinition
} from '../../models';
import { ScenarioProductionService } from './scenario-production.service';
import { SVG } from '@svgdotjs/svg.js';
import {
  getSmTransitionByName,
  getScenarioActionDefinitions,
  getScenarioIntentDefinitions,
  getSmStateParentById,
  revertTransformMatrix,
  getAllSmTransitions,
  getSmStateById,
  getAllSmStatesNames,
  removeSmStateById
} from '../../commons/utils';
import { JsonPreviewerComponent } from '../../../shared/json-previewer/json-previewer.component';

const CANVAS_TRANSITION_TIMING = 300;
const TRANSITION_COLOR = '#006fd6';
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
    });
  }

  pathHandlers = [];

  svgCanvas;
  svgCanvasStartArrowMarker;
  svgCanvasEndArrowMarker;
  svgCanvasGroup;
  initSvgCanvas() {
    this.svgCanvas = SVG().addTo(this.canvasElem.nativeElement).size('150%', '150%');
    this.svgCanvas.attr('style', 'position:absolute;top:0;left:0;pointer-events: none;');
    this.svgCanvasStartArrowMarker = this.svgCanvas.marker(2, 2, function (add) {
      add.circle(2).fill(TRANSITION_COLOR);
    });
    this.svgCanvasEndArrowMarker = this.svgCanvas.marker(3.5, 3.5, function (add) {
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

    let transitions = getAllSmTransitions(this.scenario.data.stateMachine);
    this.pathHandlers = [];
    transitions.forEach((entry) => {
      const transitionName = entry[0];
      const transitionTarget = entry[1];
      const transitionComponent =
        this.scenarioProductionService.scenarioProductionTransitionsComponents.find(
          (comp) =>
            comp.transition.name === transitionName && comp.transition.target === transitionTarget
        );
      if (!transitionComponent) return;

      const transitionElem = transitionComponent.elementRef.nativeElement;
      const transitionElemPos = revertTransformMatrix(
        transitionElem,
        this.canvasElem.nativeElement
      );

      const sourceStateComponent =
        this.scenarioProductionService.scenarioProductionStateComponents[
          transitionComponent.parentState.id
        ];
      const sourceStateElem = sourceStateComponent.stateWrapper.nativeElement;
      const sourceStateElemPos = revertTransformMatrix(
        sourceStateElem,
        this.canvasElem.nativeElement
      );

      const targetStateComponent =
        this.scenarioProductionService.scenarioProductionStateComponents[
          transitionTarget.replace(/^#/, '')
        ];
      const targetStateElem = targetStateComponent.stateWrapper.nativeElement;
      const targetStateElemPos = revertTransformMatrix(
        targetStateElem,
        this.canvasElem.nativeElement
      );

      let inPath;
      let inStartLeft;
      let inStartTop;
      let inEndLeft;
      let inEndTop;

      if (targetStateElemPos.left - canvasLeft > transitionElemPos.left - canvasLeft) {
        inStartLeft = sourceStateElemPos.left + 2 - canvasLeft;
        inStartTop = transitionElemPos.top + transitionElemPos.height / 2 - canvasTop;
        inEndLeft = transitionElemPos.left - canvasLeft;
        inEndTop = transitionElemPos.top + transitionElemPos.height / 2 - canvasTop;

        if (Math.round(inStartTop) === Math.round(inEndTop)) {
          inPath = `M${inStartLeft} ${inStartTop} L${inEndLeft} ${inEndTop}`;
        } else {
          let offset = 15;
          inPath = `M${inStartLeft} ${inStartTop} L${inEndLeft - offset} ${inStartTop}  L${
            inEndLeft - offset
          } ${inEndTop} L${inEndLeft} ${inEndTop}`;
        }
      } else {
        inStartLeft = sourceStateElemPos.left + sourceStateElemPos.width - canvasLeft;
        inStartTop = sourceStateElemPos.top + sourceStateElemPos.height / 2 - canvasTop;
        inEndLeft = transitionElemPos.left + transitionElemPos.width - canvasLeft;
        inEndTop = transitionElemPos.top + transitionElemPos.height / 2 - canvasTop;
        if (Math.round(inStartTop) === Math.round(inEndTop)) {
          inPath = `M${inStartLeft} ${inStartTop} L${inEndLeft} ${inEndTop}`;
        } else {
          let offset = 10;
          inPath = `M${inStartLeft} ${inStartTop} L${inStartLeft + offset} ${inStartTop}  L${
            inStartLeft + offset
          } ${inEndTop} L${inEndLeft} ${inEndTop}`;
        }
      }

      this.svgCanvasGroup
        .path(inPath)
        .fill({ opacity: 0 })
        .stroke({ color: TRANSITION_COLOR, width: 4, linecap: 'round', linejoin: 'round' })
        .marker('start', this.svgCanvasStartArrowMarker);

      let outPath;
      let outStartLeft;
      let outStartTop;
      if (targetStateElemPos.left - canvasLeft > transitionElemPos.left - canvasLeft) {
        outStartLeft = transitionElemPos.left + transitionElemPos.width - canvasLeft;
        outStartTop = transitionElemPos.top + transitionElemPos.height / 2 - canvasTop;
      } else {
        outStartLeft = transitionElemPos.left - canvasLeft;
        outStartTop = transitionElemPos.top + transitionElemPos.height / 2 - canvasTop;
      }
      const outEndLeft = targetStateElemPos.left - canvasLeft - 5;
      const outEndTop = targetStateElemPos.top + targetStateElemPos.height / 2 - canvasTop;

      if (Math.round(outStartTop) === Math.round(outEndTop)) {
        outPath = `M${outStartLeft} ${outStartTop} L${outEndLeft} ${outEndTop}`;
      } else {
        let offset = 15;
        outPath = `M${outStartLeft} ${outStartTop} L${outEndLeft - offset} ${outStartTop}  L${
          outEndLeft - offset
        } ${outEndTop} L${outEndLeft} ${outEndTop}`;
      }

      this.svgCanvasGroup
        .path(outPath)
        .fill({ opacity: 0 })
        .stroke({ color: TRANSITION_COLOR, width: 4, linecap: 'round', linejoin: 'round' })
        .marker('end', this.svgCanvasEndArrowMarker);

      this.pathHandlers.push({
        transitionName: transitionName,
        transitionSource: transitionComponent.parentState.id,
        transitionTarget: transitionTarget,
        transStartLeft: inStartLeft - 2,
        transStartTop: inStartTop - 10,
        transEndLeft: outEndLeft - 15,
        transEndTop: outEndTop - 10
      });
    });
  }

  getScenarioIntentDefinitions() {
    return getScenarioIntentDefinitions(this.scenario).sort((a, b) => {
      const aIsUsed = this.isIntentInUse(a);
      if (aIsUsed) return 1;
      const bIsUsed = this.isIntentInUse(b);
      if (bIsUsed) return -1;
      return 0;
    });
  }

  getDraggableIntentType(intent: intentDefinition) {
    if (intent.primary) return 'primaryIntent';
    return 'intent';
  }

  getIntentTooltip(intent: intentDefinition) {
    return intent.label ? intent.label : intent.name;
  }

  getScenarioActionDefinitions(): TickActionDefinition[] {
    return getScenarioActionDefinitions(this.scenario).sort((a, b) => {
      const aIsUsed = this.isActionInUse(a);
      if (aIsUsed) return 1;
      const bIsUsed = this.isActionInUse(b);
      if (bIsUsed) return -1;
      return 0;
    });
  }

  collectAllUsedNames(): string[] {
    let names = new Set<string>();
    getScenarioActionDefinitions(this.scenario).forEach((a) => names.add(a.name));
    getScenarioIntentDefinitions(this.scenario).forEach((a) => names.add(a.name));
    getAllSmStatesNames(this.scenario.data.stateMachine).forEach((n) => names.add(n));
    return [...names];
  }

  isActionInUse(action: TickActionDefinition): machineState {
    return getSmStateById(action.name, this.scenario.data.stateMachine);
  }

  isIntentInUse(intent: intentDefinition): string {
    return getSmTransitionByName(intent.name, this.scenario.data.stateMachine);
  }

  isIntentDraggable(intent: intentDefinition): boolean {
    const item = this.scenario.data.scenarioItems.find((item) => item.intentDefinition === intent);
    if (item.main) {
      let exists = this.isIntentInUse(intent);
      return exists ? false : true;
    }
    return true;
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
      let target = getSmStateById(event.stateId, this.scenario.data.stateMachine);
      if (target) {
        target.states[event.dropped.name] = { id: event.dropped.name };
        if (!target.initial) target.initial = event.dropped.name;
      }
    }
    if (event.dropped.type === 'intent') {
      let parent = getSmStateParentById(event.stateId, this.scenario.data.stateMachine);
      if (parent) {
        parent.on[event.dropped.name] = `#${event.stateId}`;
      }
    }
    if (event.dropped.type === 'transitionTarget') {
      let sourceState = getSmStateById(event.dropped.source, this.scenario.data.stateMachine);
      sourceState.on[event.dropped.name] = event.stateId;
    }

    if (event.dropped.type === 'transitionSource') {
      const initialSourceState = getSmStateById(
        event.dropped.source,
        this.scenario.data.stateMachine
      );
      const target = initialSourceState.on[event.dropped.name];
      delete initialSourceState.on[event.dropped.name];
      const newSourceState = getSmStateById(event.stateId, this.scenario.data.stateMachine);
      if (!newSourceState.on) newSourceState.on = {};
      newSourceState.on[event.dropped.name] = target;
    }

    this.scenarioProductionService.updateLayout();
    setTimeout(() => this.scenarioProductionService.updateLayout(), 300);
  }

  addStateGroup(event) {
    let target = getSmStateById(event.stateId, this.scenario.data.stateMachine);
    if (target) {
      const alreadyExists = getSmStateById(event.groupName, this.scenario.data.stateMachine);
      if (alreadyExists) {
        return;
      }
      target.states[event.groupName] = { id: event.groupName, states: {}, on: {} };
      if (!target.initial) target.initial = event.groupName;

      this.scenarioProductionService.updateLayout();
    }
  }

  removeState(event) {
    removeSmStateById(event.stateId, this.scenario.data.stateMachine);
  }

  displayStateMachineCode(): void {
    const jsonPreviewerRef = this.dialogService.openDialog(JsonPreviewerComponent, {
      context: { jsonData: this.scenario.data.stateMachine }
    });

    jsonPreviewerRef.componentRef.instance.jsonPreviewerRef = jsonPreviewerRef;
  }

  preventDefault(event) {
    event.stopPropagation();
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
