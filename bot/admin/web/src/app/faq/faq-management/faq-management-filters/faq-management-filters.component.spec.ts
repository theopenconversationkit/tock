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
    component.form.patchValue({ search: '', tags: [], enabled: null });
    fixture.detectChanges();
    let element = fixture.debugElement.query(By.css('[data-testid="clear-button"]'));

    expect(element).toBeFalsy();
    expect(component.isFiltered).toBeFalse();
  });

  describe('should show clear button when at least one filter is active', () => {
    [
      { description: 'search active', formValue: { search: 'test', tags: [], enabled: null } },
      { description: 'tags active', formValue: { search: '', tags: ['tag1', 'tag2'], enabled: null } },
      { description: 'enabled active', formValue: { search: '', tags: [], enabled: true } },
      { description: 'all field active', formValue: { search: 'test', tags: ['tag1', 'tag2'], enabled: true } }
    ].forEach((parameter) => {
      it(parameter.description, () => {
        component.form.patchValue(parameter.formValue);
        fixture.detectChanges();
        const element = fixture.debugElement.query(By.css('[data-testid="clear-button"]'));

        expect(element).toBeTruthy();
      });
    });
  });

  it('should call the method to clear form when the clear button is clicked', () => {
    const clearFiltersSpy = spyOn(component, 'clearFilters');
    component.form.patchValue({ search: 'test' });
    fixture.detectChanges();
    const element = fixture.debugElement.query(By.css('[data-testid="clear-button"]'));

    element.triggerEventHandler('click', null);

    expect(clearFiltersSpy).toHaveBeenCalledTimes(1);
  });

  describe('should clear form when the method is called', () => {
    [
      { description: 'search active', formValue: { search: 'test', tags: [], enabled: null } },
      { description: 'tags active', formValue: { search: '', tags: ['tag1', 'tag2'], enabled: null } },
      { description: 'enabled active', formValue: { search: '', tags: [], enabled: true } },
      { description: 'all field active', formValue: { search: 'test', tags: ['tag1', 'tag2'], enabled: true } }
    ].forEach((parameter) => {
      it(parameter.description, () => {
        component.form.patchValue(parameter.formValue);

        component.clearFilters();

        expect(component.search.value).toBeNull();
        expect(component.tags.value).toEqual([]);
        expect(component.enabled.value).toBeNull();
      });
    });
  });

  it('should update enabled field when the method is called', () => {
    // null to true
    component.enabled.setValue(null);
    component.enabledCheckChanged();
    expect(component.enabled.value).toBeTrue();

    // true to false
    component.enabledCheckChanged();
    expect(component.enabled.value).toBeFalse();

    // false to null
    component.enabledCheckChanged();
    expect(component.enabled.value).toBeNull();
  });
});
