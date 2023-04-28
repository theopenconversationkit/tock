import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbButtonModule, NbIconModule, NbSelectComponent, NbSelectModule, NbTooltipModule } from '@nebular/theme';

import { Pagination, PaginationComponent } from './pagination.component';
import { TestingModule } from '../../../../testing';

describe('PaginationComponent', () => {
  let component: PaginationComponent;
  let fixture: ComponentFixture<PaginationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PaginationComponent, NbSelectComponent],
      imports: [TestingModule, NbButtonModule, NbIconModule, NbSelectModule, NbTooltipModule]
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
    const previousButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-back-outline"]')).nativeElement
      .parentElement;

    previousButtonElement.click();

    expect(previousButtonElement.hasAttribute('disabled')).toBeTruthy();
    expect(component.onPaginationChange.emit).not.toHaveBeenCalled();
  });

  it('should enable the back button if the beginning of the page is strictly upper than 0', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { start: 1, size: 10 } as Pagination;
    fixture.detectChanges();
    const previousButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-back-outline"]')).nativeElement
      .parentElement;

    previousButtonElement.click();

    expect(previousButtonElement.hasAttribute('disabled')).toBeFalsy();
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should disable the next button if the end of the page is greater than or equal to the total of the result', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 22, total: 22 } as Pagination;
    fixture.detectChanges();

    const nextButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-forward-outline"]')).nativeElement
      .parentElement;

    nextButtonElement.click();

    expect(nextButtonElement.hasAttribute('disabled')).toBeTruthy();
    expect(component.onPaginationChange.emit).not.toHaveBeenCalled();

    component.pagination = { end: 28, total: 22 } as Pagination;
    fixture.detectChanges();
    nextButtonElement.click();

    expect(nextButtonElement.hasAttribute('disabled')).toBeTruthy();
    expect(component.onPaginationChange.emit).not.toHaveBeenCalled();
  });

  it('should enable the next button if the ending of the page is strictly less than the total of result', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 13, total: 22 } as Pagination;
    fixture.detectChanges();
    const nextButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-forward-outline"]')).nativeElement
      .parentElement;

    nextButtonElement.click();

    expect(nextButtonElement.hasAttribute('disabled')).toBeFalsy();
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should reduce page start count based on page size when back button is clicked', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 15, size: 5, start: 10, total: 22 };
    fixture.detectChanges();
    const previousButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-back-outline"]')).nativeElement
      .parentElement;

    previousButtonElement.click();

    expect(component.pagination.start).toBe(5);
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should reduce the page start count to 0 if the difference between page start and page size is below 0 when the previous button is clicked', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 7, size: 5, start: 2, total: 22 };
    fixture.detectChanges();
    const previousButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-back-outline"]')).nativeElement
      .parentElement;

    previousButtonElement.click();

    expect(component.pagination.start).toBe(0);
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should increase page start count based on page size when next button is clicked', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 20, size: 10, start: 10, total: 22 };
    fixture.detectChanges();
    const nextButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-forward-outline"]')).nativeElement
      .parentElement;

    nextButtonElement.click();

    expect(component.pagination.start).toBe(20);
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should reduce page end count based on page size and start when back button is clicked', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 15, size: 5, start: 10, total: 22 };
    fixture.detectChanges();
    const previousButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-back-outline"]')).nativeElement
      .parentElement;

    previousButtonElement.click();

    expect(component.pagination.end).toBe(10);
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should increase page start count based on page size when next button is clicked', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 10, size: 10, start: 0, total: 22 };
    fixture.detectChanges();
    const nextButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-forward-outline"]')).nativeElement
      .parentElement;

    nextButtonElement.click();

    expect(component.pagination.end).toBe(20);
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should increase the number of page starts based on the page size when the next button is clicked and take the value of total if the total is greater', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 20, size: 10, start: 10, total: 22 };
    fixture.detectChanges();
    const nextButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-forward-outline"]')).nativeElement
      .parentElement;

    nextButtonElement.click();

    expect(component.pagination.end).toBe(22);
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  /**
   * to avoid a complex request for retrieving elements then modifying the value of the pagination size, we directly simulate the result of the change by the user and the call to the method
   */
  it('should reset start and end when size is changed', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 20, size: 10, start: 10, total: 22 };
    fixture.detectChanges();

    component.pagination.size = 20;
    component.paginationSize();

    expect(component.pagination).toEqual({ end: 20, size: 20, start: 0, total: 22 });
    expect(component.onPaginationChange.emit).toHaveBeenCalled();

    component.pagination.size = 30;
    component.paginationSize();

    // end must take the value of total, total being less than the size
    expect(component.pagination).toEqual({ end: 22, size: 30, start: 0, total: 22 });
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should not update page size when next button is clicked', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 20, size: 10, start: 10, total: 22 };
    fixture.detectChanges();
    const nextButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-forward-outline"]')).nativeElement
      .parentElement;

    nextButtonElement.click();

    expect(component.pagination.size).toBe(10);
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should not update page size when previous button is clicked', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination = { end: 20, size: 10, start: 10, total: 22 };
    fixture.detectChanges();
    const previousButtonElement: HTMLElement = fixture.debugElement.query(By.css('[icon="arrow-ios-back-outline"]')).nativeElement
      .parentElement;

    previousButtonElement.click();

    expect(component.pagination.size).toBe(10);
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });
});
