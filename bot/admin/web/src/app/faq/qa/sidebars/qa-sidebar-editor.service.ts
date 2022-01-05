import {Injectable} from '@angular/core';

import {empty, Observable, ReplaySubject, Subject} from 'rxjs';
import {map, take, takeUntil, tap, filter} from 'rxjs/operators';
import {FrequentQuestion} from '../../common/model/frequent-question';

/**
 * Q&A Editor Dialog related service and types
 */


// Which action it is
type ActionName = 'save' | 'cancel-save' | 'switch-tab' | 'save-done';

// Specific action payload
export type EditorTabName = 'Info' | 'Answer' | 'Question';

// Action = name + payload
export type QaEditorAction = {
  name:  ActionName,
  payload?: FrequentQuestion | EditorTabName /* Proper sub typing retreival is partially enforced by this service */
};

const hasName = (name: ActionName) => (action: QaEditorAction) => action.name === name;

const toFrequentQuestion = (action: QaEditorAction) => <FrequentQuestion> action.payload;
const toEditorTabName = (action: QaEditorAction) => <EditorTabName> action.payload;

/**
 * Controlling interactions between parts of the 0&A Editor Side Bar
 */
@Injectable()
export class QaSidebarEditorService {

  private action$: Subject<QaEditorAction> = new Subject<QaEditorAction>();

  constructor() {
  }

  /**
   * Listen to any event
   */
  when(name: ActionName, cancel$: Observable<any> = empty()): Observable<QaEditorAction> {
    return this.action$.pipe(
      filter(hasName(name)),
      takeUntil(cancel$),
    );
  }

  /**
   * Listen to 'switch-tab' event
   */
  whenSwitchTab(cancel$: Observable<any> = empty()): Observable<EditorTabName> {
    return this.when('switch-tab', cancel$)
      .pipe(
        takeUntil(cancel$),
        map(toEditorTabName)
      );
  }

  /**
   * Trigger 'cancel-save' event
   */
  cancelSave(): void {
    this.action$.next({
      name: 'cancel-save'
    });
  }

  /**
   * Trigger 'switch-tab' event
   */
  switchTab(tabName: EditorTabName): void {
    console.log("switchTab", tabName);
    this.action$.next({
      name: 'switch-tab',
      payload: tabName
    });
  }

  /**
   * Trigger 'save' event
   */
  save(cancel$: Observable<any> = empty()): Promise<FrequentQuestion> {

    // listen if someone saved the thing
    const result = this.action$.pipe(
      filter(hasName('save-done')),
      take(1), takeUntil(cancel$),
      map(toFrequentQuestion)
    ).toPromise();

    // ask to save that thing
    this.action$.next({
      name: 'save'
    });

    // get the ear which is listening
    return result;
  }

  /**
   * Trigger 'save-done' event
   */
  saveDone(fq: FrequentQuestion): void {
    this.action$.next({
      name: 'save-done',
      payload: fq
    });
  }

}
