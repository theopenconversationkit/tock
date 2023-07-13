import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { G, Marker, Svg, SVG } from '@svgdotjs/svg.js';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NbDialogService } from '@nebular/theme';

import { ChoiceDialogComponent, JsonPreviewerComponent } from '../../../shared/components';
import {
  ScenarioIntentDefinition,
  MachineState,
  ScenarioVersionExtended,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  ScenarioActionDefinition,
  ScenarioTriggerDefinition
} from '../../models';
import { ScenarioProductionService } from './scenario-production.service';
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

type ScenarioDefinition = {
  actions: ScenarioActionDefinition[];
  intents: ScenarioIntentDefinition[];
  triggers: ScenarioTriggerDefinition[];
};

type PathHandler = {
  transitionName: string;
  transitionSource: string;
  transitionTarget: string;
  transStartLeft: number;
  transStartTop: number;
  transEndLeft: number;
  transEndTop: number;
};

const TRANSITION_COLOR = '#006fd6';
const TRANSITION_COLOR_HOVERED = '#42aaff';
@Component({
  selector: 'tock-scenario-production',
  templateUrl: './scenario-production.component.html',
  styleUrls: ['./scenario-production.component.scss'],
  providers: [ScenarioProductionService]
})
export class ScenarioProductionComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit {
  destroy = new Subject();
  @Input() scenario: ScenarioVersionExtended;
  @Input() isReadonly: boolean;
  @Input() isFullscreen: boolean = false;

  @Output() requestFullscreen = new EventEmitter();

  @ViewChild('canvasElem') canvasElem: ElementRef;

  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  isSidePanelOpen: boolean = true;
  transitionsDefinition: (ScenarioTriggerDefinition | ScenarioIntentDefinition)[] = [];
  scenarioDefinition: ScenarioDefinition = {
    actions: [],
    intents: [],
    triggers: []
  };

