export enum SourceTypes {
  file = 'file',
  remote = 'remote'
}

export enum ProcessAdvancement {
  pristine = 'pristine',
  running = 'running',
  complete = 'complete',
  error = 'error'
}

export interface SourceParameters {}

export interface FileSourceParameters extends SourceParameters {
  file_format: 'csv' | 'json';

  source_url?: never;
  exclusion_urls?: never;
  xpaths?: never;

  periodic_update?: never;
  periodic_update_frequency?: never;
}

export interface RemoteSourceParameters extends SourceParameters {
  file_format?: never;

  source_url: URL;
  exclusion_urls?: URL[];
  xpaths?: string[];

  periodic_update?: boolean;
  periodic_update_frequency?: number;
}

export interface Source {
  id: string;
  name: string;
  description: string;
  source_type: SourceTypes;

  enabled: boolean;

  status: ProcessAdvancement;

  current_indexing_session_id?: string;
  indexing_sessions?: IndexingSession[];

  source_parameters: FileSourceParameters | RemoteSourceParameters;

  rawData?: any;
}

export interface IndexingSession {
  id: string;
  status: ProcessAdvancement;
  start_date: Date;
  end_date: Date;
  embeding_engine: string;
  tasks?: IndexingSessionTask[];
}

export interface IndexingSessionTask {
  id: string;
  type: IndexingSessionTaskTypes;
  status: ProcessAdvancement;
}

export enum IndexingSessionTaskTypes {
  initialization = 'initialization',
  crawling = 'crawling',
  fetching = 'fetching',
  chunking = 'chunking',
  embeddings = 'embeddings'
}

export enum ImportDataTypes {
  content = 'content',
  source_ref = 'source_ref'
}

export const dataTypesDefinition = [
  { label: 'Content (question, answer...)', type: ImportDataTypes.content, formCtrl: ImportDataTypes.content },
  { label: 'Source reference (public url of the source)', type: ImportDataTypes.source_ref, formCtrl: ImportDataTypes.source_ref }
];

export interface SourceImportData {
  content: string;
  source_ref: string;
}

export interface SourceImportParams {
  content_path: string | string[][];
  source_path: string | string[][];
  content: SourceImportData[];
}
