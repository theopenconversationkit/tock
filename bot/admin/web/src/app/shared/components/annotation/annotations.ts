export interface Annotation {
  _id?: string;
  actionId?: string;
  dialogId?: string;
  state: AnnotationState;
  reason: AnnotationReason;
  description: string;
  user: string;
  groundTruth: string;
  events?: AnnotationEvent[];
  createdAt?: Date;
  lastUpdateDate?: Date;
  expiresAt?: Date;
}

export enum AnnotationEventType {
  COMMENT = 'COMMENT',
  STATE = 'STATE',
  REASON = 'REASON',
  GROUND_TRUTH = 'GROUND_TRUTH',
  DESCRIPTION = 'DESCRIPTION'
}

export const AnnotationEventTypes = [
  { label: 'Comment', value: AnnotationEventType.COMMENT },
  { label: 'State', value: AnnotationEventType.STATE },
  { label: 'Reason', value: AnnotationEventType.REASON },
  { label: 'Ground truth', value: AnnotationEventType.GROUND_TRUTH },
  { label: 'Description', value: AnnotationEventType.DESCRIPTION }
];

export interface AnnotationEvent {
  eventId: string;
  type: AnnotationEventType;
  creationDate: Date;
  lastUpdateDate: Date;
  user: string;
  before: any;
  after: any;
  comment?: string;
  _edited?: boolean;
}

export enum AnnotationState {
  ANOMALY = 'ANOMALY',
  REVIEW_NEEDED = 'REVIEW_NEEDED',
  RESOLVED = 'RESOLVED',
  WONT_FIX = 'WONT_FIX'
}

export const AnnotationStates = [
  { label: 'Opened', value: AnnotationState.ANOMALY },
  { label: 'Review needed', value: AnnotationState.REVIEW_NEEDED },
  { label: 'Resolved', value: AnnotationState.RESOLVED },
  { label: "Won't fix", value: AnnotationState.WONT_FIX }
];

export enum AnnotationReason {
  INACCURATE_ANSWER = 'INACCURATE_ANSWER',
  INCOMPLETE_ANSWER = 'INCOMPLETE_ANSWER',
  HALLUCINATION = 'HALLUCINATION',
  INCOMPLETE_SOURCES = 'INCOMPLETE_SOURCES',
  OBSOLETE_SOURCES = 'OBSOLETE_SOURCES',
  WRONG_ANSWER_FORMAT = 'WRONG_ANSWER_FORMAT',
  BUSINESS_LEXICON_PROBLEM = 'BUSINESS_LEXICON_PROBLEM',
  QUESTION_MISUNDERSTOOD = 'QUESTION_MISUNDERSTOOD',
  OTHER = 'OTHER'
}

export const AnnotationReasons = [
  { label: 'Inaccurate answer', value: AnnotationReason.INACCURATE_ANSWER },
  { label: 'Incomplete answer', value: AnnotationReason.INCOMPLETE_ANSWER },
  { label: 'Hallucination', value: AnnotationReason.HALLUCINATION },
  { label: 'Incomplete sources / documents', value: AnnotationReason.INCOMPLETE_SOURCES },
  { label: 'Obsolete sources / documents', value: AnnotationReason.OBSOLETE_SOURCES },
  { label: 'Wrong answer format', value: AnnotationReason.WRONG_ANSWER_FORMAT },
  { label: 'Business lexicon problem', value: AnnotationReason.BUSINESS_LEXICON_PROBLEM },
  { label: 'Question not/misunderstood', value: AnnotationReason.QUESTION_MISUNDERSTOOD },
  { label: 'Other', value: AnnotationReason.OTHER }
];
