import * as Papa from 'papaparse';

export interface DatasetCsvQuestion {
  question: string;
  groundTruth: string;
}

export const DATASET_CSV_HEADERS = ['question', 'groundTruth'] as const;

const BOM = '\uFEFF';
const EXPORT_DELIMITER = ';';

export type CsvParseErrorCode = 'EMPTY_FILE' | 'MISSING_QUESTION_COLUMN' | 'NO_VALID_ROW';

export class CsvParseError extends Error {
  constructor(public readonly code: CsvParseErrorCode) {
    super(code);
    // Required for `instanceof` to work when targeting ES5 downlevel output
    Object.setPrototypeOf(this, CsvParseError.prototype);
  }
}

/** Prevents CSV injection when the exported file is reopened in a spreadsheet. */
function sanitizeCsvField(value: string): string {
  return /^[=+\-@\t\r]/.test(value) ? `'${value}` : value;
}

export function questionsToCsv(questions: Partial<DatasetCsvQuestion>[], delimiter: string = EXPORT_DELIMITER): string {
  const csv = Papa.unparse(
    {
      fields: [...DATASET_CSV_HEADERS],
      data: questions.map((q) => [sanitizeCsvField(q.question ?? ''), sanitizeCsvField(q.groundTruth ?? '')])
    },
    { delimiter, newline: '\r\n', quotes: true }
  );

  return BOM + csv;
}

export function buildCsvTemplate(delimiter: string = EXPORT_DELIMITER): string {
  return questionsToCsv([], delimiter);
}

export function csvToQuestions(content: string): DatasetCsvQuestion[] {
  const result = Papa.parse<Record<string, string>>(content.replace(/^\uFEFF/, ''), {
    header: true,
    skipEmptyLines: 'greedy',
    delimiter: '', // auto-detection (; , \t |) handled by Papa
    transformHeader: (h) =>
      h
        .trim()
        .toLowerCase()
        .replace(/[\s_-]/g, '')
  });

  if (!result.data.length && !result.meta.fields?.length) throw new CsvParseError('EMPTY_FILE');
  if (!result.meta.fields?.includes('question')) throw new CsvParseError('MISSING_QUESTION_COLUMN');

  const hasGroundTruth = result.meta.fields.includes('groundtruth');

  const questions = result.data
    .map((row) => ({
      question: (row['question'] ?? '').trim(),
      groundTruth: hasGroundTruth ? (row['groundtruth'] ?? '').trim() : ''
    }))
    .filter((q) => q.question.length);

  if (!questions.length) throw new CsvParseError('NO_VALID_ROW');

  return questions;
}
