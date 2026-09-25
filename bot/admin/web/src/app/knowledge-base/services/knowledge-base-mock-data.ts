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

/**
 * MOCK ONLY — seed data and demo scenarios.
 * This whole file disappears once the backend exists.
 */

import { KnowledgeBaseEntry, KnowledgeBaseEntryStatus, KnowledgeBaseProjectionState } from '../models';

/** Demo scenarios, switchable from the board while the feature is being discussed. */
export enum KnowledgeBaseMockScenario {
  /** No index session on the bot: standalone mode entry point */
  NO_INDEX = 'NO_INDEX',
  /** Index created and fed by Tock, everything up to date */
  IN_SYNC = 'IN_SYNC',
  /** Index created and fed by Tock, pending and orphan entries */
  OUT_OF_SYNC = 'OUT_OF_SYNC',
  /** Index produced by a third party pipeline, embedding model unknown */
  EXTERNAL_INDEX = 'EXTERNAL_INDEX',
  /** Empty knowledge base */
  NO_ENTRIES = 'NO_ENTRIES'
}

export const MOCK_INDEX_SESSION_ID = '0e1c7a54-8b1d-4c22-9f3a-2f6b7c9d4e10';
export const MOCK_EXTERNAL_INDEX_SESSION_ID = '7bd3f180-44ac-4d9e-8a71-15c0b2e6f933';
export const MOCK_EMBEDDING_MODEL = 'text-embedding-3-large';

/**
 * Physical index names, as the server would return them from the indexes endpoint.
 * Normalization differs per provider — PGVector replaces every character outside
 * [a-z0-9_] with '_', OpenSearch keeps hyphens — so the studio never builds them.
 * These stand for a PGVector store.
 */
export const MOCK_INDEX_NAME = 'ns_acme_bot_assistant_session_0e1c7a54_8b1d_4c22_9f3a_2f6b7c9d4e10';
export const MOCK_EXTERNAL_INDEX_NAME = 'ns_acme_bot_assistant_session_7bd3f180_44ac_4d9e_8a71_15c0b2e6f933';

const NOW = new Date();

function daysAgo(days: number, hours: number = 0): string {
  const date = new Date(NOW);
  date.setDate(date.getDate() - days);
  date.setHours(date.getHours() - hours);
  return date.toISOString();
}

interface SeedEntry {
  title: string;
  searchHints: string[];
  content: string;
  sourceUrl: string | null;
  tags: string[];
  status: KnowledgeBaseEntryStatus;
  projectionState: KnowledgeBaseProjectionState;
  createdBy: string;
  updatedBy: string | null;
  createdDaysAgo: number;
  updatedDaysAgo: number | null;
}

