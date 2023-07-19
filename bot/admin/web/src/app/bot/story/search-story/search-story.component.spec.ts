import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import {
  NbButtonModule,
  NbCardModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbTooltipModule,
  NbSelectModule,
  NbDialogService,
  NbToastrService
} from '@nebular/theme';
import { of } from 'rxjs';
import { DialogService } from '../../../core-nlp/dialog.service';
import { StateService } from '../../../core-nlp/state.service';
import { BotConfigurationService } from '../../../core/bot-configuration.service';

import { TestSharedModule } from '../../../shared/test-shared.module';
import { BotService } from '../../bot-service';
import { StoryDefinitionConfigurationSummary } from '../../model/story';
import { SearchStoryComponent } from './search-story.component';

const stories = [
  {
    _id: '641c1753bdc29e7464b6eacc',
    storyId: 'logement',
    botId: 'new_assistant',
    intent: {
      name: 'logement'
    },
    currentType: 'simple',
    name: '1% logement',
    category: 'faq',
    description: '',
    lastEdited: '2023-07-11T12:28:31.065Z'
  } as unknown as StoryDefinitionConfigurationSummary,
  {
    _id: '641c1754bdc29e7464b6eae1',
    storyId: 'mois',
    botId: 'new_assistant',
    intent: {
      name: 'mois'
    },
    currentType: 'simple',
    name: '14,5 mois',
    category: 'faq',
    description: '',
    lastEdited: '2023-07-11T12:28:31.426Z'
  } as unknown as StoryDefinitionConfigurationSummary,
  {
    _id: '641c174fbdc29e7464b6ea2a',
    storyId: 'annuaire',
    botId: 'new_assistant',
    intent: {
      name: 'annuaire'
    },
    currentType: 'simple',
    name: 'Annuaire',
    category: 'faq',
    description: '',
    lastEdited: '2023-07-11T12:28:28.106Z'
  } as unknown as StoryDefinitionConfigurationSummary
];

fdescribe('SearchStoryComponent', () => {
  let component: SearchStoryComponent;
  let fixture: ComponentFixture<SearchStoryComponent>;

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
      declarations: [SearchStoryComponent],
      providers: [
        {
          provide: NbDialogService,
          useValue: { open: () => {} }
        },
        {
          provide: DialogService,
          useValue: { open: () => {} }
        },
        {
          provide: NbToastrService,
          useValue: { show: () => {} }
        },
        {
          provide: BotConfigurationService,
          useValue: { configurations: of([{}]) }
        },
        {
          provide: Router,
          useValue: {
            getCurrentNavigation: () => {
              return { extras: { state: { category: 'test' } } };
            }
          }
        },
        {
          provide: StateService,
          useValue: { currentApplication: { name: 'TestApp', namespace: 'TestNamespace' }, currentLocale: 'fr' }
        },
        {
          provide: BotService,
          useValue: {
            searchStories: () => {
              return of(stories);
            }
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchStoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load stories', () => {
    expect(component.stories).toEqual(stories);
  });

  it('should initialize stories categories', () => {
    const storyCategories = [
      {
        category: 'faq',
        stories: [
          {
            _id: '641c1753bdc29e7464b6eacc',
            storyId: 'logement',
            botId: 'new_assistant',
            intent: {
              name: 'logement'
            },
            currentType: 'simple',
            name: '1% logement',
            category: 'faq',
            description: '',
            lastEdited: '2023-07-11T12:28:31.065Z'
          } as unknown as StoryDefinitionConfigurationSummary,
          {
            _id: '641c1754bdc29e7464b6eae1',
            storyId: 'mois',
            botId: 'new_assistant',
            intent: {
              name: 'mois'
            },
            currentType: 'simple',
            name: '14,5 mois',
            category: 'faq',
            description: '',
            lastEdited: '2023-07-11T12:28:31.426Z'
          } as unknown as StoryDefinitionConfigurationSummary,
          {
            _id: '641c174fbdc29e7464b6ea2a',
            storyId: 'annuaire',
            botId: 'new_assistant',
            intent: {
              name: 'annuaire'
            },
            currentType: 'simple',
            name: 'Annuaire',
            category: 'faq',
            description: '',
            lastEdited: '2023-07-11T12:28:28.106Z'
          } as unknown as StoryDefinitionConfigurationSummary
        ]
      }
    ];
    expect(component.storyCategories).toEqual(storyCategories);
  });

  it('should initialize filtered stories', () => {
    const filteredStories = [
      {
        _id: '641c1753bdc29e7464b6eacc',
        storyId: 'logement',
        botId: 'new_assistant',
        intent: {
          name: 'logement'
        },
        currentType: 'simple',
        name: '1% logement',
        category: 'faq',
        description: '',
        lastEdited: '2023-07-11T12:28:31.065Z'
      } as unknown as StoryDefinitionConfigurationSummary,
      {
        _id: '641c1754bdc29e7464b6eae1',
        storyId: 'mois',
        botId: 'new_assistant',
        intent: {
          name: 'mois'
        },
        currentType: 'simple',
        name: '14,5 mois',
        category: 'faq',
        description: '',
        lastEdited: '2023-07-11T12:28:31.426Z'
      } as unknown as StoryDefinitionConfigurationSummary,
      {
        _id: '641c174fbdc29e7464b6ea2a',
        storyId: 'annuaire',
        botId: 'new_assistant',
        intent: {
          name: 'annuaire'
        },
        currentType: 'simple',
        name: 'Annuaire',
        category: 'faq',
        description: '',
        lastEdited: '2023-07-11T12:28:28.106Z'
      } as unknown as StoryDefinitionConfigurationSummary
    ];
    expect(component.filteredStories).toEqual(filteredStories);
  });
});
