import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { scenarioItem } from '../models/scenario.model';

@Injectable({
  providedIn: 'root'
})
export class ScenarioDesignerService {
  public scenarioDesignerItemsCommunication = new Subject<any>();
  constructor() {}

  // Child components to designer communication
  addAnswer(item: scenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'addAnswer',
      item: item
    });
  }

  deleteAnswer(item: scenarioItem, parentItemId: number): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'deleteAnswer',
      item: item,
      parentItemId: parentItemId
    });
  }

  itemDropped(targetId: number, droppedId: number): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'itemDropped',
      targetId: targetId,
      droppedId: droppedId
    });
  }

  selectItem(item: scenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'itemSelected',
      item: item
    });
  }

  testItem(item: scenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'testItem',
      item: item
    });
  }

  exposeItemPosition(
    item: scenarioItem,
    position: { left: number; top: number; width: number; height: number }
  ) {
    this.scenarioDesignerItemsCommunication.next({
      type: 'exposeItemPosition',
      item: item,
      position: position
    });
  }

  // Designer to child components communication
  focusItem(item: scenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'focusItem',
      item: item
    });
  }

  requireItemPosition(item: scenarioItem): void {
    this.scenarioDesignerItemsCommunication.next({
      type: 'requireItemPosition',
      item: item
    });
  }
}
