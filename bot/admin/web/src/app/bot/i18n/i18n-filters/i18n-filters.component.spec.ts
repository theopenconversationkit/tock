import { ComponentFixture, TestBed } from '@angular/core/testing';

import { I18nFiltersComponent } from './i18n-filters.component';

describe('I18nFiltersComponent', () => {
  let component: I18nFiltersComponent;
  let fixture: ComponentFixture<I18nFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ I18nFiltersComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(I18nFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
