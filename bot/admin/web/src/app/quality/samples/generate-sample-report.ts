import * as pdfMake from 'pdfmake/build/pdfmake';
import * as pdfFonts from 'pdfmake/build/vfs_fonts';
import { EvaluationSampleDataDefinition, EvaluationSampleDefinition, EvaluationStatus } from './models';
import { getEvaluationRate, getSampleCoverage } from './utils';
import { ActionReport, Sentence } from '../../shared/model/dialog-data';
import { ResponseIssueReasons } from '../../shared/model/response-issue';
import { getExportFileName } from '../../shared/utils';

(<any>pdfMake).addVirtualFileSystem(pdfFonts);

const color_blue = '#3366ff';
const color_lightBlue = '#99b3ff';
const color_green = '#00b377';
const color_orange = '#ffaa00';
const color_paleBlue = '#edf1f7';

export async function generateSampleReport(
  namespace: string,
  botName: string,
  datePipe,
  sample: EvaluationSampleDefinition,
  data: EvaluationSampleDataDefinition
) {
  const dateFormat = 'y/MM/dd HH:mm';

  const sampleName = sample.name;
  const sampleStart = datePipe.transform(sample.dialogActivityFrom, dateFormat);
  const sampleEnd = datePipe.transform(sample.dialogActivityTo, dateFormat);

  const sampleCreation = `${sample.createdBy} on ${datePipe.transform(sample.creationDate, dateFormat)}`;
  const sampleValidation = `${sample.statusChangedBy} on ${datePipe.transform(sample.statusChangeDate, dateFormat)}`;
  const includeTests = sample.allowTestDialogs ? 'Yes' : 'No';
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
                text: 'Tock Bot Dialogue Sample Evaluation Report',
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
                text: [{ text: 'Tock Namespace: ', bold: true }, namespace]
              },
              {
                text: [{ text: 'Tock Bot Name: ', bold: true }, botName]
              }
            ],
            [
              {
                text: [{ text: 'Sample Name: ', bold: true }, sampleName],
                colSpan: 2
              },

              ''
            ],
            [
              {
                text: [{ text: 'Created by: ', bold: true }, sampleCreation]
              },
              {
                text: [{ text: 'Validated by: ', bold: true }, sampleValidation]
              }
            ],
            [
              {
                text: [{ text: 'Coverage Period From: ', bold: true }, sampleStart]
              },
              {
                text: [{ text: 'Coverage Period To: ', bold: true }, sampleEnd]
              }
            ],
            [
              {
                text: [{ text: 'Number of Requested Dialogues: ', bold: true }, requestedDialogCount]
              },
              {
                text: [
                  { text: 'Number of Actual Dialogues: ', bold: true },
                  { text: dialogsCount, color: color_blue }
                ]
              }
            ],
            [
              {
                text: [{ text: 'Total dialogues recorded for the period: ', bold: true }, totalDialogCount]
              },
              {
                text: [
                  { text: 'Sample Coverage Rate: ', bold: true },
                  { text: sampleCoverage, color: color_blue }
                ]
              }
            ],
            [
              {
                text: [{ text: 'May Include Test Dialogues: ', bold: true }, includeTests]
              },

              ''
            ],
            [
              {
                text: [{ text: 'Sample Description: ', bold: true }, description]
              },

              {
                text: [{ text: 'Validation Comment: ', bold: true }, validationComment]
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
              { text: 'Answers Evaluated', bold: true, color: color_blue, fillColor: color_paleBlue, margin: [lr, tb, lr, 0] },
              { text: botActionCount, color: color_blue, alignment: 'right', fillColor: color_paleBlue, margin: [5, tb, 5, 0] },
              { text: '', alignment: 'right', fillColor: color_paleBlue, margin: [5, tb, lr, 0] }
            ],
            [
              { text: 'Positive Evaluations', color: color_green, bold: true, fillColor: color_paleBlue, margin: [lr, 0, lr, 0] },
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
              { text: 'Negative Evaluations', color: color_orange, bold: true, fillColor: color_paleBlue, margin: [lr, 0, lr, tb] },
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
          body: getDetailedRows(datePipe, dateFormat, data),
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

function getDetailedRows(datePipe, dateFormat: string, data: EvaluationSampleDataDefinition) {
  const result = [
    [
      { text: 'Dialog ID', style: 'tableHeader' },
      { text: 'Date', style: 'tableHeader' },
      { text: 'Question / Answer', style: 'tableHeader' },
      { text: 'Evaluation', style: 'tableHeader' },
      { text: 'Reason', style: 'tableHeader' },
      { text: 'Evaluator', style: 'tableHeader' }
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
    let questionText = 'NA';
    let actionText = 'NA';
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
              actionText = '[Attachment message]';
            }
            if (sentence.messages[0].choices.length) {
              actionText = '[Choice message]';
            }
            if (sentence.messages[0].locations.length) {
              actionText = '[Location message]';
            }
          }
        }
      }
    }

    const evaluationStatus = evaluation.status === EvaluationStatus.UP ? 'Good' : 'Bad';
    const evaluationColor = evaluation.status === EvaluationStatus.UP ? color_green : color_orange;
    const reason = ResponseIssueReasons.find((r) => r.value === evaluation.reason)?.label || '';
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