const SEED: SeedEntry[] = [
  {
    title: 'Quel est le plafond de retrait de ma carte bancaire ?',
    searchHints: ['Combien puis-je retirer par semaine ?', 'Je ne peux plus retirer au distributeur, pourquoi ?'],
    content:
      "Le plafond de retrait dépend de la gamme de la carte. Il est consultable à tout moment depuis l'application, rubrique Mes cartes, puis Plafonds. Une modification temporaire peut être demandée en ligne pour une durée maximale de 30 jours ; au-delà, elle nécessite un accord du conseiller.",
    sourceUrl: 'https://intranet.example.com/cartes/plafonds',
    tags: ['cartes', 'quotidien'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.INDEXED,
    createdBy: 'marie.l',
    updatedBy: 'marie.l',
    createdDaysAgo: 41,
    updatedDaysAgo: 12
  },
  {
    title: 'Comment faire opposition sur ma carte ?',
    searchHints: ["J'ai perdu ma carte bancaire", 'Ma carte a été volée, que faire ?', 'Bloquer ma carte'],
    content:
      "L'opposition peut être posée immédiatement depuis l'application, rubrique Mes cartes, bouton Bloquer ma carte. Elle est également possible 24h/24 par téléphone au numéro d'opposition figurant au dos des relevés. Le blocage est immédiat et irréversible : une nouvelle carte est commandée automatiquement et livrée sous 5 jours ouvrés.",
    sourceUrl: 'https://intranet.example.com/cartes/opposition',
    tags: ['cartes', 'urgence'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.INDEXED,
    createdBy: 'marie.l',
    updatedBy: null,
    createdDaysAgo: 41,
    updatedDaysAgo: null
  },
  {
    title: 'En combien de temps un virement SEPA est-il reçu ?',
    searchHints: ['Mon virement met combien de temps ?', 'Quand mon bénéficiaire recevra-t-il le virement ?'],
    content:
      "Un virement SEPA classique émis avant 16h un jour ouvré est crédité sur le compte du bénéficiaire le jour ouvré suivant. Émis après 16h, un week-end ou un jour férié, il est traité le jour ouvré suivant. Le virement instantané, lorsqu'il est disponible pour le bénéficiaire, est crédité en moins de 10 secondes, 7j/7.",
    sourceUrl: null,
    tags: ['virements', 'quotidien'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.INDEXED,
    createdBy: 'thomas.b',
    updatedBy: 'thomas.b',
    createdDaysAgo: 30,
    updatedDaysAgo: 3
  },
  {
    title: 'Comment ajouter un nouveau bénéficiaire ?',
    searchHints: ['Enregistrer un IBAN', "Je veux virer de l'argent à quelqu'un de nouveau"],
    content:
      "L'ajout se fait depuis l'application, rubrique Virements, puis Bénéficiaires, bouton Ajouter. Une validation par l'application sécurisée est demandée. Le bénéficiaire est utilisable immédiatement après validation, sans délai d'attente.",
    sourceUrl: 'https://intranet.example.com/virements/beneficiaires',
    tags: ['virements'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.PENDING,
    createdBy: 'thomas.b',
    updatedBy: 'thomas.b',
    createdDaysAgo: 28,
    updatedDaysAgo: 0
  },
  {
    title: 'Quels sont les frais de tenue de compte ?',
    searchHints: ['Combien coûte mon compte par mois ?'],
    content:
      'Les frais de tenue de compte sont détaillés dans la brochure tarifaire en vigueur, disponible en agence et en ligne. Ils sont prélevés mensuellement et apparaissent sur le relevé sous le libellé Frais de tenue de compte. Certaines offres groupées de services les incluent.',
    sourceUrl: 'https://intranet.example.com/tarifs',
    tags: ['tarifs'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.PENDING,
    createdBy: 'marie.l',
    updatedBy: 'sophie.d',
    createdDaysAgo: 22,
    updatedDaysAgo: 1
  },
  {
    title: 'Comment ouvrir un livret A ?',
    searchHints: ['Je veux ouvrir un livret', 'Souscrire un livret A en ligne'],
    content:
      "L'ouverture se fait en ligne depuis l'espace client, rubrique Épargne, ou en agence. Un seul livret A par personne est autorisé, tous établissements confondus. Le versement initial minimum est de 10 euros.",
    sourceUrl: null,
    tags: ['épargne'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.INDEXED,
    createdBy: 'sophie.d',
    updatedBy: null,
    createdDaysAgo: 19,
    updatedDaysAgo: null
  },
  {
    title: 'Comment contacter mon conseiller ?',
    searchHints: ["Je veux parler à quelqu'un", 'Prendre rendez-vous avec mon conseiller'],
    content:
      "La messagerie sécurisée de l'espace client permet d'écrire directement au conseiller, avec une réponse sous 48h ouvrées. La prise de rendez-vous, en agence ou par téléphone, est disponible dans la rubrique Mon agence. Les coordonnées directes du conseiller figurent également dans cette rubrique.",
    sourceUrl: 'https://intranet.example.com/contact',
    tags: ['relation client'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.INDEXED,
    createdBy: 'sophie.d',
    updatedBy: 'sophie.d',
    createdDaysAgo: 17,
    updatedDaysAgo: 9
  },
  {
    title: "Comment contester un prélèvement que je n'ai pas autorisé ?",
    searchHints: ['Un prélèvement inconnu sur mon compte', 'Se faire rembourser un prélèvement'],
    content:
      "Un prélèvement SEPA autorisé peut être contesté sans motif dans les 8 semaines suivant le débit, directement depuis l'application, rubrique Opérations. En l'absence de mandat, le délai de contestation est porté à 13 mois. Le remboursement intervient sous 10 jours ouvrés après acceptation de la demande.",
    sourceUrl: 'https://intranet.example.com/prelevements/contestation',
    tags: ['prélèvements', 'litiges'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.INDEXED,
    createdBy: 'thomas.b',
    updatedBy: null,
    createdDaysAgo: 15,
    updatedDaysAgo: null
  },
  {
    title: 'Comment demander une autorisation de découvert ?',
    searchHints: ['Augmenter mon découvert', 'Je suis à découvert, que faire ?'],
    content:
      "La demande d'autorisation de découvert, ou de modification du montant autorisé, se fait auprès du conseiller. Elle donne lieu à une étude préalable. Un découvert non autorisé génère des frais et doit être régularisé au plus vite.",
    sourceUrl: null,
    tags: ['crédit'],
    status: KnowledgeBaseEntryStatus.DRAFT,
    projectionState: KnowledgeBaseProjectionState.NONE,
    createdBy: 'sophie.d',
    updatedBy: 'sophie.d',
    createdDaysAgo: 9,
    updatedDaysAgo: 2
  },
  {
    title: 'Quels justificatifs de domicile sont acceptés ?',
    searchHints: ['Quel document pour prouver mon adresse ?'],
    content:
      "Sont acceptées les factures d'électricité, de gaz, d'eau ou de téléphone fixe de moins de 3 mois, ainsi que la dernière quittance de loyer ou le dernier avis d'imposition. Une attestation d'hébergement accompagnée de la pièce d'identité de l'hébergeant est acceptée pour les personnes hébergées.",
    sourceUrl: 'https://intranet.example.com/kyc/justificatifs',
    tags: ['dossier client'],
    status: KnowledgeBaseEntryStatus.DRAFT,
    projectionState: KnowledgeBaseProjectionState.NONE,
    createdBy: 'marie.l',
    updatedBy: null,
    createdDaysAgo: 6,
    updatedDaysAgo: null
  },
  {
    title: "Quelle est l'offre en cours pour les 18-25 ans ?",
    searchHints: ['Offre jeune', 'Promotion ouverture de compte étudiant'],
    content:
      "L'offre de bienvenue jeunes actifs a pris fin le 30 juin. Aucune offre promotionnelle n'est en cours actuellement pour cette tranche d'âge. Les conditions tarifaires réduites pour les moins de 25 ans restent applicables.",
    sourceUrl: null,
    tags: ['offres'],
    status: KnowledgeBaseEntryStatus.DRAFT,
    projectionState: KnowledgeBaseProjectionState.ORPHAN,
    createdBy: 'sophie.d',
    updatedBy: 'sophie.d',
    createdDaysAgo: 60,
    updatedDaysAgo: 4
  },
  {
    title: 'Quel est le plafond du livret A ?',
    searchHints: ['Combien puis-je mettre au maximum sur mon livret A ?'],
    content:
      'Le plafond de versement du livret A est de 22 950 euros pour un particulier, hors capitalisation des intérêts. Les intérêts annuels peuvent porter le solde au-delà de ce plafond sans que cela pose de difficulté.',
    sourceUrl: null,
    tags: ['épargne'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.INDEXED,
    createdBy: 'marie.l',
    updatedBy: null,
    createdDaysAgo: 14,
    updatedDaysAgo: null
  },
  {
    title: 'Comment transférer mes prélèvements depuis une autre banque ?',
    searchHints: ['Service de mobilité bancaire', 'Changer de banque sans rien faire'],
    content:
      "Le service d'aide à la mobilité bancaire prend en charge le transfert des virements et prélèvements récurrents depuis l'ancienne banque. Il est gratuit et s'active à l'ouverture du compte, sur simple mandat signé. Le transfert est effectif sous 22 jours ouvrés.",
    sourceUrl: 'https://intranet.example.com/mobilite',
    tags: ['dossier client'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.INDEXED,
    createdBy: 'thomas.b',
    updatedBy: null,
    createdDaysAgo: 11,
    updatedDaysAgo: null
  },
  {
    title: 'Ma carte est bloquée après trois codes faux, que faire ?',
    searchHints: ["J'ai tapé trois fois le mauvais code", 'Débloquer ma carte après code erroné'],
    content:
      "Après trois saisies erronées, la carte est bloquée par sécurité. Le déblocage se fait par le conseiller, après vérification d'identité. Le code confidentiel reste inchangé ; en cas d'oubli, une demande de nouveau code doit être faite, avec un délai de réception d'environ 5 jours ouvrés.",
    sourceUrl: null,
    tags: ['cartes', 'urgence'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.PENDING,
    createdBy: 'sophie.d',
    updatedBy: 'sophie.d',
    createdDaysAgo: 8,
    updatedDaysAgo: 0
  },
  {
    title: 'Où trouver mes relevés de compte ?',
    searchHints: ['Télécharger mes relevés', 'Historique de mes relevés bancaires'],
    content:
      "Les relevés sont disponibles au format PDF dans l'espace client, rubrique Mes documents. L'historique couvre les 10 dernières années. Un relevé plus ancien peut être demandé au conseiller, avec des frais de recherche selon la brochure tarifaire.",
    sourceUrl: null,
    tags: ['quotidien'],
    status: KnowledgeBaseEntryStatus.PUBLISHED,
    projectionState: KnowledgeBaseProjectionState.INDEXED,
    createdBy: 'marie.l',
    updatedBy: null,
    createdDaysAgo: 5,
    updatedDaysAgo: null
  }
];

export function buildMockEntries(namespace: string, botId: string): KnowledgeBaseEntry[] {
  return SEED.map((seed, index) => ({
    id: `kb-${(index + 1).toString().padStart(3, '0')}`,
    namespace,
    botIds: [botId],
    title: seed.title,
    searchHints: seed.searchHints,
    content: seed.content,
    sourceUrl: seed.sourceUrl,
    tags: seed.tags,
    status: seed.status,
    contentHash: `h${(index + 1).toString().padStart(3, '0')}`,
    projectionState: seed.projectionState,
    projectedAt:
      seed.projectionState === KnowledgeBaseProjectionState.NONE || seed.projectionState === KnowledgeBaseProjectionState.PENDING
        ? null
        : daysAgo(7),
    projectedIndexSessionId:
      seed.projectionState === KnowledgeBaseProjectionState.NONE || seed.projectionState === KnowledgeBaseProjectionState.PENDING
        ? null
        : MOCK_INDEX_SESSION_ID,
    createdAt: daysAgo(seed.createdDaysAgo, 3),
    createdBy: seed.createdBy,
    updatedAt: seed.updatedDaysAgo === null ? null : daysAgo(seed.updatedDaysAgo, 2),
    updatedBy: seed.updatedBy
  }));
}

/** Documentary chunks used to make the retrieval test result plausible in mixed mode. */
export const MOCK_DOCUMENT_HITS: { title: string; source: string; content: string }[] = [
  {
    title: 'Conditions générales de banque — Chapitre 4',
    source: 'https://intranet.example.com/docs/cgb-2024.pdf',
    content:
      "Article 4.2 — Moyens de paiement. Les conditions d'utilisation des cartes de paiement, les modalités de fixation et de révision des plafonds, ainsi que les cas de suspension du service sont détaillés aux articles suivants. Toute demande de modification est soumise à l'appréciation de l'établissement."
  },
  {
    title: 'Brochure tarifaire — Particuliers',
    source: 'https://intranet.example.com/docs/tarifs-particuliers.pdf',
    content:
      "Extrait standard des tarifs. Les prestations listées sont celles définies par l'arrêté du 5 septembre 2018. Les montants indiqués s'entendent toutes taxes comprises et sont susceptibles de révision annuelle."
  },
  {
    title: "Guide de l'espace client",
    source: 'https://intranet.example.com/docs/guide-espace-client.pdf',
    content:
      "La rubrique Mes cartes regroupe l'ensemble des opérations de gestion courante : consultation des plafonds, blocage temporaire, commande de nouveau code, paramétrage du paiement à l'étranger et du sans contact."
  },
  {
    title: 'Procédure interne — Gestion des oppositions',
    source: 'https://intranet.example.com/docs/proc-opposition.pdf',
    content:
      "La mise en opposition est enregistrée dans le système de gestion des cartes et prend effet immédiatement. Le conseiller informe le client du caractère irréversible de l'opération et du délai de réception de la nouvelle carte."
  }
];
