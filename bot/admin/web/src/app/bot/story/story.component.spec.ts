import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogService } from '@nebular/theme';
import { of } from 'rxjs';
import { DialogService } from '../../core-nlp/dialog.service';
import { StateService } from '../../core-nlp/state.service';
import { TestSharedModule } from '../../shared/test-shared.module';
import { BotService } from '../bot-service';
import { AnswerConfigurationType, IntentName, SimpleAnswerConfiguration, StoryDefinitionConfiguration, StoryStep } from '../model/story';
import { StoryComponent } from './story.component';

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

const storyMock = new StoryDefinitionConfiguration(
  'testStory',
  'abcd',
  new IntentName('testIntent'),
  AnswerConfigurationType.simple,
  'testNamespace',
  [],
  'build',
  'testStory',
  '',
  'fr',
  []
);

describe('StoryComponent', () => {
  let component: StoryComponent;
  let fixture: ComponentFixture<StoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule],
      declarations: [StoryComponent],
      providers: [
        { provide: StateService, useValue: { currentIntentsCategories: of([]) } },
        { provide: BotService, useValue: {} },
        { provide: DialogService, useValue: {} },
        { provide: NbDialogService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StoryComponent);
    component = fixture.componentInstance;
    component.story = storyMock;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should evaluate if a story can be a metric story', () => {
    expect(component.canBeMetricStory()).toBeFalsy();
    component.story.steps = [stepMock];
    expect(component.canBeMetricStory()).toBeTruthy();
  });
});
