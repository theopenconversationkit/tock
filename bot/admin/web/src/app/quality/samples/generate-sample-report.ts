import * as pdfMake from 'pdfmake/build/pdfmake';
import * as pdfFonts from 'pdfmake/build/vfs_fonts';
import { TranslocoService } from '@jsverse/transloco';
import { EvaluationSampleDataDefinition, EvaluationSampleDefinition, EvaluationStatus } from './models';
import { getEvaluationRate, getSampleCoverage } from './utils';
import { ActionReport, Sentence } from '../../shared/model/dialog-data';
import { ResponseIssueReason, ResponseIssueReasons } from '../../shared/model/response-issue';
import { getExportFileName } from '../../shared/utils';

(<any>pdfMake).addVirtualFileSystem(pdfFonts);

const color_blue = '#3366ff';
const color_lightBlue = '#99b3ff';
const color_green = '#00b377';
const color_orange = '#ffaa00';
const color_paleBlue = '#edf1f7';

// Scoped translate helper: tr('report_title') => 'quality.sample-report.report_title'
type TranslateFn = (key: string, params?: object) => string;

export async function generateSampleReport(
  namespace: string,
  botName: string,
  datePipe,
  sample: EvaluationSampleDefinition,
  data: EvaluationSampleDataDefinition,
  transloco: TranslocoService
) {
  const tr: TranslateFn = (key, params) => transloco.translate(`quality.sample-report.${key}`, params);

  const reasonKeyByValue: Record<ResponseIssueReason, string> = {
    [ResponseIssueReason.QUESTION_MISUNDERSTOOD]: 'questionNotOrMisunderstood',
    [ResponseIssueReason.INACCURATE_ANSWER]: 'inaccurateAnswer',
    [ResponseIssueReason.INCOMPLETE_ANSWER]: 'incompleteAnswer',
    [ResponseIssueReason.INCOMPLETE_SOURCES]: 'incompleteSourcesOrDocuments',
    [ResponseIssueReason.OBSOLETE_SOURCES]: 'obsoleteSourcesOrDocuments',
    [ResponseIssueReason.BUSINESS_LEXICON_PROBLEM]: 'businessLexiconProblem',
    [ResponseIssueReason.WRONG_ANSWER_FORMAT]: 'wrongAnswerFormat',
    [ResponseIssueReason.HALLUCINATION]: 'hallucination',
    [ResponseIssueReason.OTHER]: 'other'
  };

  const getReasonLabel = (reason: ResponseIssueReason): string =>
    reason ? transloco.translate(`common.responseIssueReasons.${reasonKeyByValue[reason]}`) : '';

  const dateFormat = 'y/MM/dd HH:mm';

  const sampleName = sample.name;
  const sampleStart = datePipe.transform(sample.dialogActivityFrom, dateFormat);
  const sampleEnd = datePipe.transform(sample.dialogActivityTo, dateFormat);

  const sampleCreation = tr('by_on', {
    by: sample.createdBy,
    date: datePipe.transform(sample.creationDate, dateFormat)
  });
  const sampleValidation = tr('by_on', {
    by: sample.statusChangedBy,
    date: datePipe.transform(sample.statusChangeDate, dateFormat)
  });
  const includeTests = sample.allowTestDialogs ? tr('yes') : tr('no');
  const requestedDialogCount = sample.requestedDialogCount;
  const dialogsCount = sample.dialogsCount;
  const totalDialogCount = sample.totalDialogCount;
  const sampleCoverage = `${getSampleCoverage(sample)}%`;
  const botActionCount = sample.botActionCount;
  const description = sample.description || '-';
  const validationComment = sample.statusComment || '-';

  const positiveVal = `${sample.evaluationsResult.positiveCount}`;
  const positiveScore = `${getEvaluationRate(sample, 'positive')}%`;
  const negativeVal = `${sample.evaluationsResult.negativeCount}`;
  const negativeScore = `${getEvaluationRate(sample, 'negative')}%`;

  const lr = 15;
  const tb = 10;

  const docDefinition = {
    pageOrientation: 'landscape',
    content: [
      {
        layout: 'noBorders',
        table: {
          widths: ['*', 'auto'],
          body: [
            [
              {
                text: tr('report_title'),
                style: 'header',
                color: 'white',
                margin: [15, 5, 15, 5],
                fillColor: color_blue
              },
              {
                text: namespace,
                style: 'header',
                color: 'white',
                margin: [15, 5, 15, 5],
                fillColor: color_lightBlue
              }
            ]
          ]
        },

        margin: [0, 0, 0, 15]
      },

      // Header Info Section
      {
        layout: 'noBorders',
        table: {
          widths: ['50%', '50%'],
          body: [
            [
              {
                text: [{ text: tr('namespace') + ': ', bold: true }, namespace]
              },
              {
                text: [{ text: tr('bot_name') + ': ', bold: true }, botName]
              }
            ],
            [
              {
                text: [{ text: tr('sample_name') + ': ', bold: true }, sampleName],
                colSpan: 2
              },

              ''
            ],
            [
              {
                text: [{ text: tr('created_by') + ': ', bold: true }, sampleCreation]
              },
              {
                text: [{ text: tr('validated_by') + ': ', bold: true }, sampleValidation]
              }
            ],
            [
              {
                text: [{ text: tr('coverage_from') + ': ', bold: true }, sampleStart]
              },
              {
                text: [{ text: tr('coverage_to') + ': ', bold: true }, sampleEnd]
              }
            ],
            [
              {
                text: [{ text: tr('requested_dialogues') + ': ', bold: true }, requestedDialogCount]
              },
              {
                text: [
                  { text: tr('actual_dialogues') + ': ', bold: true },
                  { text: dialogsCount, color: color_blue }
                ]
              }
            ],
            [
              {
                text: [{ text: tr('total_dialogues') + ': ', bold: true }, totalDialogCount]
              },
              {
                text: [
                  { text: tr('coverage_rate') + ': ', bold: true },
                  { text: sampleCoverage, color: color_blue }
                ]
              }
            ],
            [
              {
                text: [{ text: tr('include_tests') + ': ', bold: true }, includeTests]
              },

              ''
            ],
            [
              {
                text: [{ text: tr('description') + ': ', bold: true }, description]
              },

              {
                text: [{ text: tr('validation_comment') + ': ', bold: true }, validationComment]
              }
            ]
          ]
        },

        margin: [0, 0, 0, 15]
      },

      // Scores
      {
        layout: 'noBorders',
        margin: [0, 0, 0, 15],
        style: 'score',
        table: {
          widths: ['*', 'auto', 'auto'],
          body: [
            [
              { text: tr('answers_evaluated'), bold: true, color: color_blue, fillColor: color_paleBlue, margin: [lr, tb, lr, 0] },
              { text: botActionCount, color: color_blue, alignment: 'right', fillColor: color_paleBlue, margin: [5, tb, 5, 0] },
              { text: '', alignment: 'right', fillColor: color_paleBlue, margin: [5, tb, lr, 0] }
            ],
            [
              { text: tr('positive_evaluations'), color: color_green, bold: true, fillColor: color_paleBlue, margin: [lr, 0, lr, 0] },
              { text: positiveVal, color: color_green, alignment: 'right', fillColor: color_paleBlue, margin: [5, 0, 5, 0] },
              {
                text: positiveScore,
                color: color_green,
                bold: true,
                alignment: 'right',
                fillColor: color_paleBlue,
                margin: [5, 0, lr, 0]
              }
            ],
            [
              { text: tr('negative_evaluations'), color: color_orange, bold: true, fillColor: color_paleBlue, margin: [lr, 0, lr, tb] },
              { text: negativeVal, color: color_orange, alignment: 'right', fillColor: color_paleBlue, margin: [5, 0, 5, tb] },
              {
                text: negativeScore,
                color: color_orange,
                bold: true,
                alignment: 'right',
                fillColor: color_paleBlue,
                margin: [5, 0, lr, tb]
              }
            ]
          ]
        }
      },

      // Detailed Evaluation Table
      {
        layout: 'lightHorizontalLines',
        table: {
          headerRows: 1,
          widths: ['auto', 'auto', '*', 'auto', 'auto', 'auto'],
          body: getDetailedRows(datePipe, dateFormat, data, tr, getReasonLabel),
          dontBreakRows: true
        }
      }
    ],

    defaultStyle: {
      fontSize: 10
    },

    styles: {
      header: {
        fontSize: 18,
        bold: true
      },
      score: {
        fontSize: 14
      },
      tableHeader: {
        bold: true,
        fontSize: 8
      },
      question: {
        bold: true
      },
      answer: {
        italic: true
      }
    }
  };

  const pdfDoc = await pdfMake.createPdf(docDefinition);

  // pdfDoc.open();

  const exportFileName = getExportFileName(namespace, botName, 'Evaluation-Report', 'pdf', sample.name);

  pdfDoc.download(exportFileName);
}

