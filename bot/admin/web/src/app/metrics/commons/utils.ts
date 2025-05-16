/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
