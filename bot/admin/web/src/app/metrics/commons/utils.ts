export function heuristicValueColorDetection(value: string): string {
  const no_answer = [
    'no answer' // Rag indicator
  ];
  const positive = [
    'success', // Rag indicator
    'true',
    'yes',
    'vrai',
    'real',
    'oui',
    'si',
    'да',
    'ja',
    '是的',
    'satisfied',
    'satisfait',
    'positif',
    'positive',
    'good',
    'bon',
    'satisfecho',
    'positivo',
    'bien',
    'correct',
    'correcto',
    'comprendido',
    'compris',
    'understood',
    'richtig',
    'verstanden',
    'exacte',
    'exact',
    'exacto'
  ];
  const negative = [
    'failure', // Rag indicator
    'false',
    'no',
    'faux',
    'falso',
    'non',
    'нет',
    'nein',
    '不',
    'not satisfied',
    'non satisfait',
    'négatif',
    'negative',
    'bad',
    'mauvais',
    'no satisfecho',
    'malo',
    'incorrect',
    'incorrecto',
    'misunderstood',
    'non compris',
    'mal compris',
    'incomprendido',
    'falsch',
    'falsch verstanden',
    'inexacte',
    'inaccurate'
  ];
  if (no_answer.includes(value.toLowerCase())) return '#ffaa00';
  if (positive.includes(value.toLowerCase())) return '#91cc75';
  if (negative.includes(value.toLowerCase())) return '#ff3d71';
  return undefined;
}
