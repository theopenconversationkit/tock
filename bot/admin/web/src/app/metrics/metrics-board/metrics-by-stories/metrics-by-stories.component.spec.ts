import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbCardModule, NbDialogRef } from '@nebular/theme';
import { of } from 'rxjs';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { StateService } from '../../../core-nlp/state.service';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { MetricResult } from '../../models';

import { MetricsByStoriesComponent } from './metrics-by-stories.component';

const indicatorMetrics: MetricResult[] = [
  {
    row: {
      type: 'QUESTION_ASKED',
      indicatorName: 'test'
    },
    count: 2
  },
  {
    row: {
      type: 'QUESTION_REPLIED',
      indicatorName: 'test',
      indicatorValueName: 'oui'
    },
    count: 1
  }
];

describe('MetricsByStoriesComponent', () => {
  let component: MetricsByStoriesComponent;
  let fixture: ComponentFixture<MetricsByStoriesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule, NbCardModule],
      declarations: [MetricsByStoriesComponent],
      providers: [
        { provide: NbDialogRef, useValue: { close: () => {} } },
        {
          provide: StateService,
          useValue: { currentApplication: { name: 'TestApp' } }
        },
        {
          provide: RestService,
          useValue: { post: () => of(indicatorMetrics) }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MetricsByStoriesComponent);
    component = fixture.componentInstance;
    component.range = {
      start: new Date('2023-01-17'),
      end: new Date('2023-04-17')
    };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
