import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { of } from 'rxjs';
import { BotConfigurationService } from '../../core/bot-configuration.service';

import { RagSourcesBoardComponent } from './rag-sources-board.component';
import { SourceManagementService } from './source-management.service';
import { IndexingSession, ProcessAdvancement, Source, SourceTypes } from './models';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { BotApplicationConfiguration } from '../../core/model/configuration';

const mockBotConfigurations: BotApplicationConfiguration[] = [
  {
    applicationId: 'new_assistant',
    botId: 'new_assistant',
    namespace: 'app',
    nlpModel: 'new_assistant',
    connectorType: {
      id: 'web',
      userInterfaceType: 0
    },
    ownerConnectorType: null,
    name: 'new_assistant',
    baseUrl: 'http://bot_api:8080',
    parameters: {},
    path: '/io/app/new_assistant/web',
    _id: '64c3d733f81d9475724819a8',
    targetConfigurationId: null
  } as BotApplicationConfiguration
];

const mockSources: Source[] = [
  {
    id: '123456789',
    name: 'Data source one',
    enabled: true,
    description: 'Data source one description',
    source_type: SourceTypes.remote,
    status: ProcessAdvancement.complete,
    source_parameters: {
      source_url: new URL('https://www.sourceone.test'),
      exclusion_urls: [new URL('https://www.sourceone.test/home'), new URL('https://www.sourceone.test/cgu')],
      xpaths: ['//*[@id="st-faq-root"]/section/div/div[2]'],
      periodic_update: true,
      periodic_update_frequency: 30
    },
    current_indexing_session_id: '111111111',
    indexing_sessions: [
      {
        id: '111111111',
        start_date: new Date('2023-07-24T12:06:11.106Z'),
        end_date: new Date('2023-07-24T14:22:07.106Z'),
        embeding_engine: 'text-embedding-ada-002',
        status: ProcessAdvancement.complete
      },
      {
        id: '222222222',
        start_date: new Date('2023-07-25T12:06:11.106Z'),
        end_date: new Date('2023-07-25T14:22:07.106Z'),
        embeding_engine: 'text-embedding-ada-002',
        status: ProcessAdvancement.running
      }
    ]
  },
  {
    id: '987654321',
    enabled: false,
    name: 'Data source two',
    description: 'Data source two description',
    source_type: SourceTypes.file,
    status: ProcessAdvancement.pristine,
    source_parameters: {
      file_format: 'json'
    }
  }
];

describe('BoardComponent', () => {
  let component: RagSourcesBoardComponent;
  let fixture: ComponentFixture<RagSourcesBoardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RagSourcesBoardComponent],
      providers: [
        {
          provide: BotConfigurationService,
          useValue: { configurations: of(mockBotConfigurations) }
        },
        {
          provide: NbDialogService,
          useValue: { open: () => {} }
        },
        {
          provide: NbToastrService,
          useValue: { success: () => {} }
        },
        {
          provide: SourceManagementService,
          useValue: {
            getSources: () => of(mockSources),
            postSource: (source) => {},
            updateSource: (sourcePartial) => {},
            deleteSource: (sourceId) => {},
            postIndexingSession: (source, data) => {},
            getIndexingSession: (source, session) => {},
            deleteIndexingSession: (source, session) => {}
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RagSourcesBoardComponent);
    component = fixture.componentInstance;
    spyOn(component, 'watchRunningSessions');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should query and watch running sessions on load', () => {
    const expectedResult: { source: Source; session: IndexingSession }[] = [
      {
        source: {
          id: '123456789',
          name: 'Data source one',
          enabled: true,
          description: 'Data source one description',
          source_type: 'remote' as SourceTypes,
          status: 'complete' as ProcessAdvancement,
          source_parameters: {
            source_url: new URL('https://www.sourceone.test/'),
            exclusion_urls: [new URL('https://www.sourceone.test/home'), new URL('https://www.sourceone.test/cgu')],
            xpaths: ['//*[@id="st-faq-root"]/section/div/div[2]'],
            periodic_update: true,
            periodic_update_frequency: 30
          },
          current_indexing_session_id: '111111111',
          indexing_sessions: [
            {
              id: '111111111',
              start_date: new Date('2023-07-24T12:06:11.106Z'),
              end_date: new Date('2023-07-24T14:22:07.106Z'),
              embeding_engine: 'text-embedding-ada-002',
              status: 'complete' as ProcessAdvancement
            },
            {
              id: '222222222',
              start_date: new Date('2023-07-25T12:06:11.106Z'),
              end_date: new Date('2023-07-25T14:22:07.106Z'),
              embeding_engine: 'text-embedding-ada-002',
              status: 'running' as ProcessAdvancement
            }
          ]
        },
        session: {
          id: '222222222',
          start_date: new Date('2023-07-25T12:06:11.106Z'),
          end_date: new Date('2023-07-25T14:22:07.106Z'),
          embeding_engine: 'text-embedding-ada-002',
          status: 'running' as ProcessAdvancement
        }
      }
    ];
    expect(component.runningSessionsWatcher).toEqual(expectedResult);
    expect(component.watchRunningSessions).toHaveBeenCalledTimes(1);
  });
});
