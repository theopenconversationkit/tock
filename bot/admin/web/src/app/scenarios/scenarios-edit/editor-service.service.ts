import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { scenarioItem } from '../models/scenario.model';

@Injectable({
  providedIn: 'root'
})
export class EditorServiceService {
  public editorItemsCommunication = new BehaviorSubject<any>({});
  constructor() {}

  // Child components to editor communication
  addAnswer(item: scenarioItem): void {
    this.editorItemsCommunication.next({
      type: 'addAnswer',
      item: item
    });
  }

  deleteAnswer(item: scenarioItem, parentItemId: number): void {
    this.editorItemsCommunication.next({
      type: 'deleteAnswer',
      item: item,
      parentItemId: parentItemId
    });
  }

  itemDropped(targetId: number, droppedId: number): void {
    this.editorItemsCommunication.next({
      type: 'itemDropped',
      targetId: targetId,
      droppedId: droppedId
    });
  }

  selectItem(item: scenarioItem): void {
    this.editorItemsCommunication.next({
      type: 'itemSelected',
      item: item
    });
  }

  testItem(item: scenarioItem): void {
    this.editorItemsCommunication.next({
      type: 'testItem',
      item: item
    });
  }

  exposeItemPosition(
    item: scenarioItem,
    position: { left: number; top: number; width: number; height: number }
  ) {
    this.editorItemsCommunication.next({
      type: 'exposeItemPosition',
      item: item,
      position: position
    });
  }

  // Editor to child components communication
  focusItem(item: scenarioItem): void {
    this.editorItemsCommunication.next({
      type: 'focusItem',
      item: item
    });
  }

  requireItemPosition(item: scenarioItem): void {
    this.editorItemsCommunication.next({
      type: 'requireItemPosition',
      item: item
    });
  }
}
