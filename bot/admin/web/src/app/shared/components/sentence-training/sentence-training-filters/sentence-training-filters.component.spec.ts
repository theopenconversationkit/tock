import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import {
  NbButtonModule,
  NbCardModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbToggleModule,
  NbTooltipModule
} from '@nebular/theme';

import { TestSharedModule } from '../../../../shared/test-shared.module';
import { SentenceTrainingFiltersComponent } from './sentence-training-filters.component';

describe('SentenceTrainingFiltersComponent', () => {
  let component: SentenceTrainingFiltersComponent;
  let fixture: ComponentFixture<SentenceTrainingFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SentenceTrainingFiltersComponent],
      imports: [
        TestSharedModule,
        NbButtonModule,
        NbCardModule,
        NbFormFieldModule,
        NbIconModule,
        NbInputModule,
        NbToggleModule,
        NbTooltipModule
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SentenceTrainingFiltersComponent);
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
    expect(onFilterSpy).toHaveBeenCalledOnceWith({ search: 'test', showUnknown: false });
  }));

  it('should show clear button of search input if the search value is defined', () => {
    let clearButtonElement = fixture.debugElement.query(By.css('[data-testid="clear-button"]'));

    expect(clearButtonElement).toBeFalsy();

    component.form.patchValue({ search: 'test' });
    fixture.detectChanges();
    clearButtonElement = fixture.debugElement.query(By.css('[data-testid="clear-button"]')).nativeElement;

    expect(clearButtonElement).toBeTruthy();
  });

  it('should call the method to clear search filter when click on clear search button', () => {
    spyOn(component, 'clearSearch');
    component.form.patchValue({ search: 'test' });
    fixture.detectChanges();
    const clearButtonElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="clear-button"]')).nativeElement;

    clearButtonElement.click();
    fixture.detectChanges();

    expect(component.clearSearch).toHaveBeenCalledTimes(1);
  });

  it('should clear search when the method is called', () => {
    component.form.patchValue({ search: 'test' });

    component.clearSearch();

    expect(component.search.value).toBeNull();
  });
});
