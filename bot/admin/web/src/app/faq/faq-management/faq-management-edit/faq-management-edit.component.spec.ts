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

import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { SimpleChange } from '@angular/core';
import { By } from '@angular/platform-browser';
import { FormControl } from '@angular/forms';
import {
  NbAlertModule,
  NbAutocompleteModule,
  NbBadgeModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbDialogRef,
  NbFormFieldModule,
  NbIconModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTabsetModule,
  NbTagModule,
  NbTooltipModule
} from '@nebular/theme';
import { of } from 'rxjs';

import { DialogService } from '../../../core-nlp/dialog.service';
import { NlpService } from '../../../core-nlp/nlp.service';
import { StateService } from '../../../core-nlp/state.service';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { FaqManagementEditComponent, FaqTabs } from './faq-management-edit.component';
import { FormControlComponent } from '../../../shared/components';
import { FaqDefinitionExtended } from '../faq-management.component';
import { Classification, Intent, PaginatedResult, Sentence, SentenceStatus } from '../../../model/nlp';

const mockSentences: Sentence[] = [
  {
    text: 'sentence 1',
    status: SentenceStatus.validated,
    classification: <Classification>{
      intentId: '1',
      intentProbability: 1,
      entitiesProbability: 1
    },
    creationDate: new Date('2022-08-03T09:50:24.952Z'),
    getIntentLabel(_state) {
      return 'intent label';
    }
  } as Sentence,
  {
    text: 'sentence 2',
    status: SentenceStatus.inbox,
    classification: <Classification>{
      intentId: '1',
      intentProbability: 1,
      entitiesProbability: 1
    },
    creationDate: new Date('2022-08-03T09:50:24.952Z'),
    getIntentLabel(_state) {
      return 'intent label';
    }
  } as Sentence,
  {
    text: 'sentence 3',
    status: SentenceStatus.inbox,
    classification: <Classification>{
      intentId: '1',
      intentProbability: 1,
      entitiesProbability: 1
    },
    creationDate: new Date('2022-08-03T09:50:24.952Z'),
    getIntentLabel(_state) {
      return 'intent label';
    }
  } as Sentence,
  {
    text: 'sentence 4',
    status: SentenceStatus.inbox,
    classification: <Classification>{
      intentId: '1',
      intentProbability: 1,
      entitiesProbability: 1
    },
    creationDate: new Date('2022-08-03T09:50:24.952Z'),
    getIntentLabel(_state) {
      return 'intent label';
    }
  } as Sentence
];

const mockFaq: FaqDefinitionExtended = {
  id: '1',
  applicationName: 'app',
  enabled: true,
  language: 'fr',
  title: 'title faq',
  description: 'description',
  tags: ['tag 1'],
  utterances: ['question 1', 'question 2'],
  answer: 'answer'
};

const mockSentencesPaginatedResult: PaginatedResult<Sentence> = {
  end: mockSentences.length,
  rows: mockSentences,
  start: 0,
  total: mockSentences.length
};

class NlpServiceMock {
  searchSentences() {
    return of(mockSentencesPaginatedResult);
  }
}

class MockState {
  createPaginatedQuery() {
    return {
      namespace: 'app',
      applicationName: 'app',
      language: 'fr',
      start: 0,
      size: 1000
    };
  }

  findIntentById(): Intent {
    return {
      name: 'intentAssociate'
    } as Intent;
  }

  intentExists(val: string): boolean {
    return val === 'titlefaq';
  }

  intentExistsInOtherApplication() {}
}

