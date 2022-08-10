import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
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

import { TestSharedModule } from '../../../shared/test-shared.module';
import { FaqTrainingFiltersComponent } from './faq-training-filters.component';

describe('FaqTrainingFiltersComponent', () => {
  let component: FaqTrainingFiltersComponent;
  let fixture: ComponentFixture<FaqTrainingFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqTrainingFiltersComponent],
      imports: [
        BrowserAnimationsModule,
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
    fixture = TestBed.createComponent(FaqTrainingFiltersComponent);
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
    let clearButtonElement = fixture.debugElement.query(By.css('[nbTooltip="Clear"]'));

    expect(clearButtonElement).toBeFalsy();

    component.form.patchValue({ search: 'test' });
    fixture.detectChanges();
    clearButtonElement = fixture.debugElement.query(By.css('[nbTooltip="Clear"]')).nativeElement;

    expect(clearButtonElement).toBeTruthy();
  });

  it('should clear search filter when click on clear search button', () => {
    spyOn(component, 'clearSearch');
    component.form.patchValue({ search: 'test' });
    fixture.detectChanges();
    const clearButtonElement: HTMLElement = fixture.debugElement.query(By.css('[nbTooltip="Clear"]')).nativeElement;

    clearButtonElement.click();
    fixture.detectChanges();

    expect(component.clearSearch).toHaveBeenCalledTimes(1);
  });
});
