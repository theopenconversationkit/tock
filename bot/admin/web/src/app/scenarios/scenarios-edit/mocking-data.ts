import { storyCollectorItem } from './story-collector.types';

export const mockingStory_3: storyCollectorItem[] = [
  { id: 0, from: 'client', text: "Bonjour je n'arrive pas à faire mon virement" },
  {
    id: 2,
    parentIds: [0],
    from: 'verification',
    text: 'Vérifier si le service est disponible pour les clients'
  },
  {
    id: 3,
    parentIds: [2],
    from: 'bot',
    text: 'Réponse OK : Quel montant souhaitez-vous virer ?',
    botAnswerType: 'question'
  },
  {
    id: 4,
    parentIds: [2],
    from: 'bot',
    text: "Réponse KO : Message de service indisponible, il faut une intervention d'un conseiller.\nProposer au client:\n- Débranchement\n- Message conseiller pour rappel",
    botAnswerType: 'question'
  },
  { id: 5, parentIds: [3], from: 'client', text: '2000€' },
  {
    id: 6,
    parentIds: [5],
    from: 'bot',
    text: "Virement en France ou à l'étranger?",
    botAnswerType: 'question'
  },
  { id: 7, parentIds: [6], from: 'client', text: 'En France' },
  {
    id: 8,
    parentIds: [7],
    from: 'verification',
    text: "Appel API :\nCalculer l'encours et comparer aux plafonds du client"
  },
  {
    id: 9,
    parentIds: [17],
    from: 'verification',
    text: 'Appel API :\nVérifier si possible de le faire en autonomie pour le client (vérifier si ça dépasse la limite ou pas)'
  },
  { id: 12, parentIds: [6], from: 'client', text: "A l'étranger", botAnswerType: 'question' },
  { id: 13, parentIds: [4], from: 'client', text: 'Je veux parler à un conseiller' },
  { id: 14, parentIds: [4], from: 'client', text: 'Laisser un message à mon conseiller' },
  {
    id: 15,
    parentIds: [14],
    from: 'verification',
    text: 'Appel API:\nEnvoyer message au conseiller'
  },
  {
    id: 16,
    parentIds: [15],
    from: 'bot',
    text: "Confirmer l'envoi du message au conseiller",
    botAnswerType: 'final'
  },
  {
    id: 17,
    parentIds: [8],
    from: 'bot',
    text: "Donner l'information que le plafond dépassé",
    botAnswerType: 'question'
  },
  {
    id: 18,
    parentIds: [9],
    from: 'bot',
    text: "Eligible & Limite non dépassée : \nProposer de modifier lui-même ses plafonds (lien vers la page du site / lien sur l'applicaion)",
    botAnswerType: 'final'
  },
  {
    id: 19,
    parentIds: [9],
    from: 'bot',
    text: 'Eligible & Limite dépassée et CDR ouvert : \nDébranchement vers un conseiller pour faire la modification',
    botAnswerType: 'final'
  },
  {
    id: 20,
    parentIds: [9],
    from: 'bot',
    text: 'Eligible & Limite dépassée et HNO : \nMessage revenir demain',
    botAnswerType: 'final'
  },
  {
    id: 21,
    parentIds: [9],
    from: 'bot',
    text: 'Non éligible : \nMessage revenir demain',
    botAnswerType: 'final'
  },
  {
    id: 22,
    parentIds: [13],
    from: 'bot',
    text: 'CDR ouvert : \nDébranchement vers un conseiller pour faire la modification',
    botAnswerType: 'final'
  },
  {
    id: 23,
    parentIds: [13],
    from: 'bot',
    text: 'HNO :\nMessage revenir demain',
    botAnswerType: 'final'
  }
];

