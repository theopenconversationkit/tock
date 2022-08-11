import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbSpinnerModule, NbToastrService, NbToggleModule } from '@nebular/theme';
import { of, Subject } from 'rxjs';

import { StateService } from '../../core-nlp/state.service';
import { Classification, PaginatedResult, SentenceStatus } from '../../model/nlp';
import { NlpService } from '../../nlp-tabs/nlp.service';
import { TestSharedModule } from '../../shared/test-shared.module';
import { FaqTrainingComponent, SentenceExtended } from './faq-training.component';

const mockSentences: SentenceExtended[] = [
  {
    text: 'sentence 1',
    status: SentenceStatus.inbox,
    classification: <Classification>{
      intentId: '1',
      intentProbability: 1,
      entitiesProbability: 1
    },
    creationDate: new Date('2022-08-03T09:50:24.952Z'),
    _selected: false,
    getIntentLabel(_state) {
      return 'intent label';
    }
  } as SentenceExtended,
  {
    text: 'sentence 2',
    status: SentenceStatus.inbox,
    classification: <Classification>{
      intentId: '1',
      intentProbability: 1,
      entitiesProbability: 1
    },
    creationDate: new Date('2022-08-03T09:50:24.952Z'),
    _selected: false,
    getIntentLabel(_state) {
      return 'intent label';
    }
  } as SentenceExtended,
  {
    text: 'sentence 3',
    status: SentenceStatus.inbox,
    classification: <Classification>{
      intentId: '1',
      intentProbability: 1,
      entitiesProbability: 1
    },
    creationDate: new Date('2022-08-03T09:50:24.952Z'),
    _selected: false,
    getIntentLabel(_state) {
      return 'intent label';
    }
  } as SentenceExtended,
  {
    text: 'sentence 4',
    status: SentenceStatus.inbox,
    classification: <Classification>{
      intentId: '1',
      intentProbability: 1,
      entitiesProbability: 1
    },
    creationDate: new Date('2022-08-03T09:50:24.952Z'),
    _selected: false,
    getIntentLabel(_state) {
      return 'intent label';
    }
  } as SentenceExtended
];

const mockSentencesPaginatedResult: PaginatedResult<SentenceExtended> = {
  end: mockSentences.length,
  rows: mockSentences,
  start: 0,
  total: mockSentences.length
};

class StateServiceMock {
  createPaginatedQuery() {
    return {
      namespace: 'app',
      application: 'app',
      language: 'fr',
      start: 0,
      size: 10
    };
  }

  configurationChange: Subject<boolean> = new Subject();
}

class NlpServiceMock {
  searchSentences() {
    return of(mockSentencesPaginatedResult);
  }
}

describe('FaqTrainingComponent', () => {
  let component: FaqTrainingComponent;
  let fixture: ComponentFixture<FaqTrainingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqTrainingComponent],
      imports: [TestSharedModule, NbSpinnerModule, NbToggleModule],
      providers: [
        { provide: NlpService, useClass: NlpServiceMock },
        { provide: StateService, useClass: StateServiceMock },
        { provide: NbToastrService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FaqTrainingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render filters and list component when the component is loaded successfully', () => {
    const noDataComponent = fixture.debugElement.query(By.css('tock-no-data-found'));
    const filtersComponent = fixture.debugElement.query(By.css('tock-faq-training-filters'));
    const listComponent = fixture.debugElement.query(By.css('tock-faq-training-list'));
    const dialogComponent = fixture.debugElement.query(By.css('tock-faq-training-dialog'));

    expect(dialogComponent).toBeFalsy();
    expect(noDataComponent).toBeFalsy();
    expect(listComponent).toBeTruthy();
    expect(filtersComponent).toBeTruthy();
  });

  it('should show no data component when the list of faq is empty', () => {
    component.sentences = [];
    fixture.detectChanges();
    const noDataComponent = fixture.debugElement.query(By.css('tock-no-data-found'));
    const filtersComponent = fixture.debugElement.query(By.css('tock-faq-training-filters'));
    const listComponent = fixture.debugElement.query(By.css('tock-faq-training-list'));

    expect(listComponent).toBeFalsy();
    expect(filtersComponent).toBeTruthy();
    expect(noDataComponent).toBeTruthy();
  });

  it('should show the dialog component when the dialog sentence is informed', () => {
    component.dialogDetailsSentence = mockSentences[0];
    fixture.detectChanges();
    const dialogComponent = fixture.debugElement.query(By.css('tock-faq-training-dialog'));

    expect(dialogComponent).toBeTruthy();
  });

  describe('#showDetails', () => {
    it('should populate the dialog sentence when the selected phrase is updated and update selected attribute of all sentences', () => {
      component.showDetails(mockSentences[0]);

      expect(component.dialogDetailsSentence).toEqual(mockSentences[0]);
      component.sentences.forEach((sentence, i) => {
        if (i === 0) expect(sentence._selected).toBeTrue();
        else expect(sentence._selected).toBeFalse();
      });
    });

    it('should set to undefined dialog sentence when selected sentence is the same as defined in dialog sentence', () => {
      component.dialogDetailsSentence = mockSentences[0];
      fixture.detectChanges();

      component.showDetails(mockSentences[0]);

      expect(component.dialogDetailsSentence).toBeUndefined();
      component.sentences.forEach((sentence) => {
        expect(sentence._selected).toBeFalse();
      });
    });

    it('should update the dialog sentence when selected sentence is not the same as defined in dialog sentence', () => {
      [0, 1, 2, 3].forEach((v) => {
        component.showDetails(mockSentences[v]);

        expect(component.dialogDetailsSentence).toEqual(mockSentences[v]);
        component.sentences.forEach((sentence, i) => {
          if (i === v) expect(sentence._selected).toBeTrue();
          else expect(sentence._selected).toBeFalse();
        });
      });
    });
  });

  it('#closeDetails should update the dialog sentence to undefined and update the selected attribute of the sentence', () => {
    component.dialogDetailsSentence = mockSentences[0];

    component.closeDetails();

    expect(component.dialogDetailsSentence).toBeUndefined();
    component.sentences.forEach((sentence) => {
      expect(sentence._selected).toBeFalse();
    });
  });
});
