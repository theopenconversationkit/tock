import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

import { OffsetPosition } from '../../../shared/canvas/models';
import { ScenarioItem, ScenarioItemFrom } from '../../models/scenario.model';

@Injectable({
  providedIn: 'root'
})
export class ScenarioConceptionService {
  public scenarioDesignerItemsCommunication = new Subject<any>();

  // Child components to designer communication
  addAnswer(item: ScenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'addAnswer',
      item
    });
  }

  deleteAnswer(item: ScenarioItem, parentItemId: number): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'deleteAnswer',
      item,
      parentItemId
    });
  }

  itemDropped(targetId: number, droppedId: number): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'itemDropped',
      targetId,
      droppedId
    });
  }

  selectItem(item: ScenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'itemSelected',
      item
    });
  }

  testItem(item: ScenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'testItem',
      item
    });
  }

  exposeItemPosition(item: ScenarioItem, position: OffsetPosition): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'exposeItemPosition',
      item,
      position
    });
  }

  changeItemType(item: ScenarioItem, targetType: ScenarioItemFrom): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'changeItemType',
      item,
      targetType
    });
  }

  removeItemDefinition(item: ScenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'removeItemDefinition',
      item
    });
  }

  // Designer to child components communication
  focusItem(item: ScenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'focusItem',
      item
    });
  }

  requireItemPosition(item: ScenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'requireItemPosition',
      item
    });
  }
}