export const mockingStory_2: storyCollectorItem[] = [
  { id: 0, from: 'client', text: 'Comment activer ma carte?' },
  { id: 1, parentIds: [0], from: 'verification', text: 'Recup identifiant Topaze' },
  {
    id: 2,
    parentIds: [1],
    from: 'bot',
    text: "Souhaitez-vous :\n- Activer une nouvelle carte max ?\n\n- Ou activer une carte bancaire externe que vous avez ajoutée dans l'application ?",
    botAnswerType: 'question'
  },
  { id: 3, parentIds: [2], from: 'client', text: 'Ajouter ma carte BNP' },
  { id: 4, parentIds: [2], from: 'client', text: 'Activer ma carte max' },
  {
    id: 5,
    parentIds: [3],
    from: 'bot',
    text: "Lors de l'ajout de votre carte, un paiement d'authentification de 35€ est fait.\n\nRetrouvez-le sous 3 jours environ sur le relevé bancaire de la carte scannée avec le libellé MAX*XXXX ; les XXXX correspondent à un code chiffré aléatoire à saisir dans l'onglet \"carte max\".\n\nCe montant sera remboursé sous 72h sur votre compte externe.",
    botAnswerType: 'final'
  },
  {
    id: 6,
    parentIds: [4],
    from: 'bot',
    text: 'Est-ce :\n- Une première carte suite à votre souscription ?\n\n- Ou une nouvelle carte suite à une opposition ?\n\n- Ou une nouvelle carte suite à une montée en gamme ?\n\n- Ou une nouvelle carte suite à un renouvellement automatique/anticipé ?',
    botAnswerType: 'question'
  },
  { id: 7, parentIds: [6], from: 'client', text: 'Souscription' },
  { id: 8, parentIds: [6], from: 'client', text: 'Opposition' },
  { id: 9, parentIds: [6], from: 'client', text: 'Renouvellement' },
  { id: 10, parentIds: [6], from: 'client', text: 'Montée en gamme' },
  {
    id: 11,
    parentIds: [7],
    from: 'bot',
    text: 'Lors de la commande, avez-vous scanné une carte bancaire ou avez-vous fait un selfie ?',
    botAnswerType: 'question'
  },
  { id: 12, parentIds: [11], from: 'client', text: "J'ai scanné une carte" },
  { id: 13, parentIds: [11], from: 'client', text: "J'ai fait un selfie" },
  {
    id: 14,
    parentIds: [12],
    from: 'bot',
    text: 'Lors du scan de votre carte bancaire, un paiement d\'authentification de 35€ a été fait.\n\nRetrouvez-le sous 3 jours environ sur le relevé bancaire de la carte scannée avec le libellé MAX*XXXX ; les XXXX correspondent à un code chiffré aléatoire à saisir dans l\'onglet "carte max".\n\nCe montant sera remboursé sous 72h sur votre compte externe.',
    botAnswerType: 'final'
  },
  {
    id: 15,
    parentIds: [13],
    from: 'bot',
    text: 'Pour activer la carte, il suffit de vous rendre dans la rubrique “carte max”, de saisir ses 16 chiffres et de vous laisser guider.',
    botAnswerType: 'final'
  },
  {
    id: 16,
    parentIds: [8],
    from: 'bot',
    text: "Pouvez-vous m'indiquer les 4 derniers chiffres de la nouvelle carte max ?",
    botAnswerType: 'question'
  },
  { id: 17, parentIds: [16], from: 'client', text: '1234' },
  { id: 18, parentIds: [17], from: 'verification', text: 'Activation auto' },
  {
    id: 19,
    parentIds: [18],
    from: 'bot',
    text: "Réponse OK: Ok c'est activé",
    botAnswerType: 'final'
  },
  {
    id: 20,
    parentIds: [18],
    from: 'bot',
    text: 'Réponse KO : Redirection vers un assistant',
    botAnswerType: 'final'
  },
  {
    id: 21,
    parentIds: [9, 10],
    from: 'bot',
    text: 'Pour activer la carte, il vous suffit de faire une opération avec le code PIN de la carte (retrait ou paiement) en France métropolitaine. Les opérations "sans contact" et par internet seront actives après cela.\n\nVotre code PIN n\'a pas changé :)',
    botAnswerType: 'final'
  }
];

export const mockingStory_1: storyCollectorItem[] = [
  {
    id: 0,
    from: 'client',
    text: 'Bonjour, je souhaite prendre un rdv'
  },
  {
    id: 1,
    parentIds: [0],
    from: 'bot',
    botAnswerType: 'question',
    text: 'Sur quel canal ? En caisse, par téléphone ou en visio ?'
  },
  {
    id: 2,
    parentIds: [1],
    from: 'client',
    text: 'En caisse'
  },
  {
    id: 3,
    parentIds: [1],
    from: 'client',
    text: 'Par téléphone'
  },
  {
    id: 4,
    parentIds: [1],
    from: 'client',
    text: 'En visio'
  },
  {
    id: 5,
    parentIds: [2, 3, 9],
    from: 'bot',
    botAnswerType: 'final',
    text: 'Pour prendre rdv vous pouvez vous rendre ici : Lien vers page de prise rdv'
  },
  {
    id: 8,
    parentIds: [4],
    from: 'bot',
    text: "Disposez-vous d'une webcam et d'un micro ?",
    botAnswerType: 'question'
  },
  {
    id: 9,
    parentIds: [8],
    from: 'client',
    text: 'non'
  },
  {
    id: 10,
    parentIds: [8],
    from: 'client',
    text: 'oui'
  },
  // {
  //     "id": 12,
  //     "parentIds": [9],
  //     "from": "bot",
  //     "text": "Pour prendre rdv vous pouvez vous rendre ici : Lien vers page de prise rdv",
  //     "botAnswerType": "final"
  // },
  {
    id: 13,
    parentIds: [10],
    from: 'bot',
    text: "Indiquez deux créneaux de dispo aux horaires d'ouverture des caisses",
    botAnswerType: 'question'
  },
  {
    id: 14,
    parentIds: [13],
    from: 'client',
    text: "Quels sont les horaires d'ouverture des caisses ?"
  },
  {
    id: 15,
    parentIds: [14],
    from: 'verification',
    text: 'Récupération des horaires caisse'
  },
  {
    id: 16,
    parentIds: [15],
    from: 'bot',
    text: "Affichage des horaires d'ouverture caisses",
    botAnswerType: 'question'
  },
  {
    id: 17,
    parentIds: [16],
    from: 'client',
    text: 'Je suis dispo le 05/03 à 14h ou le 07/03 à 15h'
  },
  {
    id: 18,
    parentIds: [17],
    from: 'bot',
    text: 'Redirection vers un conseiller',
    botAnswerType: 'final'
  }
];
