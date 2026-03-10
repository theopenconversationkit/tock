import { RagSettings } from '../../rag/rag-settings/models';

export enum DatasetRunState {
  QUEUED = 'QUEUED',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export enum DatasetRunActionState {
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export interface DatasetRunAction {
  datasetId: string;
  runId: string;
  questionId: string;
  state: DatasetRunActionState;
  action: any | null; // ActionReport | null
  retryCount: number;
}

export interface DatasetRunStats {
  totalQuestions: number;
  completedQuestions: number;
  failedQuestions: number;
}

export interface DatasetRun {
  id: string;
  state: DatasetRunState;
  startTime: string; // ISO 8601
  endTime: string | null; // ISO 8601
  settingsSnapshot?: Partial<RagSettings>;
  startedBy: string;
  stats: DatasetRunStats;
}

export interface DatasetQuestion {
  id: string;
  question: string;
  groundTruth: string;
}

export interface Dataset {
  id: string;
  name: string;
  description: string;
  questions: DatasetQuestion[];
  runs: DatasetRun[];
  createdAt: string; // ISO 8601
  createdBy: string;
  updatedAt: string | null; // ISO 8601
  updatedBy: string | null;
  // Front-only flag — true once GET /datasets/:id has been called and settingsSnapshot
  // fields are populated. Never sent to or expected from the backend.
  _settingsLoaded?: boolean;
}

// Front-only — derived from DatasetRunAction.state + action nullability.
// Never sent to or expected from the backend.
export enum DatasetRunActionDisplayState {
  SUCCESS = 'SUCCESS', // state COMPLETED + action présent
  FAILED = 'FAILED', // state FAILED, pas de rapport disponible
  PURGED = 'PURGED' // state COMPLETED + action null, dialogue purgé de la base
}

export interface SourceInfos {
  title: string;
  url?: string;
  content: string;
  _detail?: boolean; // Front-only flag to indicate if the source is shown in detail view. Never sent to or expected from the backend.
}

export interface SourcesDiffResult {
  added: SourceInfos[];
  removed: SourceInfos[];
  modified: SourceInfos[];
}
