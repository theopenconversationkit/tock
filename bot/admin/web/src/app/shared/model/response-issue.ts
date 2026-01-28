export enum ResponseIssueReason {
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

export const ResponseIssueReasons = [
  { label: 'Question not/misunderstood', value: ResponseIssueReason.QUESTION_MISUNDERSTOOD },
  { label: 'Inaccurate answer', value: ResponseIssueReason.INACCURATE_ANSWER },
  { label: 'Incomplete answer', value: ResponseIssueReason.INCOMPLETE_ANSWER },
  { label: 'Incomplete sources / documents', value: ResponseIssueReason.INCOMPLETE_SOURCES },
  { label: 'Obsolete sources / documents', value: ResponseIssueReason.OBSOLETE_SOURCES },
  { label: 'Business lexicon problem', value: ResponseIssueReason.BUSINESS_LEXICON_PROBLEM },
  { label: 'Wrong answer format', value: ResponseIssueReason.WRONG_ANSWER_FORMAT },
  { label: 'Hallucination', value: ResponseIssueReason.HALLUCINATION },
  { label: 'Other', value: ResponseIssueReason.OTHER }
];
