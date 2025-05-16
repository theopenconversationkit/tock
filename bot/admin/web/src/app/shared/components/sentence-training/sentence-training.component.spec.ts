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

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbSpinnerModule, NbToastrService, NbToggleModule } from '@nebular/theme';
import { of, Subject } from 'rxjs';

import { StateService } from '../../../core-nlp/state.service';
import { Classification, Intent, PaginatedResult, Sentence, SentenceStatus } from '../../../model/nlp';
import { NlpService } from '../../../core-nlp/nlp.service';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { Action } from './models';
import { SentenceTrainingComponent, SentenceExtended } from './sentence-training.component';

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
    _showDialog: false,
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
    _showDialog: false,
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
    _showDialog: false,
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
    _showDialog: false,
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
    const sentencesPayload = JSON.parse(JSON.stringify(mockSentencesPaginatedResult));
    return of(sentencesPayload);
  }

  updateSentence() {
    return of(mockSentences[0]);
  }
}

describe('SentenceTrainingComponent', () => {
  let component: SentenceTrainingComponent;
  let fixture: ComponentFixture<SentenceTrainingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SentenceTrainingComponent],
      imports: [TestSharedModule, NbSpinnerModule, NbToggleModule],
      providers: [
        { provide: NlpService, useClass: NlpServiceMock },
        { provide: StateService, useClass: StateServiceMock },
        { provide: NbToastrService, useValue: { success: () => {} } }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SentenceTrainingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render filters and list component when the component is loaded successfully', () => {
    const noDataComponent = fixture.debugElement.query(By.css('tock-no-data-found'));
    const filtersComponent = fixture.debugElement.query(By.css('tock-sentence-training-filters'));
    const listComponent = fixture.debugElement.query(By.css('tock-sentence-training-list'));
    const dialogComponent = fixture.debugElement.query(By.css('tock-sentence-training-dialog'));

    expect(dialogComponent).toBeFalsy();
    expect(noDataComponent).toBeFalsy();
    expect(listComponent).toBeTruthy();
    expect(filtersComponent).toBeTruthy();
  });

  it('should show no data component when the list of faq is empty', () => {
    component.sentences = [];
    fixture.detectChanges();
    const noDataComponent = fixture.debugElement.query(By.css('tock-no-data-found'));
    const filtersComponent = fixture.debugElement.query(By.css('tock-sentence-training-filters'));
    const listComponent = fixture.debugElement.query(By.css('tock-sentence-training-list'));

    expect(listComponent).toBeFalsy();
    expect(filtersComponent).toBeTruthy();
    expect(noDataComponent).toBeTruthy();
  });

  it('should show the dialog component when the dialog sentence is informed', () => {
    component.dialogDetailsSentence = component.sentences[0];
    fixture.detectChanges();
    const dialogComponent = fixture.debugElement.query(By.css('tock-sentence-training-dialog'));

    expect(dialogComponent).toBeTruthy();
  });

  describe('#showDetails', () => {
    it('should populate the dialog sentence when the selected phrase is updated and update selected attribute of all sentences', () => {
      component.showDetails(component.sentences[0]);

      expect(component.dialogDetailsSentence).toEqual(component.sentences[0]);
      component.sentences.forEach((sentence, i) => {
        if (i === 0) expect(sentence._showDialog).toBeTrue();
        else expect(sentence._showDialog).toBeFalse();
      });
    });

    it('should set to undefined dialog sentence when selected sentence is the same as defined in dialog sentence', () => {
      component.dialogDetailsSentence = component.sentences[0];
      fixture.detectChanges();

      component.showDetails(component.sentences[0]);

      expect(component.dialogDetailsSentence).toBeUndefined();
      component.sentences.forEach((sentence) => {
        expect(sentence._showDialog).toBeFalse();
      });
    });

    it('should update the dialog sentence when selected sentence is not the same as defined in dialog sentence', () => {
      [0, 1, 2, 3].forEach((v) => {
        component.showDetails(component.sentences[v]);

        expect(component.dialogDetailsSentence).toEqual(component.sentences[v]);
        component.sentences.forEach((sentence, i) => {
          if (i === v) expect(sentence._showDialog).toBeTrue();
          else expect(sentence._showDialog).toBeFalse();
        });
      });
    });
  });

  it('#closeDetails should update the dialog sentence to undefined and update the selected attribute of the sentence', () => {
    component.dialogDetailsSentence = component.sentences[0];

    component.closeDetails();

    expect(component.dialogDetailsSentence).toBeUndefined();
    component.sentences.forEach((sentence) => {
      expect(sentence._showDialog).toBeFalse();
    });
  });

  describe('#handleAction', () => {
    it('should update the sentence according to the action', () => {
      const sentence: Sentence = JSON.parse(JSON.stringify(component.sentences[0]));
      const expectedSentence: Sentence = JSON.parse(JSON.stringify(component.sentences[0]));

      // Delete action
      expectedSentence.status = SentenceStatus.deleted;
      component.handleAction({ action: Action.DELETE, sentence });

      expect(sentence).toEqual(expectedSentence);

      // Unknown action
      expectedSentence.classification.intentId = Intent.unknown;
      expectedSentence.classification.entities = [];
      expectedSentence.status = SentenceStatus.validated;
      component.handleAction({ action: Action.UNKNOWN, sentence });

      expect(sentence).toEqual(expectedSentence);

      // Validate action
      expectedSentence.status = SentenceStatus.validated;
      component.handleAction({ action: Action.VALIDATE, sentence });

      expect(sentence).toEqual(expectedSentence);
    });

    it('should call the method to update sentence', () => {
      const sentence: Sentence = JSON.parse(JSON.stringify(component.sentences[0]));
      spyOn(component['nlp'], 'updateSentence').and.returnValue(of(sentence));

      component.handleAction({ action: Action.VALIDATE, sentence });

      expect(component['nlp'].updateSentence).toHaveBeenCalledOnceWith(sentence);
    });

    it('should unselect the sentence when the action is performed and is the sentence is selected', fakeAsync(() => {
      const sentence: Sentence = JSON.parse(JSON.stringify(component.sentences[0]));
      component.selection.select(sentence);

      expect(component.selection.isSelected(sentence)).toBeTrue();

      component.handleAction({ action: Action.VALIDATE, sentence });
      tick();

      expect(component.selection.isSelected(sentence)).toBeFalse();
    }));

    it('should remove the sentence in the list when the action is performed', fakeAsync(() => {
      const sentence = component.sentences[0];
      expect(component.sentences).toContain(sentence);

      component.handleAction({ action: Action.VALIDATE, sentence: sentence });
      tick();

      expect(component.sentences).not.toContain(sentence);
    }));

    it('should reduce the pagination total when the action is performed', fakeAsync(() => {
      expect(component.pagination.total).toBe(4);

      component.handleAction({ action: Action.VALIDATE, sentence: component.sentences[0] });
      tick();

      expect(component.pagination.total).toBe(3);
    }));

    /**
     * This scenario occurs when there are several pages and the action performed is performed on a page that does not correspond to the last one
     */
    it('should load a sentence when the pagination end is inferior to the pagination total', fakeAsync(() => {
      component.pagination.end = 2;
      component.pagination.size = 2;
      spyOn(component, 'loadData');

      component.handleAction({ action: Action.VALIDATE, sentence: component.sentences[0] });
      tick();

      expect(component.loadData).toHaveBeenCalledOnceWith(1, 1, true, true, true);
    }));

    /**
     * This scenario occurs when you are on the last page and this page contains several sentences after updating the pagination variables
     */
    it('should not load a sentence when the pagination end is superior to the pagination total and the pagination total is not equal to the pagination start', fakeAsync(() => {
      component.pagination.start = 2;
      component.pagination.end = 4;
      component.pagination.size = 2;
      spyOn(component, 'loadData');

      component.handleAction({ action: Action.VALIDATE, sentence: component.sentences[3] });
      tick();

      expect(component.pagination.end).toBe(3);
      expect(component.loadData).not.toHaveBeenCalled();
    }));

    /**
     * This scenario occurs when you are on the last page and this page no longer has a sentence after updating the pagination variables
     */
    it('should load a sentence when the pagination end is superior to the pagination total and the pagination total is equal to the pagination start and upper than 0', fakeAsync(() => {
      component.pagination.start = 3;
      component.pagination.end = 4;
      component.pagination.size = 3;
      spyOn(component, 'loadData');

      component.handleAction({ action: Action.VALIDATE, sentence: component.sentences[3] });
      tick();

      expect(component.pagination.end).toBe(3);
      expect(component.loadData).toHaveBeenCalledOnceWith(0);
    }));
  });

  describe('#handleBatchAction', () => {
    beforeEach(() => {
      [0, 1].forEach((v) => {
        component.selection.select(component.sentences[v]);
      });
    });

    it('should update all sentences according to the action', () => {
      // Delete action
      component.handleBatchAction(Action.DELETE);

      component.selection.selected.forEach((s) => {
        const expectedSentence: Sentence = JSON.parse(JSON.stringify(s));
        expectedSentence.status = SentenceStatus.deleted;

        expect(s).toEqual(expectedSentence);
      });

      // Unknown action
      component.handleBatchAction(Action.UNKNOWN);

      component.selection.selected.forEach((s) => {
        const expectedSentence: Sentence = JSON.parse(JSON.stringify(s));
        expectedSentence.classification.intentId = Intent.unknown;
        expectedSentence.classification.entities = [];
        expectedSentence.status = SentenceStatus.validated;

        expect(s).toEqual(expectedSentence);
      });

      // Validate action
      component.handleBatchAction(Action.VALIDATE);

      component.selection.selected.forEach((s) => {
        const expectedSentence: Sentence = JSON.parse(JSON.stringify(s));
        expectedSentence.status = SentenceStatus.validated;

        expect(s).toEqual(expectedSentence);
      });
    });

    it('should call the method to update sentence for each selected sentences', () => {
      spyOn(component['nlp'], 'updateSentence').and.returnValue(of(mockSentences[0]));

      component.handleBatchAction(Action.VALIDATE);

      component.selection.selected.forEach((s) => {
        expect(component['nlp'].updateSentence).toHaveBeenCalledWith(s);
      });
    });

    it('should unselect the sentence when the action is performed and is the sentence is selected', fakeAsync(() => {
      component.handleBatchAction(Action.VALIDATE);
      tick();

      expect(component.selection.isEmpty()).toBeTrue();
    }));

    it('should remove all sentences in the list when the action is performed', fakeAsync(() => {
      component.selection.selected.forEach((s) => {
        expect(component.sentences).toContain(s);
      });

      component.handleBatchAction(Action.VALIDATE);
      tick();

      component.selection.selected.forEach((s) => {
        expect(component.sentences).not.toContain(s);
      });
    }));

    it('should reduce the pagination total when the action is performed', fakeAsync(() => {
      expect(component.pagination.total).toBe(4);

      component.handleBatchAction(Action.VALIDATE);
      tick();

      expect(component.pagination.total).toBe(2);
    }));

    /**
     * This scenario occurs when there are several pages and the action performed is performed on a page that does not correspond to the last one
     */
    it('should load sentences when the pagination end is inferior to the pagination total', fakeAsync(() => {
      component.pagination.end = 2;
      component.pagination.size = 2;
      spyOn(component, 'loadData');

      component.handleBatchAction(Action.VALIDATE);
      tick();

      expect(component.loadData).toHaveBeenCalledOnceWith(0, 2, true, true, true);
    }));

    /**
     * This scenario occurs when you are on the last page and this page contains several sentences after updating the pagination variables
     */
    it('should not load a sentence when the pagination end is superior to the pagination total and the pagination total is not equal to the pagination start', fakeAsync(() => {
      component.selection.clear();
      component.selection.select(component.sentences[3]);
      component.pagination.start = 2;
      component.pagination.end = 4;
      component.pagination.size = 2;
      spyOn(component, 'loadData');

      component.handleBatchAction(Action.VALIDATE);
      tick();

      expect(component.pagination.end).toBe(3);
      expect(component.loadData).not.toHaveBeenCalled();
    }));

    /**
     * This scenario occurs when you are on the last page and this page no longer has a sentence after updating the pagination variables
     */
    it('should load a sentence when the pagination end is superior to the pagination total and the pagination total is equal to the pagination start and upper than 0', fakeAsync(() => {
      component.selection.clear();
      component.selection.select(component.sentences[3]);
      component.pagination.start = 3;
      component.pagination.end = 4;
      component.pagination.size = 3;
      spyOn(component, 'loadData');

      component.handleBatchAction(Action.VALIDATE);
      tick();

      expect(component.pagination.end).toBe(3);
      expect(component.loadData).toHaveBeenCalledOnceWith(0);
    }));
  });
});