  constructor(private scenarioProductionService: ScenarioProductionService, private nbDialogService: NbDialogService) {
    this.scenarioProductionService.scenarioProductionItemsCommunication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
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

  ngOnChanges(changes: SimpleChanges) {
    if (!this.scenario.data.stateMachine) this.initStateMachine();
    this.setScenarioDefinition();
  }

  ngOnInit(): void {
    this.transitionsDefinition = [...this.scenarioDefinition.intents, ...this.scenarioDefinition.triggers];
  }

  private setScenarioDefinition() {
    this.scenarioDefinition = {
      actions: this.getScenarioActionDefinitions(),
      intents: this.getScenarioIntentDefinitions(),
      triggers: this.getScenarioTriggerDefinition()
    };
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.drawPaths();
    });
  }

  pathHandlers: PathHandler[] = [];

  svgCanvas: Svg;
  svgCanvasGroup: G;

  svgCanvasStartMarker: Marker;
  svgCanvasStartMarkerHovered: Marker;
  svgCanvasEndMarker: Marker;
  svgCanvasEndMarkerHovered: Marker;

  initSvgCanvas(): void {
    this.svgCanvas = SVG().addTo(this.canvasElem.nativeElement).size('150%', '150%');
    this.svgCanvas.attr('style', 'position:absolute;top:0;left:0;pointer-events: none;');
    this.svgCanvasGroup = this.svgCanvas.group();

    this.svgCanvasStartMarker = this.svgCanvas.marker(2, 2, function (add) {
      add.circle(2).fill(TRANSITION_COLOR);
    });
    this.svgCanvasStartMarkerHovered = this.svgCanvas.marker(2, 2, function (add) {
      add.circle(2).fill(TRANSITION_COLOR_HOVERED);
    });
    this.svgCanvasEndMarker = this.svgCanvas.marker(3.5, 3.5, function (add) {
      add.polygon('0 0, 3.5 1.75, 0 3.5').fill(TRANSITION_COLOR);
    });
    this.svgCanvasEndMarkerHovered = this.svgCanvas.marker(3.5, 3.5, function (add) {
      add.polygon('0 0, 3.5 1.75, 0 3.5').fill(TRANSITION_COLOR_HOVERED);
    });
  }

  drawPaths(): void {
    if (!this.svgCanvas) this.initSvgCanvas();
    this.svgCanvasGroup.clear();
    this.pathHandlers = [];

    const canvasPos: DOMRect = revertTransformMatrix(this.svgCanvas.node, this.canvasElem.nativeElement);
    const canvasLeft = canvasPos.left;
    const canvasTop = canvasPos.top;

    const transitions = getAllSmTransitions(this.scenario.data.stateMachine);
    const sortedTransitions = [];
    transitions.forEach((entry) => {
      const transitionName = entry[0];
      const transitionTarget = entry[1];
      const transitionComponent = this.scenarioProductionService.scenarioProductionTransitionsComponents.find(
        (comp) => comp.transition.name === transitionName && comp.transition.target === transitionTarget
      );
      if (transitionComponent) {
        sortedTransitions.push({
          name: entry[0],
          target: entry[1],
          component: transitionComponent,
          hovered: transitionComponent.isHovered
        });
      }
    });

    sortedTransitions.sort((a, b) => {
      if (a.hovered && b.hovered) return 0;
      if (a.hovered) return 1;
      if (b.hovered) return -1;
    });

    sortedTransitions.forEach((transition) => {
      let color = transition.hovered ? TRANSITION_COLOR_HOVERED : TRANSITION_COLOR;
      let pathWidth = transition.hovered ? 4.3 : 3;
      let startMarker = transition.hovered ? this.svgCanvasStartMarkerHovered : this.svgCanvasStartMarker;
      let endMarker = transition.hovered ? this.svgCanvasEndMarkerHovered : this.svgCanvasEndMarker;

      const transitionElem = transition.component.elementRef.nativeElement;
      const transitionElemPos = revertTransformMatrix(transitionElem, this.canvasElem.nativeElement);

      const sourceStateComponent = this.scenarioProductionService.scenarioProductionStateComponents[transition.component.parentState.id];
      const sourceStateElem = sourceStateComponent.stateWrapper.nativeElement;
      const sourceStateElemPos = revertTransformMatrix(sourceStateElem, this.canvasElem.nativeElement);

      const targetStateComponent = this.scenarioProductionService.scenarioProductionStateComponents[transition.target.replace(/^#/, '')];
      const targetStateElem = targetStateComponent.stateWrapper.nativeElement;
      const targetStateElemPos = revertTransformMatrix(targetStateElem, this.canvasElem.nativeElement);

      let inPath: string;
      let inStartLeft: number;
      let inStartTop: number;
      let inEndLeft: number;
      let inEndTop: number;

      if (targetStateElemPos.left - canvasLeft > transitionElemPos.left - canvasLeft) {
        if (sourceStateComponent.state.states) {
          inStartLeft = sourceStateElemPos.left + 2 - canvasLeft;
          inStartTop = transitionElemPos.top + transitionElemPos.height / 2 - canvasTop;
          inEndLeft = transitionElemPos.left - canvasLeft;
          inEndTop = transitionElemPos.top + transitionElemPos.height / 2 - canvasTop;

          if (Math.round(inStartTop) === Math.round(inEndTop)) {
            inPath = `M${inStartLeft} ${inStartTop} L${inEndLeft} ${inEndTop}`;
          } else {
            const offset = 15;
            inPath = `M${inStartLeft} ${inStartTop} L${inEndLeft - offset} ${inStartTop}  L${
              inEndLeft - offset
            } ${inEndTop} L${inEndLeft} ${inEndTop}`;
          }
        } else {
          inStartLeft = sourceStateElemPos.left + sourceStateElemPos.width / 2 - canvasLeft;
          inStartTop = sourceStateElemPos.top + sourceStateElemPos.height - canvasTop;
          inEndLeft = transitionElemPos.left + transitionElemPos.width / 2 - canvasLeft;
          inEndTop = transitionElemPos.top - canvasTop;

          inPath = `M${inStartLeft} ${inStartTop} L${inEndLeft} ${inEndTop}`;
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

          if (inEndLeft > inStartLeft) {
            offset = inEndLeft - inStartLeft + 10;
          }

          inPath = `M${inStartLeft} ${inStartTop} L${inStartLeft + offset} ${inStartTop}  L${
            inStartLeft + offset
          } ${inEndTop} L${inEndLeft} ${inEndTop}`;
        }
      }

      this.svgCanvasGroup
        .path(inPath)
        .fill({ opacity: 0 })
        .stroke({ color: color, width: pathWidth, linecap: 'round', linejoin: 'round' })
        .marker('start', startMarker);

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
        const offset = 15;
        outPath = `M${outStartLeft} ${outStartTop} L${outEndLeft - offset} ${outStartTop}  L${
          outEndLeft - offset
        } ${outEndTop} L${outEndLeft} ${outEndTop}`;
      }

      this.svgCanvasGroup
        .path(outPath)
        .fill({ opacity: 0 })
        .stroke({ color: color, width: pathWidth, linecap: 'round', linejoin: 'round' })
        .marker('end', endMarker);

      this.pathHandlers.push({
        transitionName: transition.name,
        transitionSource: transition.component.parentState.id,
        transitionTarget: transition.target,
        transStartLeft: inStartLeft - 2,
        transStartTop: inStartTop - 10,
        transEndLeft: outEndLeft - 15,
        transEndTop: outEndTop - 10
      });
    });
  }

  private getScenarioIntentDefinitions(): ScenarioIntentDefinition[] {
    return getScenarioIntentDefinitions(this.scenario).sort((a, b) => {
      const aIsUsed = this.isIntentInUse(a);
      if (aIsUsed) return 1;
      const bIsUsed = this.isIntentInUse(b);
      if (bIsUsed) return -1;
      return 0;
    });
  }

  getDraggableIntentType(intent: ScenarioIntentDefinition): string {
    if (intent.primary) return 'primaryIntent';
    return 'intent';
  }

  getIntentTooltip(intent: ScenarioIntentDefinition): string {
    return intent.label ? intent.label : intent.name;
  }

  private getScenarioActionDefinitions(): ScenarioActionDefinition[] {
    return getScenarioActionDefinitions(this.scenario).sort((a, b) => {
      const aIsUsed = this.isActionInUse(a);
      if (aIsUsed) return 1;
      const bIsUsed = this.isActionInUse(b);
      if (bIsUsed) return -1;
      return 0;
    });
  }

  private getScenarioTriggerDefinition(): ScenarioTriggerDefinition[] {
    const triggers = getScenarioActionDefinitions(this.scenario)
      .filter((a) => a.trigger)
      .map((a) => a.trigger);

    return [...new Set(triggers)];
  }

  collectAllUsedNames(): string[] {
    let names = new Set<string>();
    getScenarioActionDefinitions(this.scenario).forEach((a) => names.add(a.name));
    getScenarioIntentDefinitions(this.scenario).forEach((a) => names.add(a.name));
    getAllSmStatesNames(this.scenario.data.stateMachine).forEach((n) => names.add(n));
    return [...names];
  }

  isActionInUse(action: ScenarioActionDefinition): MachineState {
    return getSmStateById(action.name, this.scenario.data.stateMachine);
  }

  isIntentInUse(intent: ScenarioIntentDefinition): string {
    return getSmTransitionByName(intent.name, this.scenario.data.stateMachine);
  }

  isIntentDraggable(intent: ScenarioIntentDefinition): boolean {
    const item = this.scenario.data.scenarioItems.find((item) => item.intentDefinition === intent);
    if (item.main) {
      let exists = this.isIntentInUse(intent);
      return exists ? false : true;
    }
    return true;
  }

  getActionTooltip(action): string {
    if (action.description) return action.description;
    if (action.answer) return action.answer;
    return action.name;
  }

  initStateMachine(): void {
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

  resetStateMachine(): void {
    const cancelAction = 'cancel';
    const confirmAction = 'reset';
    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `Reset state machine`,
        subtitle: 'Are you sure you want to completely reset this state machine?',
        modalStatus: 'danger',
        actions: [{ actionName: cancelAction, buttonStatus: 'basic', ghost: true }, { actionName: confirmAction }]
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

  private formatTransitionName(transition: string): string {
    const expected = /^#[^#].*/;
    return transition.match(expected) ? transition : `#${transition}`;
  }

  itemDropped(event): void {
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
        parent.on[event.dropped.name] = this.formatTransitionName(event.stateId);
      }
    }
    if (event.dropped.type === 'transitionTarget') {
      let sourceState = getSmStateById(event.dropped.source, this.scenario.data.stateMachine);
      sourceState.on[event.dropped.name] = this.formatTransitionName(event.stateId);
    }

    if (event.dropped.type === 'transitionSource') {
      const initialSourceState = getSmStateById(event.dropped.source, this.scenario.data.stateMachine);
      const target = initialSourceState.on[event.dropped.name];
      delete initialSourceState.on[event.dropped.name];
      const newSourceState = getSmStateById(event.stateId, this.scenario.data.stateMachine);
      if (!newSourceState.on) newSourceState.on = {};
      newSourceState.on[event.dropped.name] = this.formatTransitionName(target);
    }

    this.scenarioProductionService.updateLayout();
  }

  private addStateGroup(event): void {
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

  private removeState(event): void {
    removeSmStateById(event.stateId, this.scenario.data.stateMachine);
  }

  displayStateMachineCode(): void {
    this.nbDialogService.open(JsonPreviewerComponent, {
      context: { jsonData: this.scenario.data.stateMachine },
      dialogClass: 'full-width-dialog'
    });
  }

  preventDefault(event): void {
    event.stopPropagation();
  }

  toggleFullscreen(_toggle: boolean): void {
    this.requestFullscreen.emit();
  }

  sidePanelChange(status: 'open' | 'close'): void {
    this.isSidePanelOpen = status === 'open';
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
