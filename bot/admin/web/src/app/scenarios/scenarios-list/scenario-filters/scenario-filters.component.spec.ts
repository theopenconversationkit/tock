import { NO_ERRORS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Scenario } from '../../models';

import { ScenarioFiltersComponent } from './scenario-filters.component';

const mockScenarios = [
  {
    id: 1,
    tags: ['tag', 'tag', 'tag 1', 'tag 2']
  },
  {
    id: 2,
    tags: ['test', 'tag', 'tag 1']
  }
] as Scenario[];

describe('ScenarioFiltersComponent', () => {
  let component: ScenarioFiltersComponent;
  let fixture: ComponentFixture<ScenarioFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioFiltersComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not show clear button when no filters are active', () => {
    let element = fixture.debugElement.query(By.css('.actions button'));

    expect(element).toBeNull();
  });

  it('should show clear button when at least one filter is active', () => {
    component.form.patchValue({ search: 'test', tags: [] });
    fixture.detectChanges();
    let element = fixture.debugElement.query(By.css('.actions button'));

    expect(element).toBeTruthy();

    component.form.patchValue({ search: '', tags: ['tag1', 'tag2'] });
    fixture.detectChanges();
    element = fixture.debugElement.query(By.css('.actions button'));

    expect(element).toBeTruthy();

    component.form.patchValue({ search: 'test', tags: ['tag1', 'tag2'] });
    fixture.detectChanges();
    element = fixture.debugElement.query(By.css('.actions button'));

    expect(element).toBeTruthy();
  });

  it('should clear form when the clear button is clicked', () => {
    const clearFiltersSpy = spyOn(component, 'clearFilters');
    component.form.patchValue({ search: 'test' });
    fixture.detectChanges();
    const element = fixture.debugElement.query(By.css('.actions button'));

    element.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(clearFiltersSpy).toHaveBeenCalledTimes(1);
  });

  it('should populate tag list with no duplicates from a scenario array if scenarios is defined', () => {
    component.scenarios = undefined;
    fixture.detectChanges();

    expect(component.tagsAvailableValues.length).toBe(0);

    component.ngOnChanges({ scenarios: new SimpleChange(null, mockScenarios, true) });
    fixture.detectChanges();

    expect(component.tagsAvailableValues.length).toBe(4);
    expect(component.tagsAvailableValues).toEqual(['tag', 'tag 1', 'tag 2', 'test']);
  });

  it('should emit the filters after 500ms after one of them is changed', fakeAsync(() => {
    const onFilterSpy = spyOn(component.onFilter, 'emit');

    expect(onFilterSpy).not.toHaveBeenCalled();

    component.form.patchValue({ search: 'test' });
    fixture.detectChanges();

    tick(400);
    expect(onFilterSpy).not.toHaveBeenCalled();

    tick(500);
    expect(onFilterSpy).toHaveBeenCalledOnceWith({ search: 'test', tags: [] });
  }));
});
