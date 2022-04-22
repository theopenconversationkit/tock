import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { storyCollectorItem } from './story-collector.types';

@Injectable({
  providedIn: 'root'
})
export class EditorServiceService {
  public editorItemsCommunication = new BehaviorSubject<any>({});
  constructor() {}

  // Child components to editor communication
  addAnswer(item: storyCollectorItem): void {
    this.editorItemsCommunication.next({
      type: 'addAnswer',
      item: item
    });
  }

  deleteAnswer(item: storyCollectorItem, parentItemId: number): void {
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

  selectItem(item: storyCollectorItem): void {
    this.editorItemsCommunication.next({
      type: 'itemSelected',
      item: item
    });
  }

  testItem(item: storyCollectorItem): void {
    this.editorItemsCommunication.next({
      type: 'testItem',
      item: item
    });
  }

  // Editor to child components communication
  focusItem(item: storyCollectorItem): void {
    this.editorItemsCommunication.next({
      type: 'focusItem',
      item: item
    });
  }
}
