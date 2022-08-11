import { ComponentFixture, discardPeriodicTasks, fakeAsync, flush, TestBed, tick } from '@angular/core/testing';
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

import { DialogService } from '../../../core-nlp/dialog.service';
import { NlpService } from '../../../nlp-tabs/nlp.service';
import { StateService } from '../../../core-nlp/state.service';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { FaqManagementEditComponent, FaqTabs } from './faq-management-edit.component';
import { FormControlComponent } from '../../../shared/form-control/form-control.component';
import { FaqDefinitionExtended } from '../faq-management.component';
import { Classification, Intent, PaginatedResult, Sentence, SentenceStatus } from 'src/app/model/nlp';
import { of } from 'rxjs';

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
  applicationId: '1',
  enabled: true,
  language: 'fr',
  title: 'title',
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

  findIntentById() {
    return {
      name: 'intentAssociate'
    } as Intent;
  }
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

  beforeEach(() => {
    fixture = TestBed.createComponent(FaqManagementEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

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
        applicationId: '1',
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
        applicationId: '1',
        language: 'fr',
        _initUtterance: 'test'
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
      expect(component.form.valid).toBeFalse();
      expect(component.form.value).toEqual({
        title: 'title',
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

    component.utteranceEditionValue = 'test';
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
});
