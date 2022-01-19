/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import {Injectable} from '@angular/core';
import {empty, Observable, ReplaySubject, Subject} from 'rxjs';
import { concatMap, debounceTime, delay } from 'rxjs/operators';
import {map, take, takeUntil, tap, filter, mergeMap} from 'rxjs/operators';
import {FaqDefinition} from '../../common/model/faq-definition';

/**
 * Q&A Editor Dialog related service and types
 *
 * Why: Decoupling event emitter from actual action handler
 **/

// Which action it is
type ActionName = 'save' | 'exit-edit-mode';

// Which action result it is
type OutcomeName = 'cancel-save' | 'save-done' | 'adhoc-action-done';


// event
export type QaEditorEvent = {
  transactionId: number,
  name:  ActionName | OutcomeName,
  payload?: FaqDefinition
};

// produce outcome for a specific event
export type ActionResult = {outcome: OutcomeName, payload?: FaqDefinition};
export type ActionHandler = (QaEditorEvent) => Observable<ActionResult>;

export type EventListener= (QaEditorEvent) => void;

const newAction = (() => {
  let idGenerator = 1;

  return (name: ActionName, payload?: FaqDefinition) => {
    return {
      name,
      transactionId: ++idGenerator,
      payload
    };
  };
})();


const isInitiatedBy = (action: QaEditorEvent) => (evt: QaEditorEvent) => evt.transactionId === action.transactionId;

const hasName = (name: ActionName | OutcomeName) => (evt: QaEditorEvent) => evt.name === name;

const toFaqDefinition = (evt: QaEditorEvent) => <FaqDefinition> evt.payload;

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
  public save(cancel$: Observable<any> = empty()): Promise<FaqDefinition> {
    const actionEvt = newAction('save');

    const result =  new Promise<FaqDefinition>((resolve, reject) => {
      this.takeActionOutcome(actionEvt, cancel$).subscribe(evt => {
        if (evt.name === 'save-done') {
          resolve(<FaqDefinition> evt.payload);
        } else {
          reject(evt.name);
        }
      })
    });
    this.action$.next(actionEvt);

    return result;
  }

  public leaveEditMode(): void {
    const actionEvt = newAction('exit-edit-mode');
    this.action$.next(actionEvt);
  }

  /**
   * Perform action and publish its result in a "outcome" channel
   *
   * Note: Do not register multiple handlers for a common action as it is unexpected behavior yet
   * @param name
   * @param cancel$
   * @param handler
   */
  public registerActionHandler(name: ActionName, cancel$: Observable<any>, handler: ActionHandler) {
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

  /**
   * Listen if a specific outcome happened
   * @param outcome Action outcome
   * @param delayMillis Wait some milliseconds before playing side effect (example: wait database updates)
   * @param cancel$ Cancellation trigger
   * @param listener Listen function
   */
  public registerOutcomeListener(outcome: OutcomeName,
                                 delayMillis: number,
                                 cancel$: Observable<any>,
                                 listener: EventListener): void
  {
    this.outcome$.pipe(
      takeUntil(cancel$),
      filter(hasName(outcome)),
      delay(delayMillis),
      debounceTime(delayMillis),
    ).subscribe(listener.bind(this));

  }
}
