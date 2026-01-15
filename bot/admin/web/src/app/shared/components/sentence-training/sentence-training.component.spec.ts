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
import { NbSpinnerModule, NbToastrService, NbToggleModule, NbDialogService } from '@nebular/theme';
import { of, Subject, throwError } from 'rxjs';

import { StateService } from '../../../core-nlp/state.service';
import { Classification, Intent, PaginatedResult, SearchQuery, SentenceStatus, TranslateSentencesQuery } from '../../../model/nlp';
import { NlpService } from '../../../core-nlp/nlp.service';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { Action, SentenceTrainingMode } from './models';
import { SentenceTrainingComponent, SentenceExtended } from './sentence-training.component';
import { ChoiceDialogComponent } from '../choice-dialog/choice-dialog.component';
import * as FileSaver from 'file-saver-es';

const mockSentences: SentenceExtended[] = [
  {
    text: 'sentence 1',
    status: SentenceStatus.inbox,
    classification: <Classification>{
      intentId: '1',
      intentProbability: 1,
      entitiesProbability: 1,
      entities: []
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
      entitiesProbability: 1,
      entities: []
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
      entitiesProbability: 1,
      entities: []
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
      entitiesProbability: 1,
      entities: []
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
  currentApplication = { namespace: 'app', name: 'app' };
  currentLocale = 'fr';
  createPaginatedQuery() {
    return {
      namespace: 'app',
      applicationName: 'app',
      language: 'fr',
      start: 0,
      size: 10
    };
  }
  configurationChange: Subject<boolean> = new Subject();
  hasRole(_role: string): boolean {
    return true;
  }
}

class NlpServiceMock {
  searchSentences() {
    const sentencesPayload = JSON.parse(JSON.stringify(mockSentencesPaginatedResult));
    return of(sentencesPayload);
  }
  updateSentence() {
    return of(mockSentences[0]);
  }
  updateSentences() {
    return of({ nbUpdates: 1 });
  }
  translateSentences(_query: TranslateSentencesQuery) {
    return of({ nbTranslations: 1 });
  }
  getSentencesDump() {
    return of(new Blob());
  }
}

class NbToastrServiceMock {
  success = jasmine.createSpy('success');
  show = jasmine.createSpy('show');
}

class NbDialogServiceMock {
  open() {
    return { onClose: of(null) };
  }
}

describe('SentenceTrainingComponent', () => {
  let component: SentenceTrainingComponent;
  let fixture: ComponentFixture<SentenceTrainingComponent>;
  let nlpService: NlpServiceMock;
  let toastrService: NbToastrServiceMock;
  let dialogService: NbDialogServiceMock;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SentenceTrainingComponent, ChoiceDialogComponent],
      imports: [TestSharedModule, NbSpinnerModule, NbToggleModule],
      providers: [
        { provide: NlpService, useClass: NlpServiceMock },
        { provide: StateService, useClass: StateServiceMock },
        { provide: NbToastrService, useClass: NbToastrServiceMock },
        { provide: NbDialogService, useClass: NbDialogServiceMock }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SentenceTrainingComponent);
    component = fixture.componentInstance;
    nlpService = TestBed.inject(NlpService) as unknown as NlpServiceMock;
    toastrService = TestBed.inject(NbToastrService) as unknown as NbToastrServiceMock;
    dialogService = TestBed.inject(NbDialogService) as unknown as NbDialogServiceMock;
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

  it('should show no data component when the list of sentences is empty', () => {
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

  describe('#setSentenceAccordingToAction', () => {
    it('should set sentence status to deleted for Action.DELETE', () => {
      const sentence: SentenceExtended = {
        ...mockSentences[0],
        getIntentLabel: () => 'intent label',
        getText: () => 'sentence 1',
        intentLabel: 'intent label',
        notRetainedEntitiesContainer: null
      } as unknown as SentenceExtended;
      component['setSentenceAccordingToAction'](Action.DELETE, sentence);
      expect(sentence.status).toBe(SentenceStatus.deleted);
    });

    it('should set intent to unknown and clear entities for Action.UNKNOWN', () => {
      const sentence: SentenceExtended = {
        ...mockSentences[0],
        getIntentLabel: () => 'intent label',
        getText: () => 'sentence 1',
        intentLabel: 'intent label',
        notRetainedEntitiesContainer: null
      } as unknown as SentenceExtended;
      component['setSentenceAccordingToAction'](Action.UNKNOWN, sentence);
      expect(sentence.classification.intentId).toBe(Intent.unknown);
      expect(sentence.classification.entities).toEqual([]);
      expect(sentence.status).toBe(SentenceStatus.validated);
    });

    it('should set intent to ragExcluded and clear entities for Action.RAGEXCLUDED', () => {
      const sentence: SentenceExtended = {
        ...mockSentences[0],
        getIntentLabel: () => 'intent label',
        getText: () => 'sentence 1',
        intentLabel: 'intent label',
        notRetainedEntitiesContainer: null
      } as unknown as SentenceExtended;
      component['setSentenceAccordingToAction'](Action.RAGEXCLUDED, sentence);
      expect(sentence.classification.intentId).toBe(Intent.ragExcluded);
      expect(sentence.classification.entities).toEqual([]);
      expect(sentence.status).toBe(SentenceStatus.validated);
    });

    it('should set status to validated for Action.VALIDATE if intentId is set', () => {
      const sentence: SentenceExtended = {
        ...mockSentences[0],
        getIntentLabel: () => 'intent label',
        getText: () => 'sentence 1',
        intentLabel: 'intent label',
        notRetainedEntitiesContainer: null
      } as unknown as SentenceExtended;
      component['setSentenceAccordingToAction'](Action.VALIDATE, sentence);
      expect(sentence.status).toBe(SentenceStatus.validated);
    });

    it('should not change sentence if intentId is null for Action.VALIDATE', () => {
      const sentence: SentenceExtended = {
        ...mockSentences[0],
        classification: { ...mockSentences[0].classification, intentId: null },
        getIntentLabel: () => 'intent label',
        getText: () => 'sentence 1',
        intentLabel: 'intent label',
        notRetainedEntitiesContainer: null
      } as unknown as SentenceExtended;
      component['setSentenceAccordingToAction'](Action.VALIDATE, sentence);
      expect(toastrService.show).toHaveBeenCalledWith('Please select an intent first');
      expect(sentence.status).not.toBe(SentenceStatus.validated);
    });
  });

  describe('#handleBatchAction', () => {
    beforeEach(() => {
      component.selection.select(mockSentences[0], mockSentences[1]);
    });

    it('should update all selected sentences and call nlp.updateSentence for each', fakeAsync(() => {
      spyOn(nlpService, 'updateSentence').and.returnValue(of(mockSentences[0]));
      spyOn(component as any, 'loadSentencesAfterActionPerformed');
      component.handleBatchAction(Action.DELETE);
      tick();
      expect(nlpService.updateSentence).toHaveBeenCalledTimes(2);
      expect((component as any).loadSentencesAfterActionPerformed).toHaveBeenCalledWith(2);
      expect(component.selection.isEmpty()).toBeTrue();
    }));

    it('should show success toast after batch action', fakeAsync(() => {
      spyOn(nlpService, 'updateSentence').and.returnValue(of(mockSentences[0]));
      component.handleBatchAction(Action.UNKNOWN);
      tick();
      expect(toastrService.success).toHaveBeenCalledWith('Unknown 2 sentences', 'Unknown', { duration: 2000, status: 'basic' });
    }));

    it('should remove selected sentences from the list', fakeAsync(() => {
      spyOn(nlpService, 'updateSentence').and.returnValue(of(mockSentences[0]));
      const selectedSentences = [...component.selection.selected];
      component.handleBatchAction(Action.DELETE);
      tick();
      selectedSentences.forEach((s) => expect(component.sentences).not.toContain(s));
    }));
  });

  describe('#loadSentencesAfterActionPerformed', () => {
    it('should load new sentences if pagination.end <= pagination.total', () => {
      component.pagination = { start: 0, end: 2, size: 10, total: 4 };
      spyOn(component, 'loadData');
      component['loadSentencesAfterActionPerformed'](1);
      expect(component.loadData).toHaveBeenCalledWith(1, 1, true, true, true);
    });

    it('should adjust pagination.end and load new sentences if pagination.end > pagination.total and pagination.start > 0', () => {
      component.pagination = { start: 3, end: 4, size: 3, total: 3 };
      spyOn(component, 'loadData');
      component['loadSentencesAfterActionPerformed'](1);
      expect(component.pagination.end).toBe(3);
      expect(component.loadData).toHaveBeenCalledWith(0);
    });

    it('should not load new sentences if pagination.end > pagination.total and pagination.start === pagination.total', () => {
      component.pagination = { start: 3, end: 4, size: 3, total: 3 };
      spyOn(component, 'loadData');
      component['loadSentencesAfterActionPerformed'](1);
      expect(component.loadData).not.toHaveBeenCalled();
    });
  });

  describe('#retrieveSentence', () => {
    it('should show dialog for existing sentence', () => {
      const sentence = component.sentences[0];
      component.retrieveSentence(sentence);
      expect(sentence._showDialog).toBeTrue();
      expect(component.dialogDetailsSentence).toEqual(sentence);
    });

    it('should update filter if sentence not found and tryCount < max', () => {
      const sentence: SentenceExtended = {
        text: 'unknown sentence',
        status: SentenceStatus.inbox,
        classification: { intentId: '1', intentProbability: 1, entitiesProbability: 1, entities: [] }
      } as SentenceExtended;
      spyOn(component.sentenceTrainingFilter, 'updateFilter');
      component.retrieveSentence(sentence);
      expect(component.sentenceTrainingFilter.updateFilter).toHaveBeenCalledWith({ search: sentence.text });
    });
  });

  describe('#toggleSort and #sortSentenceTraining', () => {
    it('should toggle isSorted and reload data', () => {
      spyOn(component, 'loadData');
      component.toggleSort();
      expect(component.isSorted).toBeTrue();
      expect(component.loadData).toHaveBeenCalled();
    });

    it('should update sort order and reload data', () => {
      spyOn(component, 'loadData');
      component.sortSentenceTraining(true);
      expect(component.filters.sort[0].second).toBeTrue();
      expect(component.loadData).toHaveBeenCalled();
    });
  });

  describe('#filterSentenceTraining', () => {
    it('should update filters and reload data', () => {
      const newFilters: Partial<SearchQuery> = { search: 'test', status: [SentenceStatus.validated] };
      spyOn(component, 'loadData');
      component.filterSentenceTraining(newFilters);
      expect(component.filters.search).toBe('test');
      expect(component.filters.status).toEqual([SentenceStatus.validated]);
      expect(component.loadData).toHaveBeenCalled();
    });
  });

  describe('#initFilters', () => {
    it('should set intentId to null and status to [inbox] for SentenceTrainingMode.INBOX', () => {
      component.sentenceTrainingMode = SentenceTrainingMode.INBOX;
      component.initFilters();
      expect(component.filters.intentId).toBeNull();
      expect(component.filters.status).toEqual([SentenceStatus.inbox]);
    });

    it('should set intentId to unknown and status to [validated, model] for SentenceTrainingMode.UNKNOWN', () => {
      component.sentenceTrainingMode = SentenceTrainingMode.UNKNOWN;
      component.initFilters();
      expect(component.filters.intentId).toBe(Intent.unknown);
      expect(component.filters.status).toEqual([SentenceStatus.validated, SentenceStatus.model]);
    });
  });

  describe('#changeSentencesIntent and #changeSentencesEntity', () => {
    it('should open dialog if no sentence is selected', () => {
      spyOn(dialogService, 'open');
      component.changeSentencesIntent('newIntent');
      expect(dialogService.open).toHaveBeenCalled();
    });

    it('should call nlp.updateSentences if sentences are selected', () => {
      component.selection.select(mockSentences[0]);
      spyOn(nlpService, 'updateSentences').and.returnValue(of({ nbUpdates: 1 }));
      component.changeSentencesIntent('newIntent');
      expect(nlpService.updateSentences).toHaveBeenCalled();
      expect(toastrService.show).toHaveBeenCalledWith('1 sentence updated', 'UPDATE', { duration: 2000 });
    });
  });

  describe('#downloadSentencesDump', () => {
    beforeEach(() => {
      spyOn(FileSaver, 'saveAs').and.callFake((blob: Blob, filename: string) => {
        // Mock de saveAs
      });
    });
    it('should call nlp.getSentencesDump and save file', () => {
      spyOn(nlpService, 'getSentencesDump').and.returnValue(of(new Blob(['test'], { type: 'application/json' })));
      component.downloadSentencesDump();
      expect(nlpService.getSentencesDump).toHaveBeenCalled();
      expect(window.saveAs).toHaveBeenCalled();
      expect(toastrService.success).toHaveBeenCalledWith('Dump provided', 'Sentences dump');
    });
  });

  describe('#getCurrentSearchQuery', () => {
    it('should return a SearchQuery with current filters', () => {
      const query = component.getCurrentSearchQuery();
      expect(query.search).toBe(component.filters.search);
    });
  });

  describe('#documentClick', () => {
    it('should delegate to sentenceTrainingService.documentClick', () => {
      const event = new MouseEvent('click');
      spyOn((component as any).sentenceTrainingService, 'documentClick');
      component.documentClick(event);
      expect((component as any).sentenceTrainingService.documentClick).toHaveBeenCalledWith(event);
    });
  });

  describe('#loadData error handling', () => {
    it('should set loading to false if search fails', () => {
      spyOn(nlpService, 'searchSentences').and.returnValue(throwError(() => new Error('test error')));
      component.loadData();
      expect(component.loading).toBeFalse();
    });
  });

  describe('#ngOnDestroy', () => {
    it('should set unloading to true and complete destroy$', () => {
      spyOn((component as any).destroy$, 'next');
      spyOn((component as any).destroy$, 'complete');
      component.ngOnDestroy();
      expect(component.unloading).toBeTrue();
      expect((component as any).destroy$.next).toHaveBeenCalledWith(true);
      expect((component as any).destroy$.complete).toHaveBeenCalled();
    });
  });
});
