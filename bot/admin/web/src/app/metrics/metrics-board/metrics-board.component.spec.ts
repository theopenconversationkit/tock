import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  NbButtonModule,
  NbCardModule,
  NbDateService,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbTooltipModule,
  NbSelectModule
} from '@nebular/theme';

import { TestSharedModule } from '../../shared/test-shared.module';
import { MetricsBoardComponent } from './metrics-board.component';

describe('MetricsBoardComponent', () => {
  let component: MetricsBoardComponent;
  let fixture: ComponentFixture<MetricsBoardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TestSharedModule,
        NbCardModule,
        NbFormFieldModule,
        NbIconModule,
        NbInputModule,
        NbTooltipModule,
        NbButtonModule,
        NbSelectModule
      ],
      declarations: [MetricsBoardComponent],
      providers: [
        {
          provide: NbDateService,
          useValue: { getMonthStart: () => '01', getMonthEnd: () => '31', addDay: (date: Date, days: number) => {} }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricsBoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
