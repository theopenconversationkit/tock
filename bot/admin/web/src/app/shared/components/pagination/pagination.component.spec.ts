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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbButtonModule, NbIconModule, NbSelectModule, NbTooltipModule } from '@nebular/theme';

import { TestSharedModule } from '../../test-shared.module';
import { Pagination, PaginationComponent } from './pagination.component';

describe('PaginationComponent', () => {
  let component: PaginationComponent;
  let fixture: ComponentFixture<PaginationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PaginationComponent],
      imports: [TestSharedModule, NbButtonModule, NbIconModule, NbSelectModule, NbTooltipModule]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PaginationComponent);
    component = fixture.componentInstance;
    component.pagination = {
      end: 0,
      size: 0,
      start: 0,
      total: 0
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should disable the back button if the beginning of the page is strictly less than 1', () => {
    spyOn(component.onPaginationChange, 'emit');
    const previousButtonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="back-button"]')).nativeElement;

    previousButtonElement.click();

    expect(previousButtonElement.hasAttribute('disabled')).toBeTruthy();
    expect(component.onPaginationChange.emit).not.toHaveBeenCalled();
  });

  it('should enable the back button if the beginning of the page is strictly upper than 0', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination.start = 1;
    component.pagination.size = 10;
    fixture.detectChanges();
    const previousButtonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="back-button"]')).nativeElement;

    previousButtonElement.click();

    expect(previousButtonElement.hasAttribute('disabled')).toBeFalsy();
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should disable the next button if the end of the page is greater than or equal to the total of the result', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination.end = 22;
    component.pagination.total = 22;
    fixture.detectChanges();

    const nextButtonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="next-button"]')).nativeElement;

    nextButtonElement.click();

    expect(nextButtonElement.hasAttribute('disabled')).toBeTruthy();
    expect(component.onPaginationChange.emit).not.toHaveBeenCalled();

    component.pagination.end = 28;
    component.pagination.total = 22;
    fixture.detectChanges();
    nextButtonElement.click();

    expect(nextButtonElement.hasAttribute('disabled')).toBeTruthy();
    expect(component.onPaginationChange.emit).not.toHaveBeenCalled();
  });

  it('should enable the next button if the ending of the page is strictly less than the total of result', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination.end = 13;
    component.pagination.total = 22;
    fixture.detectChanges();
    const nextButtonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="next-button"]')).nativeElement;

    nextButtonElement.click();

    expect(nextButtonElement.hasAttribute('disabled')).toBeFalsy();
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should reduce page start count based on page size when back button is clicked', () => {
    spyOn(component.onPaginationChange, 'emit');
    const pagination: Pagination = {
      end: 10,
      size: 5,
      start: 9,
      total: 22
    };
    component.pagination = { ...pagination };
    fixture.detectChanges();
    const previousButtonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="back-button"]')).nativeElement;

    previousButtonElement.click();

    expect(component.pagination).toEqual({ ...pagination, start: 4 });
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should reduce the page start count to 0 if the difference between page start and page size is below 0 when the previous button is clicked', () => {
    spyOn(component.onPaginationChange, 'emit');
    const pagination: Pagination = {
      end: 10,
      size: 5,
      start: 2,
      total: 22
    };
    component.pagination = { ...pagination };
    fixture.detectChanges();
    const previousButtonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="back-button"]')).nativeElement;

    previousButtonElement.click();

    expect(component.pagination).toEqual({ ...pagination, start: 0 });
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should increase page start count based on page size when next button is clicked', () => {
    spyOn(component.onPaginationChange, 'emit');
    const pagination: Pagination = {
      end: 10,
      size: 10,
      start: 10,
      total: 22
    };
    component.pagination = { ...pagination };
    fixture.detectChanges();
    const nextButtonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="next-button"]')).nativeElement;

    nextButtonElement.click();

    expect(component.pagination).toEqual({ ...pagination, start: 20 });
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });
});
