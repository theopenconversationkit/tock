import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbAlertModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NbToastrService } from '@nebular/theme';
import { of } from 'rxjs';

import { DialogService } from '../../core-nlp/dialog.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { TestSharedModule } from '../../shared/test-shared.module';
import { IndicatorDefinition } from '../models';
import { IndicatorsComponent } from './indicators.component';

const indicator1: IndicatorDefinition = { name: 'test', label: 'test', description: 'test desc', dimensions: ['test'], values: [] };
const indicator2: IndicatorDefinition = {
  name: 'otherTest',
  label: 'Other Test',
  description: 'Other Test desc',
  dimensions: ['test', 'Other test dim'],
  values: []
};

describe('IndicatorsComponent', () => {
  let component: IndicatorsComponent;
  let fixture: ComponentFixture<IndicatorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule, NbButtonModule, NbAlertModule, NbSpinnerModule, NbCardModule, NbIconModule, NbSpinnerModule],
      declarations: [IndicatorsComponent],
      providers: [
        { provide: BotConfigurationService, useValue: { configurations: of([{ applicationId: 'TestApp' }]) } },
        { provide: StateService, useValue: { currentApplication: { name: 'TestApp' } } },
        { provide: RestService, useValue: { get: () => of([indicator1, indicator2]), post: (_, ind) => of(ind) } },
        { provide: NbToastrService, useValue: { success: () => {} } },
        { provide: DialogService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IndicatorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load indicators on init', () => {
    expect(component.indicators).toEqual([indicator1, indicator2]);
  });

  it('should filter indicators according to indicators filters', () => {
    component.filterIndicators({
      search: '',
      dimensions: ['test']
    });
    expect(component.filteredIndicators).toEqual([indicator2, indicator1]);

    component.filterIndicators({
      search: 'Other test desc',
      dimensions: []
    });
    expect(component.filteredIndicators).toEqual([indicator2]);

    component.filterIndicators({
      search: 'test',
      dimensions: ['Other test dim']
    });
    expect(component.filteredIndicators).toEqual([indicator2]);
  });

  it('should maintain a cache list of dimensions', () => {
    expect(component.dimensionsCache).toEqual(['Other test dim', 'test']);
  });

  it('should always return a unic indicator name', () => {
    const newIndicator1Name = component.getUnicIndicatorName(indicator2);
    expect(newIndicator1Name).toEqual('otherTest1');
    component.indicators.push({ ...indicator2, name: newIndicator1Name });
    const newIndicator2Name = component.getUnicIndicatorName(indicator2);
    expect(newIndicator2Name).toEqual('otherTest2');
  });

  it('should add a unic name when posting an indicator', () => {
    component.saveOrCreateIndicator({
      existing: false,
      indicator: {
        label: 'Other test'
      } as IndicatorDefinition
    });
    const newIndicator = component.indicators.find((ind) => ind.name === 'otherTest1');
    expect(newIndicator).toBeTruthy();
  });
});
