import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbFormFieldModule, NbIconModule, NbInputModule, NbSelectModule, NbTooltipModule } from '@nebular/theme';

import { TestSharedModule } from '../../../shared/test-shared.module';
import { IndicatorsFiltersComponent } from './indicators-filters.component';

describe('IndicatorsFiltersComponent', () => {
  let component: IndicatorsFiltersComponent;
  let fixture: ComponentFixture<IndicatorsFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule, NbFormFieldModule, NbInputModule, NbSelectModule, NbIconModule, NbTooltipModule],
      declarations: [IndicatorsFiltersComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(IndicatorsFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
