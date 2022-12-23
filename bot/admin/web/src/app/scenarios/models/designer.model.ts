import { ParseQuery } from '../../model/nlp';
import { ScenarioAnswer, ScenarioItem } from './scenario.model';

export class TempSentence extends ParseQuery {
  public classification: TempClassification;

  constructor(
    public namespace: string,
    public applicationName: string,
    public language: string,
    public query: string,
    public checkExistingQuery: boolean,
    public state?: string
  ) {
    super(namespace, applicationName, language, query, checkExistingQuery);
    this.classification = { entities: [] };
  }
}
export interface TempEntity {
  type: string;
  role: string;
  start: number;
  end: number;
  entityColor: string;
  qualifiedRole?: string;
  subEntities: any[];
}
export interface TempClassification {
  entities: TempEntity[];
  intentId?: string;
}

export interface Transition {
  name: string;
  target: string;
}

export interface IntegrityCheckResult {
  valid: boolean;
  reason?: string;
}

export interface DependencyUpdateJob {
  type: 'creation' | 'update';
  done: boolean;
  item: ScenarioItem;
  answer?: ScenarioAnswer;
}
