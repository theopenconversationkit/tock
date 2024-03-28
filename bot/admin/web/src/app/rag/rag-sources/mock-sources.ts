import { ProcessAdvancement, Source, SourceTypes } from './models';

export const TMPsources: Source[] = [
  {
    id: '1234567879',
    enabled: true,
    name: 'CMB Faqs',
    description: '',
    source_type: SourceTypes.file,
    status: ProcessAdvancement.complete,
    source_parameters: {
      file_format: 'csv'
    },
    current_indexing_session_id: '987654',
    indexing_sessions: [
      {
        id: '987654',
        start_date: new Date('2023-07-24T12:06:11.106Z'),
        end_date: new Date('2023-07-24T14:22:07.106Z'),
        embeding_engine: 'text-embedding-ada-002',
        status: ProcessAdvancement.complete
      },
      {
        id: '4654654',
        start_date: new Date('2023-07-21T12:06:11.106Z'),
        end_date: new Date('2023-07-21T12:06:11.106Z'),
        embeding_engine: 'text-embedding-ada-002',
        status: ProcessAdvancement.complete
      }
    ]
  },
  {
    id: '555654654',
    name: 'Faqs CMSO',
    enabled: true,
    description: 'Faqs en ligne CMSO',
    source_type: SourceTypes.remote,
    status: ProcessAdvancement.complete,
    source_parameters: {
      source_url: new URL('https://www.cmso.com/reseau-bancaire-cooperatif/web/aide/faq'),
      exclusion_urls: [
        new URL('https://www.arkea.com/banque/assurance/credit/accueil'),
        new URL('https://www.cmso.com/reseau-bancaire-cooperatif/web/communiques-de-presse-1')
      ],
      xpaths: ['//*[@id="st-faq-root"]/section/div/div[2]'],
      periodic_update: true,
      periodic_update_frequency: 30
    },
    current_indexing_session_id: undefined,
    indexing_sessions: [
      {
        id: '321321',
        start_date: new Date('2023-07-24T12:06:11.106Z'),
        end_date: new Date('2023-07-24T14:22:07.106Z'),
        embeding_engine: 'text-embedding-ada-002',
        status: ProcessAdvancement.complete
      },
      {
        id: '999999',
        start_date: new Date('2023-07-25T12:06:11.106Z'),
        end_date: new Date('2023-07-25T14:22:07.106Z'),
        embeding_engine: 'text-embedding-ada-002',
        status: ProcessAdvancement.running
      }
    ]
  },
  {
    id: '987654321',
    enabled: true,
    name: 'ArkInfo',
    description: '',
    source_type: SourceTypes.file,
    status: ProcessAdvancement.pristine,
    source_parameters: {
      file_format: 'json'
    },
    current_indexing_session_id: undefined,
    indexing_sessions: []
  },
  {
    id: '654',
    enabled: false,
    name: 'Other kind of json source format',
    description: '',
    source_type: SourceTypes.file,
    status: ProcessAdvancement.error,
    source_parameters: {
      file_format: 'json'
    },
    current_indexing_session_id: undefined,
    indexing_sessions: []
  }
];
