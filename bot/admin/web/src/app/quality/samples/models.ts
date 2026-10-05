import { DialogReport } from '../../shared/model/dialog-data';

export enum EvaluationSampleStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  VALIDATED = 'VALIDATED'
}

export interface EvaluationSampleResultDefinition {
  positiveCount: number;
  negativeCount: number;
  evaluated: number;
  remaining: number;
  total: number;
}

export interface EvaluationSampleDefinition {
  _id: string;
  botId: string;
  namespace: string;
  name: string;
  description: string;
  dialogActivityFrom: string;
  dialogActivityTo: string;
  allowTestDialogs: boolean;
  creationDate: string;
  createdBy: string;
  statusChangeDate: string | null;
  statusComment: string;
  statusChangedBy: string | null;
  requestedDialogCount: number;
  dialogsCount: number;
  createdFromRun?: string | null;
  totalDialogCount: number | null;
  botActionCount: number;
  status: EvaluationSampleStatus;
  evaluationsResult: EvaluationSampleResultDefinition;
}

export enum EvaluationStatus {
  UNSET = 'UNSET',
  UP = 'UP',
  DOWN = 'DOWN'
}

type Evaluator = {
  id: string;
};

type EvaluationBase = {
  _id: string;
  dialogId: string;
  actionId: string;
  evaluationSampleId: string;
  evaluator: Evaluator;
  evaluationDate: string;
};

type EvaluationUnset = EvaluationBase & {
  status: EvaluationStatus.UNSET;
  reason?: null;
};

type EvaluationUp = EvaluationBase & {
  status: EvaluationStatus.UP;
  reason?: null;
};

type EvaluationDown = EvaluationBase & {
  status: EvaluationStatus.DOWN;
  reason: string;
};

export type EvaluationDefinition = EvaluationUnset | EvaluationUp | EvaluationDown;

export interface EvaluationSampleDataDefinition {
  dialogs: DialogReport[];
  evaluations: EvaluationDefinition[];
}
