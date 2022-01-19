/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import { SentenceStatus } from "src/app/model/nlp";
import {FaqDefinition} from "../model/faq-definition";

/**
 * Some title that could be picked for mock
 */
export const MOCK_QA_TITLES: string[] = [
  'FAQ Abonnement',
  'FAQ Activation carte',
  'FAQ Activation Paylib',
  'FAQ Adresse RIB Différente',
];

export const MOCK_QA_DEFAULT_LABEL = 'Question quel est le meilleur moyen pour supprimer';
export const MOCK_QA_DEFAULT_DESCRIPTION = 'A paragraph of text with an unassigned link';

/**
 * Templates for FAQ Definitions
 */
export const MOCK_FREQUENT_QUESTIONS: Partial<FaqDefinition>[] = [
  {
    title: 'Comment accéder au CMB?',
    status: 'Draft',
    enabled: true,
    answer: 'Une fois la carte max reçue et activée, vous avez accès à votre RIB max via le menu ou via « finances » puis « mon compte max ».\n' +
      'Il s\'agit d\'un RIB français.',
    utterances: [
      'Où puis-je consulter mon RIB ?'
    ],
    tags: ['RIB', 'Max']
  },
  {
    title: 'Adresse RIB différente',
    status: 'Draft',
    enabled: true,
    answer: 'L\'adresse mentionnée sur le RIB correspond à celle du siège social de Nouvelle Vague.',
    utterances: [
      'L\'adresse de mon rib n\'est pas la bonne'
    ],
    tags: ['RIB']
  },
  {
    title: 'Process virement MAX',
    status: 'Draft',
    enabled: true,
    answer: '"Vous pouvez effectuer et recevoir gratuitement des virements SEPA (Single Euro Payments Area). Le compte max autorise uniquement les virements en euros et exclut les virements internationaux.\n' +
      '\n' +
      '- Pour faire un virement sur votre compte max : il vous suffit d\'ajouter le RIB max en bénéficiaire sur votre banque externe. Le RIB de votre compte max est disponible une fois la carte max reçue et activée via le menu ou via « finances » puis « mon compte max ». \n' +
      '\n' +
      '- Pour faire un virement de votre compte max vers un autre compte : il vous suffit de vous rendre dans ""envoyer de l\'argent"" puis d\'ajouter le RIB du compte destinataire en bénéficiaire, en choisissant bien ""mon compte max"" dans ""établissement associé"". Vous pouvez ensuite faire votre virement.\n' +
      '\n' +
      'https://www.aumaxpourmoi.fr/faq/"',
    utterances: [
      'Où est ce que je peux faire un virement entrant?'
    ],
    tags: ['RIB', 'Sepa', 'Max']
  },
  {
    title: 'Délai virement compte Max',
    status: 'Draft',
    enabled: true,
    answer: '"Nous sommes compatibles avec les virements instantanés en France, à condition que l\'autre banque le soit aussi. Sinon, le délai d\'un virement interbancaire est de 72 heures ouvrées maximum (hors week-end et jours fériés). \n' +
      '\n' +
      '"',
    utterances: [
      'Combien de temps un virement met-il pour être crédité sur ma carte ?'
    ],
    tags: ['Virement']
  },
  {
    title: 'Delais reception carte',
    status: 'Draft',
    enabled: true,
    answer: '"Après commande de votre carte, le délai de réception en courrier simple est de 4 jours en moyenne. Pour une carte premium ou métal, le délai peut être rallongé de quelques jours.\n' +
      'Dès réception de votre première carte max, vous pourrez directement l\'activer sur l\'appli dans ""carte max"" ou bien sur le tchat s\'il s\'agissait d\'une opposition. \n' +
      '"',
    utterances: [
      'Quel est le délai de récéption de la carte une fois le contrat signé ?'
    ],
    tags: ['Max']
  }
];