function getDetailedRows(
  datePipe,
  dateFormat: string,
  data: EvaluationSampleDataDefinition,
  tr: TranslateFn,
  getReasonLabel: (reason: ResponseIssueReason) => string
) {
  const result = [
    [
      { text: tr('col_dialog_id'), style: 'tableHeader' },
      { text: tr('col_date'), style: 'tableHeader' },
      { text: tr('col_question_answer'), style: 'tableHeader' },
      { text: tr('col_evaluation'), style: 'tableHeader' },
      { text: tr('col_reason'), style: 'tableHeader' },
      { text: tr('col_evaluator'), style: 'tableHeader' }
    ]
  ];

  function truncateString(str: string, maxLength: number): string {
    const nStr = str.replace(/\n{2,}/g, '\n');
    return nStr.length > maxLength ? nStr.substring(0, maxLength) + '...' : nStr;
  }

  function getActionQuestion(action: ActionReport, actionsStack: ActionReport[]): string {
    let question = '-';

    const actionIndex = actionsStack.findIndex((act) => act === action);
    if (actionIndex > 0) {
      let questionAction = actionsStack[actionIndex - 1];

      if (questionAction.message.isDebug()) {
        questionAction = actionsStack[actionIndex - 2];
      }

      if (!questionAction.isBot()) {
        const questionSentence = questionAction.message as unknown as Sentence;
        question = questionSentence.text;
      }
    }

    return question;
  }

  data.evaluations.forEach((evaluation) => {
    let actionDate = '';
    let questionText = tr('not_available');
    let actionText = tr('not_available');
    const dialog = data.dialogs.find((d) => d.id === evaluation.dialogId);
    if (dialog) {
      const action = dialog.actions.find((a) => a.id === evaluation.actionId);
      if (action) {
        actionDate = datePipe.transform(action.date, dateFormat);

        questionText = truncateString(getActionQuestion(action, dialog.actions), 140);

        if (action.message?.isSentence() || action.message?.isSentenceWithFootnotes()) {
          const sentence = action.message as unknown as Sentence;
          if (sentence.text) {
            actionText = truncateString(sentence.text, 140);
          } else {
            if (sentence.messages[0].attachments.length) {
              actionText = tr('attachment_message');
            }
            if (sentence.messages[0].choices.length) {
              actionText = tr('choice_message');
            }
            if (sentence.messages[0].locations.length) {
              actionText = tr('location_message');
            }
          }
        }
      }
    }

    const evaluationStatus = evaluation.status === EvaluationStatus.UP ? tr('eval_good') : tr('eval_bad');
    const evaluationColor = evaluation.status === EvaluationStatus.UP ? color_green : color_orange;
    const reason = getReasonLabel(evaluation.reason as ResponseIssueReason);
    const evaluatedBy = evaluation.evaluator?.id;

    const margin = [0, 10, 0, 10];
    result.push([
      { text: evaluation.dialogId, margin: margin },
      { text: actionDate, margin: margin },
      {
        stack: [
          { text: questionText, style: 'question' },
          { text: actionText, style: 'answer' }
        ],
        margin: margin
      },
      { text: evaluationStatus, bold: true, color: evaluationColor, margin: margin },
      { text: reason, margin: margin },
      { text: evaluatedBy, margin: margin }
    ] as any);
  });

  return result;
}
