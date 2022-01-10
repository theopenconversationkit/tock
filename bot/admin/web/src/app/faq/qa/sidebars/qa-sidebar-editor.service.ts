import {Injectable} from '@angular/core';

import {empty, Observable, ReplaySubject, Subject} from 'rxjs';
import { concatMap } from 'rxjs/operators';
import {map, take, takeUntil, tap, filter, mergeMap} from 'rxjs/operators';
import {FrequentQuestion} from '../../common/model/frequent-question';

/**
 * Q&A Editor Dialog related service and types
 *
 * Why: Decoupling event emitter from actual action handler
 **/

// Which action it is
type ActionName = 'save' | 'some-example-action';

// Which action result it is
type OutcomeName = 'cancel-save' | 'save-done';


// event
export type QaEditorEvent = {
  transactionId: number,
  name:  ActionName | OutcomeName,
  payload?: FrequentQuestion /* Proper sub typing retreival is partially enforced by this service */
};

// produce outcome for a specific event
export type ActionResult = {outcome: OutcomeName, payload?: FrequentQuestion};

export type ActionHandler = (QaEditorEvent) => Observable<ActionResult>;

const newAction = (() => {
  let idGenerator = 1;

  return (name: ActionName, payload?: FrequentQuestion) => {
    return {
      name,
      transactionId: ++idGenerator,
      payload
    };
  };
})();


const isInitiatedBy = (action: QaEditorEvent) => (evt: QaEditorEvent) => evt.transactionId === action.transactionId;

const hasName = (name: ActionName | OutcomeName) => (evt: QaEditorEvent) => evt.name === name;

const toFrequentQuestion = (evt: QaEditorEvent) => <FrequentQuestion> evt.payload;

/**
 * Controlling interactions between parts of the 0&A Editor Side Bar
 */
@Injectable()
export class QaSidebarEditorService {

  private action$: Subject<QaEditorEvent> = new Subject<QaEditorEvent>();

  private outcome$: Subject<QaEditorEvent> = new Subject<QaEditorEvent>();

  constructor() {
  }

  private takeActionOutcome(action: QaEditorEvent, cancel$: Observable<any> = empty()): Observable<QaEditorEvent> {
    return this.outcome$.pipe(
      takeUntil(cancel$),
      filter(isInitiatedBy(action)),
      take(1)
    );
  }

  /**
   * Called by button to trigger a save and wait for its response
   *
   * Note: Trigger 'save' event then await its outcome
   */
  public save(cancel$: Observable<any> = empty()): Promise<FrequentQuestion> {
    const actionEvt = newAction('save');

    const result =  new Promise<FrequentQuestion>((resolve, reject) => {
      this.takeActionOutcome(actionEvt, cancel$).subscribe(evt => {
        if (evt.name === 'save-done') {
          resolve(<FrequentQuestion> evt.payload);
        } else {
          reject(evt.name);
        }
      })
    });
    this.action$.next(actionEvt);

    return result;
  }

  /**
   * Perform action and publish its result in a "outcome" channel
   *
   * Note: Do not register multiple handlers for a common action as it is unexpected behavior yet
   * @param name
   * @param cancel$
   * @param handler
   */
  public registerActionHandler(name: ActionName, cancel$: Observable<any> = empty(), handler: ActionHandler) {
    return this.action$.pipe(
      takeUntil(cancel$),
      filter(hasName(name)),
      concatMap(action => {
        return handler(action).pipe(map(
          res => {
            const outcome: QaEditorEvent = {
              transactionId: action.transactionId,
              name: res.outcome,
              payload: res.payload
            };
            return outcome;
          }
        ));
      })
    ).subscribe(this.outcome$.next.bind(this.outcome$));
  }
}
