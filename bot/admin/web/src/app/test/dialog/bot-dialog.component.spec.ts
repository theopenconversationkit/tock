/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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
import { of, throwError } from 'rxjs';
import { BotDialogComponent } from './bot-dialog.component';
import { BotDialogResponse } from '../model/test';
import { ParseQuery } from '../../model/nlp';

describe('BotDialogComponent save in Inbox', () => {
  let component: BotDialogComponent;
  let nlp: jasmine.SpyObj<any>;
  let test: jasmine.SpyObj<any>;
  let toastr: jasmine.SpyObj<any>;
  let userLocale: string;

  beforeEach(() => {
    userLocale = 'fr';
    nlp = jasmine.createSpyObj('NlpService', ['parse']);
    nlp.parse.and.returnValue(of({}));
    test = jasmine.createSpyObj('TestService', ['talk']);
    test.talk.and.callFake(() => of(new BotDialogResponse([], false, 'actionId', userLocale)));
    toastr = jasmine.createSpyObj('NbToastrService', ['show', 'warning']);
    const state = {
      currentApplication: { namespace: 'app', name: 'bot', supportedLocales: ['fr', 'en'] },
      currentLocale: 'fr',
      hasRole: () => true
    };
    const shared = { session_storage: {} };
    const transloco = { translate: (key: string) => key };
    component = new BotDialogComponent(state as any, test as any, null, shared as any, toastr as any, null, nlp as any, transloco as any);
    component.currentConfigurationId = 'conf';
    component.saveSentences = true;
  });

  function send(text: string) {
    component.userMessage = text;
    component.submit();
  }

  function parsedQuery(): ParseQuery {
    return nlp.parse.calls.mostRecent().args[0];
  }

  it('saves the typed sentence in the locale detected by the bot, without displaying the errors', () => {
    userLocale = 'en';
    send(' hello ');

    expect(nlp.parse).toHaveBeenCalledTimes(1);
    expect(parsedQuery().query).toEqual('hello');
    expect(parsedQuery().language).toEqual('en');
    expect(nlp.parse.calls.mostRecent().args[1]).toBeTrue();
  });

  it('uses the current locale when the detected locale is not supported by the application', () => {
    userLocale = 'de';
    send('hallo');

    expect(parsedQuery().language).toEqual('fr');
  });

  it('does not save the built-in keywords', () => {
    ['_test_', '_end_test_', '_restart_', '_xray_ my test, JIRA-1', '_xray_update_ TEST-1'].forEach((k) => send(k));
    component.enableTestContext();
    component.enableTestContext();

    expect(test.talk).toHaveBeenCalledTimes(7);
    expect(nlp.parse).not.toHaveBeenCalled();
  });

  it('does not save the messages sent by the buttons', () => {
    component.onNewMessage({ text: 'yes' } as any);

    expect(nlp.parse).not.toHaveBeenCalled();
  });

  it('does not save when the option is disabled', () => {
    component.saveSentences = false;
    send('hello');

    expect(nlp.parse).not.toHaveBeenCalled();
  });

  it('warns when the sentence could not be saved', () => {
    nlp.parse.and.returnValue(throwError(() => 'error'));
    send('hello');

    expect(toastr.warning).toHaveBeenCalledTimes(1);
  });
});
