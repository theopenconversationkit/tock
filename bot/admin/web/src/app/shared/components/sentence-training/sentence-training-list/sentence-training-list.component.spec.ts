import { By } from '@angular/platform-browser';
import { SelectionModel } from '@angular/cdk/collections';
import { Location } from '@angular/common';
import { Component } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Routes } from '@angular/router';
import {
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbSelectModule,
  NbTooltipModule
} from '@nebular/theme';
import { BehaviorSubject } from 'rxjs';

import { Classification, SentenceStatus } from '../../../../model/nlp';
import { StateService } from '../../../../core-nlp/state.service';
import { Pagination, PaginationComponent } from '../../../../shared/components';
import { TestSharedModule } from '../../../../shared/test-shared.module';
import { SentenceTrainingComponent, SentenceExtended } from '../sentence-training.component';
import { SentenceTrainingListComponent } from './sentence-training-list.component';
import { Action } from './../models';

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
    _selected: true,
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

class StateServiceMock {
  currentIntentsCategories: BehaviorSubject<any[]> = new BehaviorSubject([]);
  hasRole = (_role): boolean => {
    return true;
  };
}

@Component({})
class MockFaqManagementComponent {}

describe('SentenceTrainingListComponent', () => {
  let component: SentenceTrainingListComponent;
  let fixture: ComponentFixture<SentenceTrainingListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SentenceTrainingListComponent, PaginationComponent],
      imports: [
        TestSharedModule,
        RouterTestingModule.withRoutes([
          { path: 'faq/training', component: SentenceTrainingComponent },
          { path: 'faq/management', component: MockFaqManagementComponent }
        ] as Routes),
        NbAutocompleteModule,
        NbCheckboxModule,
        NbIconModule,
        NbFormFieldModule,
        NbSelectModule,
        NbCardModule,
        NbButtonModule,
        NbTooltipModule
      ],
      providers: [
        {
          provide: StateService,
          useClass: StateServiceMock
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SentenceTrainingListComponent);
    component = fixture.componentInstance;
    component.selection = new SelectionModel();
    component.pagination = {
      size: 10,
      start: 0,
      end: mockSentences.length,
      total: mockSentences.length
    };
    component.sentences = mockSentences;
    component.isFilteredUnknown = false;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create as many entries as the list contains', () => {
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));

    expect(listElement).toHaveSize(mockSentences.length);

    listElement.forEach((child, i) => {
      const textElement: HTMLSpanElement = child.nativeElement.querySelector('[data-testid="text"]');
      expect(textElement.textContent.trim()).toBe(mockSentences[i].text);
    });
  });

  /**
   * A sentence is selected when his detail panel is open
   */
  it('should add css indicator when a sentence is selected', () => {
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));

    listElement.forEach((child, i) => {
      const element = child.nativeElement;

      if (i === 0) expect(element).toHaveClass('selected');
      else expect(element).not.toHaveClass('selected');
    });
  });

  it('should emit the sentence when clicking on the validate button of an item', () => {
    spyOn(component.onAction, 'emit');
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
    const buttonElement: HTMLButtonElement = listElement[0].query(By.css('[data-testid="validate"]')).nativeElement;

    buttonElement.click();

    expect(component.onAction.emit).toHaveBeenCalledOnceWith({ action: Action.VALIDATE, sentence: mockSentences[0] });
  });

  it('should emit the sentence when clicking on the unknown button of an item', () => {
    spyOn(component.onAction, 'emit');
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
    const buttonElement: HTMLButtonElement = listElement[0].query(By.css('[data-testid="unknown"]')).nativeElement;

    buttonElement.click();

    expect(component.onAction.emit).toHaveBeenCalledOnceWith({ action: Action.UNKNOWN, sentence: mockSentences[0] });
  });

  it('should emit the sentence when clicking on the delete button of an item', () => {
    spyOn(component.onAction, 'emit');
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
    const buttonElement: HTMLButtonElement = listElement[0].query(By.css('[data-testid="delete"]')).nativeElement;

    buttonElement.click();

    expect(component.onAction.emit).toHaveBeenCalledOnceWith({ action: Action.DELETE, sentence: mockSentences[0] });
  });

  it('should emit the sentence when clicking on the show details button of an item', () => {
    spyOn(component.onDetails, 'emit');
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
    const buttonElement: HTMLButtonElement = listElement[0].query(By.css('[data-testid="details"]')).nativeElement;

    buttonElement.click();

    expect(component.onDetails.emit).toHaveBeenCalledOnceWith(mockSentences[0]);
  });

  it('should redirect to the faq management page with a question sentence when clicking on the button to create new faq of an item', fakeAsync(() => {
    const location = TestBed.inject(Location);
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
    const buttonElement: HTMLButtonElement = listElement[0].query(By.css('[data-testid="redirectToFaqManagement"]')).nativeElement;

    buttonElement.click();
    tick();

    const state = location.getState();
    expect(location.path()).toBe('/faq/management');
    expect(state['question']).toBe(mockSentences[0].text);
  }));

  it('should emit the sort status when clicking on the container to sort the list of sentence', () => {
    spyOn(component.onSort, 'emit');
    const buttonElement: HTMLSpanElement = fixture.debugElement.query(By.css('[data-testid="sort"]')).nativeElement;

    buttonElement.click();

    expect(component.onSort.emit).toHaveBeenCalledOnceWith(component.isSorted);
  });

  describe('selection sentences', () => {
    it('should clear the selection when the pagination change', () => {
      component.selection.select(mockSentences[0]);

      expect(component.selection.isEmpty()).toBeFalse();

      component.paginationChange({ end: 20, size: 10, start: 10, total: 20 } as Pagination);

      expect(component.selection.isEmpty()).toBeTrue();
    });

    it('should show container of batch action when the selection is not empty', () => {
      component.selection.clear();
      fixture.detectChanges();
      let batchActionElement = fixture.debugElement.query(By.css('[data-testid="batch-action"]'));

      expect(batchActionElement).toBeFalsy();

      component.selection.select(mockSentences[0]);
      fixture.detectChanges();
      batchActionElement = fixture.debugElement.query(By.css('[data-testid="batch-action"]'));

      expect(batchActionElement).toBeTruthy();
    });

    it('should emit validate action when clicking on the button to validate all sentences selected', () => {
      spyOn(component.onBatchAction, 'emit');
      component.selection.select(mockSentences[0]);
      fixture.detectChanges();
      const buttonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="batch-action-validate"]')).nativeElement;

      buttonElement.click();

      expect(component.onBatchAction.emit).toHaveBeenCalledOnceWith(Action.VALIDATE);
    });

    it('should emit unknown action when clicking on the button to unknown all sentences selected', () => {
      spyOn(component.onBatchAction, 'emit');
      component.selection.select(mockSentences[0]);
      fixture.detectChanges();
      const buttonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="batch-action-unknown"]')).nativeElement;

      buttonElement.click();

      expect(component.onBatchAction.emit).toHaveBeenCalledOnceWith(Action.UNKNOWN);
    });

    it('should emit delete action when clicking on the button to delete all sentences selected', () => {
      spyOn(component.onBatchAction, 'emit');
      component.selection.select(mockSentences[0]);
      fixture.detectChanges();
      const buttonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="batch-action-delete"]')).nativeElement;

      buttonElement.click();

      expect(component.onBatchAction.emit).toHaveBeenCalledOnceWith(Action.DELETE);
    });
  });

  describe('when filtered by unknown', () => {
    it('should not show the button for each item of the list', () => {
      component.isFilteredUnknown = true;
      fixture.detectChanges();

      const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));

      listElement.forEach((element) => {
        const buttonElement = element.query(By.css('[data-testid="unknown"]'));

        expect(buttonElement).toBeFalsy();
      });
    });

    it('should not show the batch button button', () => {
      component.isFilteredUnknown = true;
      fixture.detectChanges();

      const buttonElement = fixture.debugElement.query(By.css('[data-testid="batch-action-unknown"]'));

      expect(buttonElement).toBeFalsy();
    });
  });
});
