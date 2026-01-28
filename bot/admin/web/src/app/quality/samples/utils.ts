import { EvaluationSampleDefinition } from './models';

export function getEvaluationBaseUrl(applicationName: string): string {
  return `/bots/${applicationName}/evaluation-samples/`;
}

export function getSampleCoverage(sample: EvaluationSampleDefinition): number {
  if (sample && sample.totalDialogCount) {
    const coverage = (sample.dialogsCount / sample.totalDialogCount) * 100;
    return parseFloat(coverage.toFixed(2));
  }

  return 0;
}

export function getEvaluationRate(sample: EvaluationSampleDefinition, type: 'positive' | 'negative'): number {
  if (sample) {
    const { positiveCount, negativeCount } = sample.evaluationsResult;
    const count = type === 'positive' ? positiveCount : negativeCount;

    const rate = (count / sample.botActionCount) * 100;

    return Math.round(rate);
  }

  return 0;
}
