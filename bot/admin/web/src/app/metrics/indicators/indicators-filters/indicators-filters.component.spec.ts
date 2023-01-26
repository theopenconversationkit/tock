import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IndicatorsFiltersComponent } from './indicators-filters.component';

describe('IndicatorsFiltersComponent', () => {
  let component: IndicatorsFiltersComponent;
  let fixture: ComponentFixture<IndicatorsFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IndicatorsFiltersComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IndicatorsFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
