import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbIconModule,
  NbInputModule,
  NbSpinnerModule,
  NbTagModule,
  NbTooltipModule
} from '@nebular/theme';

import { DialogService } from '../../../core-nlp/dialog.service';
import { FormControlComponent } from '../../../shared/components';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { IndicatorsEditComponent } from './indicators-edit.component';

const mockIndicator = {
  existing: false,
  indicator: {
    name: '',
    label: '',
    description: '',
    values: [],
    dimensions: []
  }
};

describe('IndicatorsEditComponent', () => {
  let component: IndicatorsEditComponent;
  let fixture: ComponentFixture<IndicatorsEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TestSharedModule,
        NbCardModule,
        NbTooltipModule,
        NbButtonModule,
        NbInputModule,
        NbAutocompleteModule,
        NbIconModule,
        NbTagModule,
        NbSpinnerModule
      ],
      declarations: [IndicatorsEditComponent, FormControlComponent],
      providers: [{ provide: DialogService, useValue: {} }]
    }).compileComponents();

    fixture = TestBed.createComponent(IndicatorsEditComponent);
    component = fixture.componentInstance;
    component.indicatorEdition = mockIndicator;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