describe('FaqManagementEditComponent', () => {
  let component: FaqManagementEditComponent;
  let fixture: ComponentFixture<FaqManagementEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqManagementEditComponent, FormControlComponent],
      imports: [
        TestSharedModule,
        NbAlertModule,
        NbAutocompleteModule,
        NbTabsetModule,
        NbTagModule,
        NbSpinnerModule,
        NbBadgeModule,
        NbCheckboxModule,
        NbIconModule,
        NbFormFieldModule,
        NbSelectModule,
        NbCardModule,
        NbButtonModule,
        NbTooltipModule
      ],
      providers: [
        { provide: StateService, useClass: MockState },
        { provide: DialogService, useValue: { openDialog: () => ({ onClose: (val: any) => of(val) }) } },
        { provide: NlpService, useClass: NlpServiceMock }
      ]
    }).compileComponents();
  });

  beforeEach(fakeAsync(() => {
    fixture = TestBed.createComponent(FaqManagementEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    tick(100);
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('new faq', () => {
    it('should initialize the current tab on the info part', () => {
      const nameElement = fixture.debugElement.query(By.css('[data-testid="name"]'));
      const descriptionElement = fixture.debugElement.query(By.css('[data-testid="description"]'));
      const tagsElement = fixture.debugElement.query(By.css('[data-testid="tags"]'));
      const utterrancesElement = fixture.debugElement.query(By.css('[data-testid="question"]'));
      const answerElement = fixture.debugElement.query(By.css('[data-testid="anwser"]'));

      expect(component.currentTab).toBe(FaqTabs.INFO);
      expect(nameElement).toBeTruthy();
      expect(descriptionElement).toBeTruthy();
      expect(tagsElement).toBeTruthy();
      expect(utterrancesElement).toBeFalsy();
      expect(answerElement).toBeFalsy();
    });

    it('should initialize an empty form', () => {
      const faq: FaqDefinitionExtended = {
        id: undefined,
        intentId: undefined,
        title: '',
        description: '',
        utterances: [],
        tags: [],
        answer: '',
        enabled: true,
        applicationName: 'app',
        language: 'fr'
      };
      component.ngOnChanges({ faq: new SimpleChange(null, faq, true) });
      fixture.detectChanges();

      expect(component.form.valid).toBeFalse();
      expect(component.form.value).toEqual({
        title: '',
        description: '',
        tags: [],
        utterances: [],
        answer: ''
      });
    });

    it('should initialize the form when the result comes from the training page', fakeAsync(() => {
      const faq: FaqDefinitionExtended = {
        id: undefined,
        intentId: undefined,
        title: 'test',
        description: '',
        utterances: [],
        tags: [],
        answer: '',
        enabled: true,
        applicationName: 'app',
        language: 'fr',
        _initQuestion: 'test'
      };
      component.ngOnChanges({ faq: new SimpleChange(null, faq, true) });
      fixture.detectChanges();

      tick(100);

      expect(component.form.dirty).toBeTrue();
      expect(component.form.valid).toBeFalse();
      expect(component.form.value).toEqual({
        title: 'test',
        description: '',
        tags: [],
        utterances: ['test'],
        answer: ''
      });
    }));
  });

  describe('edit faq', () => {
    it('should initialize the current tab on the info part', () => {
      component.ngOnChanges({ faq: new SimpleChange(null, mockFaq, true) });
      fixture.detectChanges();
      const nameElement = fixture.debugElement.query(By.css('[data-testid="name"]'));
      const descriptionElement = fixture.debugElement.query(By.css('[data-testid="description"]'));
      const tagsElement = fixture.debugElement.query(By.css('[data-testid="tags"]'));
      const utterrancesElement = fixture.debugElement.query(By.css('[data-testid="question"]'));
      const answerElement = fixture.debugElement.query(By.css('[data-testid="anwser"]'));

      expect(component.currentTab).toBe(FaqTabs.INFO);
      expect(nameElement).toBeTruthy();
      expect(descriptionElement).toBeTruthy();
      expect(tagsElement).toBeTruthy();
      expect(utterrancesElement).toBeFalsy();
      expect(answerElement).toBeFalsy();
    });

    it('should initialize a form with the correct value', () => {
      component.ngOnChanges({ faq: new SimpleChange(null, mockFaq, true) });
      fixture.detectChanges();
      expect(component.form.value).toEqual({
        title: 'title faq',
        description: 'description',
        tags: ['tag 1'],
        utterances: ['question 1', 'question 2'],
        answer: 'answer'
      });
    });
  });

  it('should associate validators to the title', () => {
    expect(component.title.valid).toBeFalse();

    // title field is required
    expect(component.title.errors.required).toBeTrue();
    expect(component.title.errors.maxlength).toBeFalsy();
    expect(component.title.errors.minlength).toBeFalsy();
    expect(component.title.valid).toBeFalse();

    // set title to short text (less than 6 characters)
    component.title.setValue('test');
    expect(component.title.errors.required).toBeFalsy();
    expect(component.title.errors.maxlength).toBeFalsy();
    expect(component.title.errors.minlength).toBeTruthy();
    expect(component.title.errors.minlength.requiredLength).toBe(6);
    expect(component.title.valid).toBeFalse();

    // set title to long text (upper than 40 characters)
    component.title.setValue('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa');
    expect(component.title.errors.required).toBeFalsy();
    expect(component.title.errors.minlength).toBeFalsy();
    expect(component.title.errors.maxlength).toBeTruthy();
    expect(component.title.errors.maxlength.requiredLength).toBe(40);
    expect(component.title.valid).toBeFalse();

    // set title to something correct
    component.title.setValue('correct value');
    expect(component.title.errors).toBeFalsy();
    expect(component.title.valid).toBeTrue();
  });

  it('should associate validators to the description', () => {
    expect(component.description.valid).toBeTrue();

    // set description to long text (upper than 500 characters)
    component.description.setValue(
      'llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll'
    );
    expect(component.description.errors.maxlength).toBeTruthy();
    expect(component.description.valid).toBeFalse();
    expect(component.description.errors.maxlength.requiredLength).toBe(500);

    // set description to something correct
    component.description.setValue('correct value');
    expect(component.description.errors).toBeFalsy();
    expect(component.description.valid).toBeTrue();
  });

  it('should associate validators to the utterances', () => {
    expect(component.utterances.valid).toBeFalse();

    // utterances is required
    expect(component.utterances.errors.required).toBeTruthy();

    // set utterances to something correct
    component.utterances.push(new FormControl('test'));
    expect(component.utterances.errors).toBeFalsy();
    expect(component.utterances.valid).toBeTrue();
  });

  it('should associate validators to the answer', () => {
    expect(component.answer.valid).toBeFalse();

    // answer is required
    expect(component.answer.errors.required).toBeTruthy();
    expect(component.answer.errors.maxlength).toBeFalsy();

    // set answer to long text (upper than 960)
    component.answer.setValue(
      'llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll'
    );
    expect(component.answer.errors.required).toBeFalsy();
    expect(component.answer.errors.maxlength).toBeTruthy();
    expect(component.answer.errors.maxlength.requiredLength).toBe(960);

    // set answer to something correct
    component.answer.setValue('correct value');
    expect(component.answer.errors).toBeFalsy();
    expect(component.answer.valid).toBeTrue();
  });

  it('should reset alert for utterance when the method is called', () => {
    component.existingUterranceInOtherintent = 'utterance';
    component.intentNameExistInApp = true;

    component.resetAlerts();

    expect(component.existingUterranceInOtherintent).toBeUndefined();
    expect(component.intentNameExistInApp).toBeUndefined();
  });

  describe('#addUtterance', () => {
    it('should add utterance to the list', () => {
      component.currentTab = FaqTabs.QUESTION;
      fixture.detectChanges();
      expect(component.utterances.value).toHaveSize(0);

      component.addUtterance('test');

      expect(component.utterances.value).toHaveSize(1);
      expect(component.utterances.value).toEqual(['test']);
    });

    it('should not add utterance to the list when it is already present', () => {
      component.currentTab = FaqTabs.QUESTION;
      fixture.detectChanges();
      const utterances = ['test', 'test 1', 'ok'];
      utterances.forEach((utterance) => {
        component.utterances.push(new FormControl(utterance));
      });

      component.addUtterance('test');

      expect(component.utterances.value).toEqual(utterances);
    });

    it('should not add utterance to the list when the utterance is already associated with another intent', () => {
      component.faq = mockFaq;
      component.currentTab = FaqTabs.QUESTION;
      fixture.detectChanges();
      expect(component.utterances.value).toHaveSize(0);

      component.addUtterance('sentence 1');

      expect(component.utterances.value).toHaveSize(0);
    });

    it('should display an error message when the utterance being added is already associated with another intent', () => {
      component.faq = mockFaq;
      component.currentTab = FaqTabs.QUESTION;
      fixture.detectChanges();

      component.addUtterance('sentence 1');
      fixture.detectChanges();

      const alertElement: HTMLElement = fixture.debugElement.query(
        By.css('[data-testid="existing-uterrance-in-other-intent"]')
      ).nativeElement;
      const alertMessageElement: HTMLSpanElement = alertElement.querySelector('[data-testid="alert-message"]');
      expect(alertElement).toBeTruthy();
      expect(alertMessageElement.textContent.trim()).toBe(
        'Addition cancelled. This Sentence is already associated with the intent : "intentAssociate"'
      );
    });
  });

  it('#removeUtterance should remove utterance from the list', () => {
    component.ngOnChanges({ faq: new SimpleChange(null, mockFaq, true) });
    fixture.detectChanges();

    expect(component.utterances.value).toHaveSize(2);
    expect(component.utterances.value).toEqual(['question 1', 'question 2']);

    component.removeUtterance('question 1');

    expect(component.utterances.value).toHaveSize(1);
    expect(component.utterances.value).toEqual(['question 2']);
  });

  it('#validateEditUtterance should update the value of utterance', () => {
    component.ngOnChanges({ faq: new SimpleChange(null, mockFaq, true) });
    fixture.detectChanges();

    expect(component.utterances.value).toHaveSize(2);
    expect(component.utterances.value).toEqual(['question 1', 'question 2']);

    component.editedUtteranceValue = 'test';
    component.validateEditUtterance(Array.from(component.utterances.controls.values())[1] as FormControl);
    expect(component.utterances.value).toHaveSize(2);
    expect(component.utterances.value).toEqual(['question 1', 'test']);
  });

  describe('#close', () => {
    it('should call the onClose method without displaying a confirmation request message when the form is not dirty', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('yes') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      component.close();

      expect(component['dialogService'].openDialog).not.toHaveBeenCalled();
      expect(component.onClose.emit).toHaveBeenCalledOnceWith(true);
    });

    it('should call the onClose method after displaying a confirmation request message and confirm when the form is dirty', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('yes') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      // To display the confirmation message, the form must have been modified
      component.form.markAsDirty();
      component.close();

      expect(component['dialogService'].openDialog).toHaveBeenCalled();
      expect(component.onClose.emit).toHaveBeenCalledOnceWith(true);
    });

    it('should not call the onClose method after displaying a confirmation request message and cancel when the form is dirty', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      // To display the confirmation message, the form must have been modified
      component.form.markAsDirty();
      component.close();

      expect(component['dialogService'].openDialog).toHaveBeenCalled();
      expect(component.onClose.emit).not.toHaveBeenCalled();
    });
  });

  describe('#getFormatedIntentName', () => {
    [
      { value: 'test', expected: 'test' },
      { value: 'testFaq', expected: 'testfaq' },
      { value: 'test Faq', expected: 'testfaq' },
      { value: '   test    Faq  ', expected: 'testfaq' },
      { value: 'test-Faq', expected: 'test-faq' },
      { value: 'test_Faq', expected: 'test_faq' },
      { value: 'ABCD', expected: 'abcd' },
      { value: 'test123456789&é"\'(èçà)=~#{[|`\\^@]}°+$£ê*µù%!:;,?./§€', expected: 'test' }
    ].forEach((test) => {
      it(`Should format ${test.value} to ${test.expected}`, () => {
        const res = component.getFormatedIntentName(test.value);

        expect(res).toBe(test.expected);
      });
    });
  });

  describe('#checkIntentNameAndSave', () => {
    it('should call save method without change the intent name when is defined', () => {
      spyOn(component, 'save');
      const faq: FaqDefinitionExtended = JSON.parse(JSON.stringify(mockFaq));
      faq.intentName = 'test';
      component.faq = faq;
      component.ngOnChanges({ faq: new SimpleChange(null, faq, true) });
      fixture.detectChanges();

      component.checkIntentNameAndSave();

      expect(component.save).toHaveBeenCalledOnceWith(faq);
    });

    it('should call save method when creating a new faq if the intent does not exists in the current or another application', () => {
      spyOn(StateService, 'intentExistsInApp').and.returnValue(false);
      spyOn(component['state'], 'intentExistsInOtherApplication').and.returnValue(false);
      spyOn(component, 'save');
      const faq: FaqDefinitionExtended = JSON.parse(JSON.stringify(mockFaq));
      faq.id = undefined;
      component.faq = faq;
      component.ngOnChanges({ faq: new SimpleChange(null, faq, true) });
      fixture.detectChanges();

      component.checkIntentNameAndSave();

      expect(component.save).toHaveBeenCalledOnceWith({ ...faq, intentName: 'titlefaq' } as FaqDefinitionExtended);
    });

    it('should not call save method when creating a new faq if the intent is already exists in the current application', () => {
      spyOn(StateService, 'intentExistsInApp').and.returnValue(true);
      spyOn(component, 'save');
      const faq: FaqDefinitionExtended = JSON.parse(JSON.stringify(mockFaq));
      faq.id = undefined;
      component.faq = faq;
      component.ngOnChanges({ faq: new SimpleChange(null, faq, true) });
      fixture.detectChanges();

      component.checkIntentNameAndSave();

      expect(component.save).not.toHaveBeenCalled();
    });

    it('should not call the save method when creating a new faq if the intent already exists in another application after displaying an info message and canceling', () => {
      spyOn(StateService, 'intentExistsInApp').and.returnValue(false);
      spyOn(component['state'], 'intentExistsInOtherApplication').and.returnValue(true);
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of(undefined) } as NbDialogRef<any>);
      spyOn(component, 'save');
      const faq: FaqDefinitionExtended = JSON.parse(JSON.stringify(mockFaq));
      faq.id = undefined;
      faq.intentName = 'test';
      component.faq = faq;
      component.ngOnChanges({ faq: new SimpleChange(null, faq, true) });
      fixture.detectChanges();

      component.checkIntentNameAndSave();

      expect(component.save).not.toHaveBeenCalled();
    });

    it('should call the save method when creating a new faq if the intent already exists in another application after displaying an info message and share intent', () => {
      spyOn(StateService, 'intentExistsInApp').and.returnValue(false);
      spyOn(component['state'], 'intentExistsInOtherApplication').and.returnValue(true);
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('Share the intent') } as NbDialogRef<any>);
      spyOn(component, 'save');
      const faq: FaqDefinitionExtended = JSON.parse(JSON.stringify(mockFaq));
      faq.id = undefined;
      component.faq = faq;
      component.ngOnChanges({ faq: new SimpleChange(null, faq, true) });
      fixture.detectChanges();

      component.checkIntentNameAndSave();

      expect(component.save).toHaveBeenCalledOnceWith({ ...faq, intentName: 'titlefaq' } as FaqDefinitionExtended);
    });

    it('should call the save method when creating a new faq if the intent already exists in another application after displaying an info message and create new intent', () => {
      spyOn(StateService, 'intentExistsInApp').and.returnValue(false);
      spyOn(component['state'], 'intentExistsInOtherApplication').and.returnValue(true);
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('Create a new intent') } as NbDialogRef<any>);
      spyOn(component, 'save');
      const faq: FaqDefinitionExtended = JSON.parse(JSON.stringify(mockFaq));
      faq.id = undefined;
      component.faq = faq;
      component.ngOnChanges({ faq: new SimpleChange(null, faq, true) });
      fixture.detectChanges();

      component.checkIntentNameAndSave();

      expect(component.save).toHaveBeenCalledOnceWith({ ...faq, intentName: 'titlefaq' } as FaqDefinitionExtended);
    });
  });
});
