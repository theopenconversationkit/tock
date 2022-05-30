import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbSelectModule,
  NbTooltipModule
} from '@nebular/theme';

import { TestSharedModule } from '../../../shared/test-shared.module';
import { FaqFilter } from '../../models';
import { FaqManagementFiltersComponent } from './faq-management-filters.component';

describe('FaqManagementFiltersComponent', () => {
  let component: FaqManagementFiltersComponent;
  let fixture: ComponentFixture<FaqManagementFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqManagementFiltersComponent],
      imports: [
        TestSharedModule,
        NbButtonModule,
        NbCardModule,
        NbCheckboxModule,
        NbFormFieldModule,
        NbIconModule,
        NbInputModule,
        NbSelectModule,
        NbTooltipModule
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FaqManagementFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit the filters after 500ms after one of them is changed', fakeAsync(() => {
    const onFilterSpy = spyOn(component.onFilter, 'emit');

    expect(onFilterSpy).not.toHaveBeenCalled();

    component.form.patchValue({ search: 'test' });
    fixture.detectChanges();

    tick(400);
    expect(onFilterSpy).not.toHaveBeenCalled();

    tick(500);
    expect(onFilterSpy).toHaveBeenCalledWith({
      search: 'test',
      tags: [],
      enabled: null
    } as FaqFilter);
  }));

  it('should not show clear button when no filters are active', () => {
    let element = fixture.debugElement.query(By.css('.actions button'));

    expect(element).toBeNull();
    expect(component.isFiltered).toBeFalse();
  });

  it('should show clear button when at least one filter is active', () => {
    component.form.patchValue({ search: '', tags: [], enabled: null });
    fixture.detectChanges();
    let element = fixture.debugElement.query(By.css('.actions button'));

    expect(element).toBeFalsy();

    component.form.patchValue({ search: 'test', tags: [], enabled: null });
    fixture.detectChanges();
    element = fixture.debugElement.query(By.css('.actions button'));

    expect(element).toBeTruthy();

    component.form.patchValue({ search: '', tags: ['tag1', 'tag2'], enabled: null });
    fixture.detectChanges();
    element = fixture.debugElement.query(By.css('.actions button'));

    expect(element).toBeTruthy();

    component.form.patchValue({ search: 'test', tags: ['tag1', 'tag2'], enabled: null });
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
});
