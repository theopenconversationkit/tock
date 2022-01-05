
export interface ValidUtteranceResult {
  cancelled: false;
  value: string;
}

interface CancelledUtteranceResult {
  cancelled: true;
}

export type EditUtteranceResult = ValidUtteranceResult | CancelledUtteranceResult;


export const notCancelled = (item: EditUtteranceResult) => !item.cancelled;


