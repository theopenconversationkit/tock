import { NO_ERRORS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import {
  NbButtonModule,
  NbCardModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule
} from '@nebular/theme';
import { of } from 'rxjs';

import { TestSharedModule } from '../../../shared/test-shared.module';
import { Scenario } from '../../models';
import { ScenarioService } from '../../services/scenario.service';
import { ScenarioFiltersComponent } from './scenario-filters.component';

describe('ScenarioFiltersComponent', () => {
  let component: ScenarioFiltersComponent;
  let fixture: ComponentFixture<ScenarioFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        TestSharedModule,
        NbButtonModule,
        NbCardModule,
        NbFormFieldModule,
        NbIconModule,
        NbInputModule,
        NbOptionModule,
        NbSelectModule
      ],
      declarations: [ScenarioFiltersComponent],
      providers: [
        {
          provide: ScenarioService,
          useValue: {
            state$: of({
              tags: ['tag1', 'tag2']
            })
          }
        }
      ],
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

  it('should not show clear button when no filters are active', fakeAsync(() => {
    fixture.whenStable().then(() => {
      let element = fixture.debugElement.query(By.css('.actions button'));
      expect(element).toBeNull();
    });
  }));

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

  it('should clear form when the clear button is clicked', fakeAsync(() => {
    fixture.whenStable().then(() => {
      tick(100);
      const clearFiltersSpy = spyOn(component, 'clearFilters');
      component.form.patchValue({ search: 'test' });
      fixture.detectChanges();
      const element = fixture.debugElement.query(By.css('.actions button'));

      element.triggerEventHandler('click', null);
      fixture.detectChanges();

      expect(clearFiltersSpy).toHaveBeenCalledTimes(1);
    });
  }));

  it('should emit the filters after 500ms after one of them is changed', fakeAsync(() => {
    fixture.whenStable().then(() => {
      const onFilterSpy = spyOn(component.onFilter, 'emit');

      expect(onFilterSpy).not.toHaveBeenCalled();

      component.form.patchValue({ search: 'test' });
      fixture.detectChanges();

      tick(400);
      expect(onFilterSpy).not.toHaveBeenCalled();

      tick(500);
      expect(onFilterSpy).toHaveBeenCalledOnceWith({ search: 'test', tags: [] });
    });
  }));
});
