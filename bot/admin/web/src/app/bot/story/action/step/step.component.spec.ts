/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbDialogService } from '@nebular/theme';
import { of } from 'rxjs';
import { DialogService } from '../../../../core-nlp/dialog.service';
import { StateService } from '../../../../core-nlp/state.service';
import { NlpService } from '../../../../core-nlp/nlp.service';
import { TestSharedModule } from '../../../../shared/test-shared.module';
import { BotService } from '../../../bot-service';
import { AnswerConfigurationType, IntentName, SimpleAnswerConfiguration, StoryStep } from '../../../model/story';
import { StepComponent } from './step.component';

const stepMock = new StoryStep(
  'testStep',
  new IntentName('testIntent'),
  new IntentName('testTargetIntent'),
  [new SimpleAnswerConfiguration([])],
  AnswerConfigurationType.simple,
  'testCategory',
  null,
  [],
  0,
  undefined,
  [
    { indicatorName: 'indicateur1', indicatorValueName: 'val1' },
    { indicatorName: 'indicateur2', indicatorValueName: 'val1' }
  ]
);

const indicatorsMock = [
  {
    name: 'indicateur1',
    label: 'Indicateur 1',
    description: 'Indicateur 1 desc',
    applicationName: 'appName',
    dimensions: ['dimension1'],
    values: [
      { name: 'val1', label: 'Val 1' },
      { name: 'val2', label: 'Val 2' }
    ]
  },
  {
    name: 'indicateur2',
    label: 'Indicateur 2',
    description: 'Indicateur 2 desc',
    applicationName: 'appName',
    dimensions: ['dimension1', 'dimension2'],
    values: [
      { name: 'val1', label: 'Val 1' },
      { name: 'val2', label: 'Val 2' }
    ]
  },
  {
    name: 'indicateur3',
    label: 'Indicateur 3',
    description: 'Indicateur 3 desc',
    applicationName: 'appName',
    dimensions: ['dimension2'],
    values: [
      { name: 'val1', label: 'Val 1' },
      { name: 'val2', label: 'Val 2' }
    ]
  }
];

describe('StepComponent', () => {
  let component: StepComponent;
  let fixture: ComponentFixture<StepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule],
      declarations: [StepComponent],
      providers: [
        { provide: StateService, useValue: { currentIntentsCategories: of([]) } },
        { provide: DialogService, useValue: {} },
        { provide: NlpService, useValue: {} },
        { provide: BotService, useValue: {} },
        { provide: NbDialogService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StepComponent);
    component = fixture.componentInstance;
    stepMock.new = true;
    component.step = stepMock;
    component.indicators = indicatorsMock;
    component.ngOnInit();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should define selectedStepsMetrics on init', () => {
    const selectedStepsMetrics = [
      {
        indicatorName: 'indicateur1',
        indicatorValueName: 'val1',
        dimension: 'dimension1'
      },
      {
        indicatorName: 'indicateur2',
        indicatorValueName: 'val1',
        dimension: 'dimension1'
      },
      {
        indicatorName: 'indicateur2',
        indicatorValueName: 'val1',
        dimension: 'dimension2'
      }
    ];
    expect(component.selectedStepsMetrics).toEqual(selectedStepsMetrics);
  });

  it('should define indicatorsValuesGroups', () => {
    const indicatorsValuesGroups = [
      {
        dimension: 'dimension1',
        entries: [
          {
            indicatorName: 'indicateur1',
            indicatorValueName: 'val1',
            dimension: 'dimension1'
          },
          {
            indicatorName: 'indicateur1',
            indicatorValueName: 'val2',
            dimension: 'dimension1'
          },
          {
            indicatorName: 'indicateur2',
            indicatorValueName: 'val1',
            dimension: 'dimension1'
          },
          {
            indicatorName: 'indicateur2',
            indicatorValueName: 'val2',
            dimension: 'dimension1'
          }
        ]
      },
      {
        dimension: 'dimension2',
        entries: [
          {
            indicatorName: 'indicateur2',
            indicatorValueName: 'val1',
            dimension: 'dimension2'
          },
          {
            indicatorName: 'indicateur2',
            indicatorValueName: 'val2',
            dimension: 'dimension2'
          },
          {
            indicatorName: 'indicateur3',
            indicatorValueName: 'val1',
            dimension: 'dimension2'
          },
          {
            indicatorName: 'indicateur3',
            indicatorValueName: 'val2',
            dimension: 'dimension2'
          }
        ]
      }
    ];

    expect(component.indicatorsValuesGroups).toEqual(indicatorsValuesGroups);
  });

  it('should return the right metric label format', () => {
    const metricLabel = component.getMetricLabel(component.step.metrics[0]);
    expect(metricLabel).toEqual('Indicateur 1 : Val 1');
  });

  it('should disable select options when needed', () => {
    const mapArray = [
      {
        key: {
          indicatorName: 'indicateur1',
          indicatorValueName: 'val1',
          dimension: 'dimension1'
        },
        value: false
      },
      {
        key: {
          indicatorName: 'indicateur1',
          indicatorValueName: 'val2',
          dimension: 'dimension1'
        },
        value: true
      },
      {
        key: {
          indicatorName: 'indicateur2',
          indicatorValueName: 'val1',
          dimension: 'dimension1'
        },
        value: false
      },
      {
        key: {
          indicatorName: 'indicateur2',
          indicatorValueName: 'val2',
          dimension: 'dimension1'
        },
        value: true
      },
      {
        key: {
          indicatorName: 'indicateur2',
          indicatorValueName: 'val1',
          dimension: 'dimension2'
        },
        value: false
      },
      {
        key: {
          indicatorName: 'indicateur2',
          indicatorValueName: 'val2',
          dimension: 'dimension2'
        },
        value: true
      },
      {
        key: {
          indicatorName: 'indicateur3',
          indicatorValueName: 'val1',
          dimension: 'dimension2'
        },
        value: false
      },
      {
        key: {
          indicatorName: 'indicateur3',
          indicatorValueName: 'val2',
          dimension: 'dimension2'
        },
        value: false
      }
    ];

    const select = fixture.debugElement.query(By.css('[data-testid="selected-steps-Metrics"]'));
    const options = select.queryAll(By.css('nb-option'));
    options.forEach((option) => {
      const val = option.nativeNode.value;
      const entry = mapArray.find(
        (ma) =>
          ma.key.indicatorName === val.indicatorName &&
          ma.key.indicatorValueName === val.indicatorValueName &&
          ma.key.dimension === val.dimension
      );
      expect(option.nativeNode.disabled).toEqual(entry.value);
    });
  });
});
