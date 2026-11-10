import { Component, Input } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbSelectComponent } from '@nebular/theme';

import { TestSharedModule } from '../../../shared/test-shared.module';
import { VectorStoreIndex } from '../../models/vector-store-inspection.models';
import { IndexSelectorComponent } from './index-selector.component';

@Component({ selector: 'tock-form-control', template: '<ng-content></ng-content>', standalone: false })
class FormControlStubComponent {
  @Input() label = '';
  @Input() name = '';
}

describe('IndexSelectorComponent selection after refresh', () => {
  let fixture: ComponentFixture<IndexSelectorComponent>;
  let component: IndexSelectorComponent;
  const index: VectorStoreIndex = {
    indexName: 'selected-index',
    indexSessionId: 'session',
    indexDatetime: '2026-11-06T14:10:00Z',
    documentCount: 1,
    chunkCount: 2,
    isCurrent: false
  };
  const other = { ...index, indexName: 'other-index' };

  function render(): void {
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
  }

  function selectedLabel(): string {
    return fixture.nativeElement.querySelector('.select-button').textContent;
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule],
      declarations: [IndexSelectorComponent, FormControlStubComponent]
    }).compileComponents();
    fixture = TestBed.createComponent(IndexSelectorComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('indexes', [index, other]);
    fixture.componentRef.setInput('selected', index);
    fixture.componentRef.setInput('placeholder', 'Select an index');
  });

  [1, 0].forEach((remainingChunks) => {
    it(`keeps the selected index visible after deletion leaves ${remainingChunks} chunks`, fakeAsync(() => {
      render();
      expect(selectedLabel()).toContain(index.indexName);
      const selectionChanged = spyOn(component.indexChange, 'emit');

      const refreshed = { ...index, chunkCount: remainingChunks, documentCount: remainingChunks ? 1 : 0 };
      fixture.componentRef.setInput('indexes', [refreshed, { ...other }]);
      fixture.componentRef.setInput('selected', refreshed);
      render();

      expect(selectedLabel()).toContain(index.indexName);
      expect(selectedLabel()).not.toContain(component.placeholder);
      expect(selectionChanged).not.toHaveBeenCalled();
    }));
  });

  it('shows the fallback index when the previously selected index was deleted', fakeAsync(() => {
    render();
    const refreshed = { ...other };
    fixture.componentRef.setInput('indexes', [refreshed]);
    fixture.componentRef.setInput('selected', refreshed);
    render();
    expect(selectedLabel()).toContain(other.indexName);
    expect(selectedLabel()).not.toContain(index.indexName);
  }));

  it('shows the placeholder when no index remains', fakeAsync(() => {
    render();
    fixture.componentRef.setInput('indexes', []);
    fixture.componentRef.setInput('selected', null);
    render();
    expect(selectedLabel()).toContain(component.placeholder);
  }));

  it('emits the latest index data when the user selects an option after refresh', fakeAsync(() => {
    render();
    const refreshed = { ...other, chunkCount: 5 };
    fixture.componentRef.setInput('indexes', [{ ...index }, refreshed]);
    render();
    const selectionChanged = spyOn(component.indexChange, 'emit');
    const select = fixture.debugElement.query(By.directive(NbSelectComponent)).componentInstance as NbSelectComponent;
    select.selectedChange.emit(refreshed.indexName);
    expect(selectionChanged).toHaveBeenCalledOnceWith(refreshed);
  }));
});
