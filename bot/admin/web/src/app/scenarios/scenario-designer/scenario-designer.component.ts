/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {
  Component,
  ElementRef,
  HostListener,
  Injectable,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { Subject } from 'rxjs';
import { pluck, takeUntil } from 'rxjs/operators';
import { ScenarioDesignerService } from './scenario-designer-service.service';
import {
  Scenario,
  scenarioItem,
  scenarioItemFrom,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE_PRODUCTION,
  SCENARIO_MODE_WRITING
} from '../models/scenario.model';
import { ScenarioService } from '../services/scenario.service';
import { ActivatedRoute, CanDeactivate, Router } from '@angular/router';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { ConfirmDialogComponent } from 'src/app/shared-nlp/confirm-dialog/confirm-dialog.component';
import { NbToastrService } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { entityColor, qualifiedRole } from '../../model/nlp';
import { getContrastYIQ } from '../commons/utils';
import { ContextCreateComponent } from './context-create/context-create.component';

const CANVAS_TRANSITION_TIMING = 300;

@Component({
  selector: 'scenario-designer',
  templateUrl: './scenario-designer.component.html',
  styleUrls: ['./scenario-designer.component.scss'],
  providers: [ScenarioDesignerService]
})
export class ScenarioDesignerComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @ViewChild('canvasWrapperElem') canvasWrapperElem: ElementRef;
  @ViewChild('canvasElem') canvasElem: ElementRef;

  scenarioId: number;
  scenario: Scenario;
  scenarioBackup: string;

  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;
  readonly SCENARIO_MODE_PRODUCTION = SCENARIO_MODE_PRODUCTION;
  readonly SCENARIO_MODE_WRITING = SCENARIO_MODE_WRITING;

  constructor(
    private scenarioService: ScenarioService,
    private scenarioDesignerService: ScenarioDesignerService,
    route: ActivatedRoute,
    private router: Router,
    private toastrService: NbToastrService,
    protected state: StateService,
    private dialogService: DialogService
  ) {
    route.params
      .pipe(takeUntil(this.destroy), pluck('id'))
      .subscribe((id) => (this.scenarioId = +id));
  }

  ngOnInit(): void {
    this.scenarioDesignerService.scenarioDesignerItemsCommunication
      .pipe(takeUntil(this.destroy))
      .subscribe((evt) => {
        if (evt.type == 'addAnswer') this.addAnswer(evt.item);
        if (evt.type == 'deleteAnswer') this.deleteAnswer(evt.item, evt.parentItemId);
        if (evt.type == 'itemDropped') this.itemDropped(evt.targetId, evt.droppedId);
        if (evt.type == 'itemSelected') this.selectItem(evt.item);
        if (evt.type == 'testItem') this.testStory(evt.item);
        if (evt.type == 'exposeItemPosition') this.centerOnItem(evt.item, evt.position);
      });

    this.scenarioService
      .getScenario(this.scenarioId)
      .pipe(takeUntil(this.destroy))
      .subscribe((data) => {
        if (typeof data.mode == 'undefined') data.mode = SCENARIO_MODE_WRITING;
        this.scenarioBackup = JSON.stringify(data);
        this.scenario = JSON.parse(JSON.stringify(data));

        this.setMode(this.scenario.mode || 'writing');

        if (!this.scenario.data) this.scenario.data = { scenarioItems: [], contexts: [] };
        if (!this.scenario.data.scenarioItems.length) {
          this.scenario.data.scenarioItems.push({
            id: 0,
            from: SCENARIO_ITEM_FROM_CLIENT,
            text: ''
          });
        }
        if (!this.scenario.data.contexts) {
          this.scenario.data.contexts = [];
        }
      });

    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => {
      this.exit();
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.centerCanvas();
    }, 0);
  }

  modeSwitchState = {
    modeBoolean: false,
    label: 'Writing',
    labelPosition: 'left'
  };

  modeSwitched(event) {
    if (event) {
      this.setMode(SCENARIO_MODE_PRODUCTION);
    } else {
      this.setMode(SCENARIO_MODE_WRITING);
    }
  }

  setMode(mode) {
    if (mode == SCENARIO_MODE_PRODUCTION) {
      this.scenario.mode = SCENARIO_MODE_PRODUCTION;
      this.modeSwitchState = {
        modeBoolean: true,
        label: 'Production',
        labelPosition: 'right'
      };
    } else {
      this.scenario.mode = SCENARIO_MODE_WRITING;
      this.modeSwitchState = {
        modeBoolean: false,
        label: 'Writing',
        labelPosition: 'left'
      };
    }
  }

  contextsPanelShowed = true;

  getContextEntityColor(context) {
    if (context.entityType)
      return entityColor(qualifiedRole(context.entityType, context.entityRole));
  }

  getContextEntityContrast(context) {
    if (context.entityType)
      return getContrastYIQ(entityColor(qualifiedRole(context.entityType, context.entityRole)));
  }

  addContext() {
    const modal = this.dialogService.openDialog(ContextCreateComponent, {
      context: {}
    });
    const validate = modal.componentRef.instance.validate
      .pipe(takeUntil(this.destroy))
      .subscribe((contextDef) => {
        this.scenario.data.contexts.push({
          name: contextDef.name,
          type: 'string'
        });

        validate.unsubscribe();
        modal.close();
      });
  }

  stringifiedCleanScenario() {
    return JSON.stringify(this.scenario, function (key, value) {
      if (key.indexOf('_') == 0) return undefined;
      return value;
    });
  }

  getCleanScenario() {
    return JSON.parse(this.stringifiedCleanScenario());
  }

  save(exit: boolean = false) {
    this.scenarioService.putScenario(this.scenarioId, this.getCleanScenario()).subscribe((data) => {
      this.toastrService.success(`Scenario successfully saved`, 'Success', {
        duration: 5000,
        status: 'success'
      });
      this.scenarioBackup = JSON.stringify(data);
      if (exit) this.exit();
    });
  }

  exit() {
    this.router.navigateByUrl('/scenarios');
  }

  deleteAnswer(itemRef: scenarioItem, parentItemId: number): void {
    if (itemRef.parentIds.length > 1) {
      itemRef.parentIds = itemRef.parentIds.filter((pi) => pi != parentItemId);
    } else {
      this.scenario.data.scenarioItems = this.scenario.data.scenarioItems.filter(
        (item) => item.id != itemRef.id
      );
    }
  }

  getNextItemId() {
    return Math.max(...this.scenario.data.scenarioItems.map((i) => i.id)) + 1;
  }

  addAnswer(itemRef: scenarioItem, from?: scenarioItemFrom): void {
    let fromType = from || SCENARIO_ITEM_FROM_CLIENT;
    if (from == undefined && itemRef.from == SCENARIO_ITEM_FROM_CLIENT) {
      fromType = SCENARIO_ITEM_FROM_BOT;
    }

    let newEntry: scenarioItem = {
      id: this.getNextItemId(),
      parentIds: [itemRef.id],
      from: fromType,
      text: ''
    };

    this.scenario.data.scenarioItems.push(newEntry);

    setTimeout(() => {
      this.selectItem(newEntry);
      this.scenarioDesignerService.requireItemPosition(newEntry);
    }, 0);
  }

  selectedItem: scenarioItem;

  selectItem(item?: scenarioItem): void {
    this.selectedItem = item ? item : undefined;
  }

  @HostListener('window:keydown', ['$event'])
  onKeyPress($event: KeyboardEvent): void {
    if (this.selectedItem) {
      if ($event.altKey) {
        if ($event.key == 'c') {
          this.addAnswer(this.selectedItem, SCENARIO_ITEM_FROM_CLIENT);
        }
        if ($event.key == 'b') {
          this.addAnswer(this.selectedItem, SCENARIO_ITEM_FROM_BOT);
        }
        if ($event.key == 'n') {
          this.addAnswer(this.selectedItem);
        }
      }
    }
  }

  itemDropped(targetId: number, droppedId: number): void {
    let targeted = this.findItemById(targetId);
    let dropped = this.findItemById(droppedId);

    if (dropped.parentIds === undefined) return;
    if (dropped.parentIds.includes(targetId)) return;
    if (this.isInFiliation(targeted, dropped)) return;

    if (this.findItemChild(dropped)) dropped.parentIds = [targetId];
    else dropped.parentIds.push(targetId);
  }

  isInFiliation(parent: scenarioItem, child: scenarioItem): boolean {
    let current = parent;
    while (true) {
      if (!current.parentIds) return false;
      current = this.findItemById(current.parentIds[0]);
      if (!current) return false;
      if (current.id == child.id) return true;
    }
  }

  mouseWheel(event: WheelEvent) {
    event.preventDefault();
    this.zoomCanvas(event);
  }

  canvasPos = { x: 0, y: 0 };
  canvasPosOffset = { x: 0, y: 0 };
  pointer = { x: 0, y: 0 };
  canvasScale = 1;
  zoomSpeed = 0.5;
  isDragingCanvas;

  zoomCanvas(event: WheelEvent) {
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

  centerCanvas(): void {
    let wrapper = this.canvasWrapperElem.nativeElement;
    let canvas = this.canvasElem.nativeElement;
    this.canvasPos.x = ((canvas.offsetWidth * this.canvasScale - wrapper.offsetWidth) / 2) * -1;
    this.canvasPos.y = 0;

    canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
  }

  centerOnItem(item, position, setFocus = true) {
    let wrapper = this.canvasWrapperElem.nativeElement;
    let canvas = this.canvasElem.nativeElement;
    this.canvasPos.x =
      position.left * this.canvasScale * -1 + (wrapper.offsetWidth - position.width) / 2;
    this.canvasPos.y =
      position.top * this.canvasScale * -1 + (wrapper.offsetHeight - position.height) / 2;

    canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;

    if (setFocus) {
      setTimeout(() => {
        this.scenarioDesignerService.focusItem(item);
      }, CANVAS_TRANSITION_TIMING);
    }
  }

  @HostListener('mousedown', ['$event'])
  onMouseDownCanvas(event: MouseEvent) {
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
  @HostListener('mouseup', ['$event'])
  onMouseUpCanvas(event: MouseEvent) {
    if (event.button == 0) {
      this.isDragingCanvas = undefined;
      let canvas = this.canvasElem.nativeElement;
      canvas.style.transition = `transform .${CANVAS_TRANSITION_TIMING}s`;
    }
  }
  @HostListener('mousemove', ['$event'])
  onMouseMoveCanvas(event: MouseEvent) {
    if (this.isDragingCanvas && event.button == 0) {
      let canvas = this.canvasElem.nativeElement;
      const dx = event.clientX - this.isDragingCanvas.x;
      const dy = event.clientY - this.isDragingCanvas.y;
      this.canvasPos.x = this.isDragingCanvas.left + dx;
      this.canvasPos.y = this.isDragingCanvas.top + dy;
      canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
    }
  }

  chatDisplayed = false;
  chatControlsDisplay = false;
  chatControlsFrom;
  chatPropositions;

  stopPropagation(event: MouseEvent, preventDefault = true): void {
    if (preventDefault) event.preventDefault();
    event.stopPropagation();
  }

  closeChat(): void {
    this.chatDisplayed = false;
    this.chatPropositions = undefined;
    this.chatControlsFrom = undefined;
    this.chatControlsDisplay = false;
  }

  testStory(item?): void {
    this.messages = [];

    if (!item) {
      item = this.findItemById(0);
    }

    this.chatPropositions = undefined;
    this.chatControlsFrom = undefined;
    this.chatControlsDisplay = false;
    this.processChatEntry(item);
    this.chatDisplayed = true;
  }

  chatResponsesTimeout = 1000;

  processChatEntry(item: scenarioItem): void {
    if (item) {
      this.addChatMessage(item.from, item.text);

      let children = this.getChildren(item);
      if (children.length < 1) return;

      if (children.length == 1) {
        setTimeout(() => {
          this.processChatEntry(children[0]);
        }, this.chatResponsesTimeout);
      } else {
        this.makePropositions(children[0]);
      }
    }
  }

  makePropositions(item: scenarioItem): void {
    this.chatPropositions = this.getBrotherhood(item);
    let from = item.from;
    this.chatControlsFrom = from;
    this.chatControlsDisplay = true;
  }

  chooseProposition(item: scenarioItem): void {
    this.chatPropositions = undefined;
    this.chatControlsFrom = undefined;
    this.chatControlsDisplay = false;
    this.addChatMessage(item.from, item.text);
    let child = this.findItemChild(item);
    if (child) {
      setTimeout(() => {
        this.processChatEntry(child);
      }, this.chatResponsesTimeout);
    }
  }

  findItemChild(item: scenarioItem): scenarioItem {
    return this.scenario.data.scenarioItems.find((oitem) => oitem.parentIds?.includes(item.id));
  }

  findItemById(id: number): scenarioItem {
    return this.scenario.data.scenarioItems.find((oitem) => oitem.id == id);
  }

  getChildren(item: scenarioItem): scenarioItem[] {
    return this.scenario.data.scenarioItems.filter((oitem) => oitem.parentIds?.includes(item.id));
  }

  getBrotherhood(item: scenarioItem): scenarioItem[] {
    return this.scenario.data.scenarioItems.filter((oitem) =>
      oitem.parentIds?.some((oip) => item.parentIds?.includes(oip))
    );
  }

  getItemBrothers(item: scenarioItem): scenarioItem[] {
    return this.getBrotherhood(item).filter((oitem) => oitem.id !== item.id);
  }

  isItemOnlyChild(item: scenarioItem): boolean {
    if (!this.getItemBrothers(item).length) return true;
    return false;
  }

  userIdentities = {
    client: { name: 'Pierre Martin', avatar: 'assets/images/scenario-client.svg' },
    bot: { name: 'Bot', avatar: 'assets/images/scenario-bot.svg' },
    api: { name: 'Vérification', avatar: 'assets/images/scenario-verification.svg' }
  };

  addChatMessage(from: string, text: string, type: string = 'text'): void {
    let user;
    switch (from) {
      case SCENARIO_ITEM_FROM_CLIENT:
        user = this.userIdentities[SCENARIO_ITEM_FROM_CLIENT];
        break;
      case SCENARIO_ITEM_FROM_BOT:
        user = this.userIdentities[SCENARIO_ITEM_FROM_BOT];
        break;
    }

    this.messages.push({
      text: text,
      date: new Date(),
      reply: from == SCENARIO_ITEM_FROM_CLIENT ? true : false,
      type: type,
      user: user
    });
  }

  messages: any[] = [];

  ngOnDestroy() {
    this.destroy.next();
    this.destroy.complete();
  }

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any) {
    if (!this.canDeactivate()) {
      $event.returnValue = true;
    }
  }

  canDeactivate() {
    return this.scenarioBackup == this.stringifiedCleanScenario();
  }
}

@Injectable()
export class ScenarioDesignerNavigationGuard implements CanDeactivate<any> {
  constructor(private dialogService: DialogService) {}

  canDeactivate(component: any) {
    const canDeactivate = component.canDeactivate();

    if (!canDeactivate) {
      const subject = new Subject<boolean>();
      const dialogResponseVerb = 'Exit';
      const modal = this.dialogService.openDialog(ConfirmDialogComponent, {
        context: {
          title: `You're about to leave without saving the changes`,
          subtitle: 'Are you sure?',
          action: dialogResponseVerb
        }
      });
      modal.onClose.subscribe((res) => {
        subject.next(res == dialogResponseVerb.toLowerCase());
      });

      return subject.asObservable();
    }
    return true;
  }
}
