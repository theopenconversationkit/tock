import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbButtonModule, NbIconModule, NbSelectModule, NbTooltipModule } from '@nebular/theme';

import { TestSharedModule } from '../test-shared.module';
import { Pagination, PaginationComponent } from './pagination.component';

describe('NoDataFoundComponent', () => {
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
    const previousButtonElement: HTMLElement = fixture.debugElement.query(
      By.css('[icon="arrow-ios-back-outline"]')
    ).nativeElement.parentElement;

    previousButtonElement.click();

    expect(previousButtonElement.hasAttribute('disabled')).toBeTruthy();
    expect(component.onPaginationChange.emit).not.toHaveBeenCalled();
  });

  it('should enable the back button if the beginning of the page is strictly upper than 0', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination.start = 1;
    component.pagination.size = 10;
    fixture.detectChanges();
    const previousButtonElement: HTMLElement = fixture.debugElement.query(
      By.css('[icon="arrow-ios-back-outline"]')
    ).nativeElement.parentElement;

    previousButtonElement.click();

    expect(previousButtonElement.hasAttribute('disabled')).toBeFalsy();
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });

  it('should disable the next button if the end of the page is greater than or equal to the total of the result', () => {
    spyOn(component.onPaginationChange, 'emit');
    component.pagination.end = 22;
    component.pagination.total = 22;
    fixture.detectChanges();

    const nextButtonElement: HTMLElement = fixture.debugElement.query(
      By.css('[icon="arrow-ios-forward-outline"]')
    ).nativeElement.parentElement;

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
    const nextButtonElement: HTMLElement = fixture.debugElement.query(
      By.css('[icon="arrow-ios-forward-outline"]')
    ).nativeElement.parentElement;

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
    const previousButtonElement: HTMLElement = fixture.debugElement.query(
      By.css('[icon="arrow-ios-back-outline"]')
    ).nativeElement.parentElement;

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
    const previousButtonElement: HTMLElement = fixture.debugElement.query(
      By.css('[icon="arrow-ios-back-outline"]')
    ).nativeElement.parentElement;

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
    const nextButtonElement: HTMLElement = fixture.debugElement.query(
      By.css('[icon="arrow-ios-forward-outline"]')
    ).nativeElement.parentElement;

    nextButtonElement.click();

    expect(component.pagination).toEqual({ ...pagination, start: 20 });
    expect(component.onPaginationChange.emit).toHaveBeenCalled();
  });
});
