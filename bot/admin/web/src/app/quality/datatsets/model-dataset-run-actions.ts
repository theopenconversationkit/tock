import { DatasetRunAction, DatasetRunActionState } from './models';

export function getMockedDatasetEntries(datasetId: string, runId: string) {
  return mock_runActions.filter((action) => action.datasetId === datasetId && action.runId === runId);
}

export const mock_runActions: DatasetRunAction[] = [
  {
    datasetId: 'abcde5f6g7h8i9j0k1l2m3',
    runId: '1b2c323456789ad4aabb1121',
    questionId: 'e5f6g7h8-i9j0-1k2l-3m4n-5o6p7q8r9s0t',
    state: DatasetRunActionState.COMPLETED,
    retryCount: 0,
    action: {
      playerId: {
        id: 'test_bot_6981ed3fad103710a83e5c49_fr',
        type: 'bot',
        clientId: null
      },
      recipientId: {
        id: 'test_6981ed3fad103710a83e5c49_fr__lprdwq8r1',
        type: 'user',
        clientId: null
      },
      date: '2026-03-03T09:25:53.895Z',
      message: {
        text: 'Oui. Après la signature de votre prêt immobilier, vous pouvez demander à remplacer votre assurance emprunteur par un autre contrat, à condition que les **garanties soient équivalentes** à celles exigées par le Crédit Mutuel du Sud Ouest.\n\nCe qu’il faut transmettre avec votre demande de substitution:\n- **Conditions contractuelles** (générales et particulières) du contrat proposé, précisant notamment: détail des garanties, **quotité assurée**, **montant du capital assuré**, **dates de prise d’effet et de cessation** des garanties.\n- **Échéancier des cotisations**, **coût total de l’assurance** sur la durée du crédit (en euros et en **TAEA**).\n\nTraitement de votre demande:\n- **Délai de réponse: 10 jours ouvrés** après réception d’un dossier complet.\n- **En cas d’accord**: vous recevez un **avenant** à votre contrat de crédit avec les informations du nouveau contrat accepté.\n- **En cas de refus**: la banque précise les critères non respectés.\n- Si le dossier est **incomplet**, il ne peut pas être étudié; vous serez informé des **pièces manquantes**.\n\nRésiliation de l’ancien contrat:\n- Vous devez **notifier l’assureur** (lettre recommandée ou recommandé électronique) avec la décision d’acceptation et la **date de prise d’effet** du contrat substitué.\n- La résiliation prend effet **10 jours** après réception de la décision par l’assureur, ou à la **date de prise d’effet** du contrat substitué si elle est postérieure.\n\nOù déposer votre demande:\n- Adressez votre dossier à votre **Caisse de Crédit Mutuel**: https://www.cmb.fr/reseau-bancaire-cooperatif/web/recherche-agence-credit-mutuel-de-bretagne\n\nCela vous aide-t-il ? Souhaitez-vous que je vous liste les documents à préparer, poursuivre sur ce sujet, changer de sujet, ou être mis en relation avec un conseiller ?',
        footnotes: [
          {
            identifier: '12345678-07637d96-dd99-4faf-9af9-071d4fcfa86e',
            title: 'une-source-ajoutee-durant-ce-run',
            url: 'https://www.cmso.com/reseau-bancaire-cooperatif/web/1344_Media/assurance-emprunteur-apres-emission',
            content: 'Lorem ipsum dolor sit amet.',
            score: 0.5555
          },
          {
            identifier: '2450d180-fc55-4e31-9c5d-1db6b06d9550',
            title: 'demande-de-substitution-apres-acceptation-de-l-offre-de-pret',
            url: 'https://www.cmso.com/reseau-bancaire-cooperatif/web/2241_Media/demande-de-substitution-apres-acceptation-de-l-offre-de-pret',
            content:
              "LES DÉMARCHES A SUIVRE POUR UNE DEMANDE DE SUBSTITUTION\nD’ASSURANCE EMPRUNTEUR APRES L’ACCEPTATION DE L’OFFRE DE CRÉDIT\n\nNous faisons de l’assurance emprunteur une condition d’octroi de nos crédits immobiliers. Vous avez la\npossibilité de nous proposer un autre contrat d’assurance que nous ne pouvons refuser si celui-ci présente un\nniveau de garanties équivalentes à celles que nous exigeons. Cette demande peut nous être faite après\nl’acceptation du contrat de crédit.\n\n\nVotre demande\n\nVous devez nous transmettre **avec votre demande de substitution** :\n\n- Les conditions contractuelles (générales et particulières) du contrat d’assurance proposé en substitution. Le\ncontrat devra notamment préciser :\n\n- Le détail des garanties souscrites,\n\n- La quotité assurée par tête et par type de garantie,\n\n- Le montant du capital assuré par type de garantie,\n\n- La date de prise d’effet et de cessation des garanties ;\n\n- L’échéancier des cotisations d’assurance, le coût de l’assurance sur la durée totale du crédit exprimé en\neuros et en Taux Annuel Effectif de l’Assurance (TAEA) devront également nous être communiqués afin de\npouvoir vous adresser l’avenant à l’offre de crédit immobilier.\n\n[Votre demande de substitution doit être transmise à votre Caisse de Crédit Mutuel qui sera en mesure,](https://www.cmb.fr/reseau-bancaire-cooperatif/web/recherche-agence-credit-mutuel-de-bretagne)\nle cas échéant, de vous apporter toute précision ou information complémentaire.\n\n\nNotre réponse\n\nDans un délai de 10 jours ouvrés à compter de la réception d’un dossier complet, nous vous\ncommuniquerons par écrit notre décision.\n\n- En cas d’accord, nous vous adresserons un avenant à votre contrat de crédit qui reprendra les informations\nrelatives au contrat d’assurance emprunteur accepté en substitution.\n\n- En cas de refus, nous vous préciserons quel(s) critère(s) n’est (ne sont) pas respecté(s).\n\n**Important : si votre dossier est incomplet, nous ne serons pas en mesure d’étudier votre demande.**\n**Nous vous indiquerons les pièces manquantes afin que votre demande puisse être traitée.**\n\n\nLa résiliation du contrat d’assurance\n\nAfin de résilier votre contrat d'assurance, vous devez notifier à l'assureur par lettre recommandée ou par\nenvoi recommandé électronique la décision d'accord que nous vous avons adressée ainsi que la date de prise\nd'effet du contrat d'assurance accepté en substitution. La résiliation du contrat d'assurance prendra effet dix\njours après la réception par l'assureur de la décision d'acceptation ou à la date de prise d'effet du contrat\naccepté en substitution si celle-ci est postérieure. En cas de refus de notre part, le contrat d'assurance n'est\npas résilié.",
            score: 0.68594944
          },
          {
            identifier: '07637d96-dd99-4faf-9af9-071d4fcfa86e',
            title: 'assurance-emprunteur-apres-emission',
            url: 'https://www.cmso.com/reseau-bancaire-cooperatif/web/1344_Media/assurance-emprunteur-apres-emission',
            content:
              'LES DEMARCHES A SUIVRE POUR UNE DEMANDE DE SUBSTITUTION\n\n\nD’ASSURANCE EMPRUNTEUR\n\n\nSubstitution entre l’émission de l’offre de crédit et avant l’acceptation de celle-ci:\n\n\nNous faisons de l’assurance emprunteur une condition d’octroi de nos crédits immobiliers. Vous avez la\npossibilité de nous proposer dès la remise de l’offre de crédit et avant l’acceptation de cette dernière, un autre\ncontrat d’assurance que nous ne pouvons refuser si celui-ci présente un niveau de garanties équivalentes à\ncelles que nous exigeons\n\n\nVotre demande\n\nVous devez nous transmettre avec votre demande de substitution :\n\n\n    - Les conditions contractuelles (générales et particulières) du contrat d’assurance proposé en\n\nsubstitution. Le contrat devra notamment préciser :\n\n     - Le détail des garanties souscrites,\n\n     - La quotité assurée par tête et par type de garantie,\n\n     - Le montant du capital assuré par type de garantie,\n\n     - La date de prise d’effet et de cessation des garanties ;\n\n\nL’échéancier des cotisations d’assurance le coût de l’assurance sur la durée totale du crédit exprimé en\neuros et en Taux Annuel Effectif de l’Assurance (TAEA) devront également nous être communiqués afin\nde pouvoir vous adresser l’offre de crédit immobilier actualisée.\n\n\nNotre réponse\n\nDans un délai de 10 jours ouvrés à compter de la réception d’un dossier complet, nous vous communiquerons\npar écrit notre décision\n\n\n    - En cas d’accord, nous vous adresserons une nouvelle offre de crédit qui reprendra les informations\nrelatives au contrat d’assurance emprunteur accepté en substitution.\n\n    - En cas de refus, nous vous préciserons quel(s) critère(s) n’est (ne sont) pas respecté(s).\n\n**Important : si votre dossier est incomplet, nous ne serons pas en mesure d’étudier votre demande. Nous**\n**vous indiquerons les pièces manquantes afin que votre demande puisse être traitée.**\n\n\nVotre demande de substitution doit être transmise à votre Caisse de Crédit Mutuel ( [ici](https://www.cmb.fr/banque/assurance/credit-mutuel/web/c_25035/rechercher-une-agence) ) qui sera en\nmesure, le cas échéant, de vous apporter toute précision ou information complémentaire.',
            score: 0.64422506
          }
        ],
        delay: 0,
        eventType: 'sentenceWithFootnotes',
        isSimpleMessage: false
      },
      userInterfaceType: 'textChat',
      test: true,
      id: '69a6a921a4b90031572c7a87',
      applicationId: 'test-cmso',
      metadata: {
        lastAnswer: false,
        priority: 'normal',
        visibility: 'UNKNOWN',
        replyMessage: 'UNKNOWN',
        quoteMessage: 'UNKNOWN',
        orchestrationLock: false,
        returnsHistory: false,
        debugEnabled: false,
        sourceWithContent: false,
        isGenAiRagAnswer: true,
        streamedResponse: false
      }
    }
  },
  {
    datasetId: 'abcde5f6g7h8i9j0k1l2m3',
    runId: '1b2c323456789ad4aabb1122',
    questionId: 'e5f6g7h8-i9j0-1k2l-3m4n-5o6p7q8r9s0t',
    state: DatasetRunActionState.COMPLETED,
    retryCount: 0,
    action: {
      playerId: {
        id: 'test_bot_6981ed3fad103710a83e5c49_fr',
        type: 'bot',
        clientId: null
      },
      recipientId: {
        id: 'test_6981ed3fad103710a83e5c49_fr__gcg4iwvbl',
        type: 'user',
        clientId: null
      },
      date: '2026-03-03T09:30:49.514Z',
      message: {
        text: 'Oui. Après la signature de votre prêt immobilier, vous pouvez demander à changer d’assurance emprunteur, à condition que le nouveau contrat présente des **garanties équivalentes** à celles exigées par le Crédit Mutuel du Sud Ouest.\n\nCe qu’il faut fournir avec votre demande:\n- **Contrat d’assurance proposé** (conditions générales et particulières) indiquant : garanties souscrites, **quotités**, **capital assuré**, **dates de prise d’effet et de cessation**.\n- **Échéancier des cotisations**, **coût total de l’assurance** sur la durée du crédit, en euros et en **TAEA**.\n\nTraitement de votre demande:\n- Réponse écrite sous **10 jours ouvrés** après réception d’un dossier complet.\n- **Accord**: envoi d’un **avenant** à votre contrat de crédit reprenant les informations du nouveau contrat.\n- **Refus**: précision des critères non respectés.\n- Dossier **incomplet**: demande des pièces manquantes.\n\nRésiliation de l’ancienne assurance:\n- Vous devez notifier votre assureur actuel par **lettre recommandée** (ou recommandé électronique) la décision d’accord de la banque et la **date d’effet** du nouveau contrat.\n- La résiliation prend effet **10 jours après** réception de l’accord par l’assureur, ou à la date d’effet du nouveau contrat si elle est plus tardive.\n\nOù adresser votre demande:\n- Transmettez-la à votre **Caisse de Crédit Mutuel**: https://www.cmb.fr/reseau-bancaire-cooperatif/web/recherche-agence-credit-mutuel-de-bretagne\n\nCela vous aide-t-il ? Souhaitez-vous que je vous liste précisément les pièces à préparer, continuer sur ce sujet, ou préférez-vous être mis en relation avec un conseiller ?',
        footnotes: [
          {
            identifier: '2450d180-fc55-4e31-9c5d-1db6b06d9550',
            title: 'demande-de-substitution-apres-acceptation-de-l-offre-de-pret',
            url: 'https://www.cmso.com/reseau-bancaire-cooperatif/web/2241_Media/demande-de-substitution-apres-acceptation-de-l-offre-de-pret',
            content:
              "LES DÉMARCHES A SUIVRE POUR UNE DEMANDE DE SUBSTITUTION\nD’ASSURANCE EMPRUNTEUR APRES L’ACCEPTATION DE L’OFFRE DE CRÉDIT\n\nNous faisons de l’assurance emprunteur une condition d’octroi de nos crédits immobiliers. Vous avez la\npossibilité de nous proposer un autre contrat d’assurance que nous ne pouvons refuser si celui-ci présente un\nniveau de garanties équivalentes à celles que nous exigeons. Cette demande peut nous être faite après\nl’acceptation du contrat de crédit.\n\n\nVotre demande\n\nVous devez nous transmettre **avec votre demande de substitution** :\n\n- Les conditions contractuelles (générales et particulières) du contrat d’assurance proposé en substitution. Le\ncontrat devra notamment préciser :\n\n- Le détail des garanties souscrites,\n\n- La quotité assurée par tête et par type de garantie,\n\n- Le montant du capital assuré par type de garantie,\n\n- La date de prise d’effet et de cessation des garanties ;\n\n- L’échéancier des cotisations d’assurance, le coût de l’assurance sur la durée totale du crédit exprimé en\neuros et en Taux Annuel Effectif de l’Assurance (TAEA) devront également nous être communiqués afin de\npouvoir vous adresser l’avenant à l’offre de crédit immobilier.\n\n[Votre demande de substitution doit être transmise à votre Caisse de Crédit Mutuel (ici) qui sera en mesure,](https://www.cmb.fr/reseau-bancaire-cooperatif/web/recherche-agence-credit-mutuel-de-bretagne)\nle cas échéant, de vous apporter toute précision ou information complémentaire.\n\n\nNotre réponse\n\nDans un délai de 10 jours ouvrés à compter de la réception d’un dossier complet, nous vous\ncommuniquerons par écrit notre décision.\n\n- En cas d’accord, nous vous adresserons un avenant à votre contrat de crédit qui reprendra les informations\nrelatives au contrat d’assurance emprunteur accepté en substitution.\n\n- En cas de refus, nous vous préciserons quel(s) critère(s) n’est (ne sont) pas respecté(s).\n\n**Important : si votre dossier est incomplet, nous ne serons pas en mesure d’étudier votre demande.**\n**Nous vous indiquerons les pièces manquantes afin que votre demande puisse être traitée.**\n\n\nLa résiliation du contrat d’assurance\n\nAfin de résilier votre contrat d'assurance, vous devez notifier à l'assureur par lettre recommandée ou par\nenvoi recommandé électronique la décision d'accord que nous vous avons adressée ainsi que la date de prise\nd'effet du contrat d'assurance accepté en substitution. La résiliation du contrat d'assurance prendra effet dix\njours après la réception par l'assureur de la décision d'acceptation ou à la date de prise d'effet du contrat\naccepté en substitution si celle-ci est postérieure. En cas de refus de notre part, le contrat d'assurance n'est\npas résilié.",
            score: 0.68594944
          },
          {
            identifier: '87654321-07637d96-dd99-4faf-9af9-071d4fcfa86e',
            title: 'une-source-qui-disparait-dans-le-run-suivant',
            content: 'Je ferai de vous mes plus beaux souvenirs.',
            score: 0.666
          }
        ],
        delay: 0,
        eventType: 'sentenceWithFootnotes',
        isSimpleMessage: false
      },
      userInterfaceType: 'textChat',
      test: true,
      id: '69a6aa49a4b90031572c7a92',
      applicationId: 'test-cmso',
      metadata: {
        lastAnswer: false,
        priority: 'normal',
        visibility: 'UNKNOWN',
        replyMessage: 'UNKNOWN',
        quoteMessage: 'UNKNOWN',
        orchestrationLock: false,
        returnsHistory: false,
        debugEnabled: false,
        sourceWithContent: false,
        isGenAiRagAnswer: true,
        streamedResponse: false
      }
    }
  },
  {
    datasetId: 'abcde5f6g7h8i9j0k1l2m3',
    runId: 'z9y8x7w6v5u4t3s2r1q0p9o8',
    questionId: 'e5f6g7h8-i9j0-1k2l-3m4n-5o6p7q8r9s0t',
    state: DatasetRunActionState.COMPLETED,
    retryCount: 0,
    action: {
      playerId: {
        id: 'test_bot_6981ed3fad103710a83e5c49_fr',
        type: 'bot',
        clientId: null
      },
      recipientId: {
        id: 'test_6981ed3fad103710a83e5c49_fr__hsbnw1qfa',
        type: 'user',
        clientId: null
      },
      date: '2026-03-03T16:54:01.051Z',
      message: {
        text: 'Oui. Après la signature et l’acceptation de votre offre de crédit immobilier, vous pouvez demander à remplacer votre assurance emprunteur par un autre contrat, à condition que le nouveau contrat présente **des garanties équivalentes** à celles exigées par le Crédit Mutuel du Sud Ouest.\n\nCe que vous devez fournir\n- **Conditions générales et particulières** du nouveau contrat d’assurance, avec :\n  - détail des garanties,\n  - **quotité assurée** par tête et par type de garantie,\n  - **montant du capital assuré** par type de garantie,\n  - **dates de prise d’effet et de cessation** des garanties.\n- **Échéancier des cotisations**, **coût total de l’assurance** sur la durée du crédit (en euros et **TAEA**) pour que nous puissions établir l’avenant à votre offre de crédit.\n\nTraitement de votre demande\n- Réponse **écrite sous 10 jours ouvrés** après réception d’un dossier complet.\n- En cas d’**accord** : envoi d’un **avenant** à votre contrat de crédit intégrant l’assurance acceptée en substitution.\n- En cas de **refus** : indication des **critères non respectés**.\n- Si le dossier est **incomplet**, nous vous indiquerons les pièces manquantes.\n\nRésiliation de l’ancienne assurance\n- Notifiez à votre assureur, par **lettre recommandée** ou **recommandé électronique**, notre décision d’accord et la **date de prise d’effet** du nouveau contrat.\n- La résiliation prend effet **10 jours après réception** par l’assureur de la décision d’acceptation, ou à la **date de prise d’effet du contrat substitué** si elle est postérieure.\n\nOù transmettre votre demande\n- Adressez votre dossier à **votre Caisse de Crédit Mutuel** : https://www.cmb.fr/reseau-bancaire-cooperatif/web/recherche-agence-credit-mutuel-de-bretagne\n\nCela vous aide ? Souhaitez-vous que je vous liste les pièces à préparer ou que je vous oriente vers votre caisse CMSO pour déposer la demande ? Je peux aussi vous mettre en relation avec un conseiller si besoin.',
        footnotes: [
          {
            identifier: '2450d180-fc55-4e31-9c5d-1db6b06d9550',
            title: 'demande-de-substitution-apres-acceptation-de-l-offre-de-pret',
            url: 'https://www.cmso.com/reseau-bancaire-cooperatif/web/2241_Media/demande-de-substitution-apres-acceptation-de-l-offre-de-pret',
            score: 0.68594944
          }
        ],
        delay: 0,
        eventType: 'sentenceWithFootnotes',
        isSimpleMessage: false
      },
      userInterfaceType: 'textChat',
      test: true,
      id: '69a71229a4b90031572c7aac',
      applicationId: 'test-cmso',
      metadata: {
        lastAnswer: false,
        priority: 'normal',
        visibility: 'UNKNOWN',
        replyMessage: 'UNKNOWN',
        quoteMessage: 'UNKNOWN',
        orchestrationLock: false,
        returnsHistory: false,
        debugEnabled: false,
        sourceWithContent: false,
        isGenAiRagAnswer: true,
        streamedResponse: false
      }
    }
  },

  {
    datasetId: 'abcde5f6g7h8i9j0k1l2m3',
    runId: '1b2c323456789ad4aabb1121',
    questionId: 'f6g7h8i9-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
    state: DatasetRunActionState.COMPLETED,
    retryCount: 0,
    action: {
      intent: 'miroirmonbeaumiroir',
      storyId: '6995a716e7871a715a250044',
      storyLabel: 'Miroir, mon beau miroir',
      playerId: {
        id: 'test_bot_6981ed3fad103710a83e5c49_fr',
        type: 'bot',
        clientId: null
      },
      recipientId: {
        id: 'test_6981ed3fad103710a83e5c49_fr__9szgg7jw0',
        type: 'user',
        clientId: null
      },
      date: '2026-02-18T11:48:38.872Z',
      message: {
        text: 'Célèbre est ta beauté, Majesté. Pourtant, une jeune fille en loques, dont les haillons ne peuvent dissimuler la grâce, est hélas encore plus belle que toi.',
        messages: [],
        delay: 0,
        eventType: 'sentence',
        isSimpleMessage: true
      },
      userInterfaceType: 'textChat',
      test: true,
      id: '6995a716e7871a715a250044',
      applicationId: 'test-cmso',
      metadata: {
        lastAnswer: false,
        priority: 'normal',
        visibility: 'UNKNOWN',
        replyMessage: 'UNKNOWN',
        quoteMessage: 'UNKNOWN',
        orchestrationLock: false,
        returnsHistory: false,
        debugEnabled: false,
        sourceWithContent: false,
        isGenAiRagAnswer: false,
        streamedResponse: false
      }
    }
  },
  {
    datasetId: 'abcde5f6g7h8i9j0k1l2m3',
    runId: '1b2c323456789ad4aabb1121',
    questionId: '11111111-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
    state: DatasetRunActionState.COMPLETED,
    retryCount: 0,
    action: {
      playerId: {
        id: 'test_bot_6981ed3fad103710a83e5c49_fr',
        type: 'bot',
        clientId: null
      },
      recipientId: {
        id: 'test_6981ed3fad103710a83e5c49_fr__zzvgzskky',
        type: 'user',
        clientId: null
      },
      date: '2026-02-26T08:47:57.808Z',
      message: {
        text: 'Je suis désolé pour cet incident.\n\nVoici ce que je peux vous indiquer selon les documents du Crédit Mutuel du Sud Ouest, et ce que vous pouvez faire dès maintenant.\n\n• Si le véhicule était un **véhicule de location** et que vous avez payé la location avec une carte du Crédit Mutuel du Sud Ouest, certaines garanties d’assurance carte peuvent couvrir :\n  - le **vol des bagages par effraction** dans le véhicule de location (avec dépôt de plainte dans les **48 heures** et justificatifs de valeur des biens). Indemnisation plafonnée et exclusions précises (espèces, papiers d’identité, cartes de paiement, etc.).\n  - les **dommages matériels au véhicule de location** ou son vol, sous réserve d’avoir réglé la location avec la carte et d’avoir déposé plainte dans les **48 heures**.\n\n• Points clés à vérifier si c’est un véhicule de location :\n  - Vous avez bien **réglé la location avec votre carte** CMSO (la preuve de paiement peut être demandée).\n  - Le **dépôt de plainte** a été effectué dans les **48 heures** et mentionne les circonstances et les références du véhicule/bagages.\n  - Attention : les **véhicules loués entre particuliers ne sont pas couverts** par ces garanties carte.\n\n• Si c’est votre **véhicule personnel** (non loué), la documentation fournie ne détaille pas les garanties applicables. Dans ce cas, je peux vous mettre en relation avec un **conseiller** pour vérifier votre contrat d’assurance (auto ou multirisque) et vous guider pour la déclaration de sinistre.\n\nQue faire tout de suite :\n- Déposer plainte (si ce n’est pas déjà fait) et conserver le récépissé.\n- Prendre des **photos** des dégradations et lister les biens volés avec leurs **factures** si possible.\n- Rassembler vos **preuves de paiement** (location réglée par carte si véhicule loué).\n- Contacter votre **agence CMSO** ou votre assureur pour la **déclaration de sinistre**.\n\nSouhaitez-vous que je vous mette en relation avec un conseiller pour vérifier vos garanties et vous accompagner ?\n\nCela vous aide-t-il ? Je peux continuer sur ce sujet, passer à un autre, ou vous mettre en contact avec le support.',
        footnotes: [
          {
            identifier: 'c370fa91-503b-4ec5-b1ba-46b2ddef006a',
            title: 'business_executive.pdf',
            url: 'https://www.cmso.com/banque/assurance/credit-mutuel/pro/upload/docs/application/pdf/2015-04/business_executive.pdf',
            score: 0.8221891
          },
          {
            identifier: 'f6794e8c-949e-4587-8b1f-398f8f01641e',
            title: '2026-cm-arkea-mastercard-gold-cmso',
            url: 'https://www.cmso.com/reseau-bancaire-cooperatif/web/4158_Media/2026-cm-arkea-mastercard-gold-cmso',
            score: 0.27512974
          }
        ],
        delay: 0,
        eventType: 'sentenceWithFootnotes',
        isSimpleMessage: false
      },
      userInterfaceType: 'textChat',
      test: true,
      id: '69a008bdebf8c6782670d469',
      applicationId: 'test-cmso',
      metadata: {
        lastAnswer: false,
        priority: 'normal',
        visibility: 'UNKNOWN',
        replyMessage: 'UNKNOWN',
        quoteMessage: 'UNKNOWN',
        orchestrationLock: false,
        returnsHistory: false,
        debugEnabled: false,
        sourceWithContent: false,
        isGenAiRagAnswer: true,
        streamedResponse: false
      }
    }
  },
  {
    datasetId: 'abcde5f6g7h8i9j0k1l2m3',
    runId: '1b2c323456789ad4aabb1121',
    questionId: '22222222-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
    state: DatasetRunActionState.COMPLETED,
    retryCount: 0,
    action: {
      playerId: {
        id: 'test_bot_696f5ce07c254b2c0160891d_fr',
        type: 'bot',
        clientId: null
      },
      recipientId: {
        id: 'test_696f5ce07c254b2c0160891d_fr__k5ts8fejj',
        type: 'user',
        clientId: null
      },
      date: '2026-03-03T11:03:23.821Z',
      message: {
        text: '**Objectif**\nValider un objet JSON contre un schéma (JSON Schema) en TypeScript/Vue.\n\n**Approche recommandée**\n- Utiliser JSON Schema pour décrire la forme attendue des données.\n- Utiliser une librairie de validation comme AJV (rapide, standard de facto).\n\n**Étapes avec AJV**\n1) Installer les dépendances\n```bash\nnpm install ajv ajv-formats\n```\n\n2) Définir un schéma JSON (ex. utilisateur)\n```ts\n// schema.ts\nexport const userSchema = {\n  type: "object",\n  properties: {\n    id: { type: "string", minLength: 1 },\n    age: { type: "number", minimum: 0 },\n    email: { type: "string", format: "email" },\n    roles: {\n      type: "array",\n      items: { type: "string" },\n      minItems: 1\n    }\n  },\n  required: ["id", "email"],\n  additionalProperties: false\n} as const;\n```\n\n3) Valider des données avec AJV\n```ts\n// validate.ts\nimport Ajv from "ajv";\nimport addFormats from "ajv-formats";\nimport { userSchema } from "./schema";\n\nconst ajv = new Ajv({ allErrors: true, strict: false });\naddFormats(ajv);\n\n// Compilez une fois le schéma pour des validations répétées\nconst validateUser = ajv.compile(userSchema);\n\nexport function validate(data: unknown) {\n  const valid = validateUser(data);\n  if (valid) return { valid: true as const };\n\n  // Format simple des erreurs\n  const errors = (validateUser.errors ?? []).map(e => {\n    const path = e.instancePath || e.schemaPath;\n    return `${path} ${e.message ?? "is invalid"}`;\n  });\n\n  return { valid: false as const, errors };\n}\n```\n\n4) Exemple d’utilisation\n```ts\nimport { validate } from "./validate";\n\nconst payload = {\n  id: "u_123",\n  email: "john@doe.com",\n  age: 28,\n  roles: ["admin"]\n};\n\nconst res = validate(payload);\nif (!res.valid) {\n  console.error("Invalid payload:", res.errors);\n} else {\n  console.log("Payload is valid");\n}\n```\n\n**Intégration Vue (exemple rapide)**\n- Valider lors de la soumission d’un formulaire ou en temps réel.\n```ts\n// composable useUserValidation.ts\nimport { ref } from "vue";\nimport { validate } from "./validate";\n\nexport function useUserValidation() {\n  const errors = ref<string[]>([]);\n  function check(data: unknown) {\n    const res = validate(data);\n    errors.value = res.valid ? [] : res.errors;\n    return res.valid;\n  }\n  return { errors, check };\n}\n```\n\n**Conseils**\n- Compilez vos schémas une seule fois et réutilisez les fonctions de validation pour de meilleures performances.\n- Utilisez `additionalProperties: false` pour refuser les champs non prévus.\n- Si vous préférez définir votre schéma en code et bénéficier d’inférence TypeScript, regardez des alternatives comme Zod ou Yup (elles ne sont pas JSON Schema, mais très pratiques côté front).\n',
        footnotes: [],
        delay: 0,
        eventType: 'sentenceWithFootnotes',
        isSimpleMessage: false
      },
      userInterfaceType: 'textChat',
      test: true,
      id: '69a6bffba4b90031572c7a9f',
      applicationId: 'test-libs',
      metadata: {
        lastAnswer: false,
        priority: 'normal',
        visibility: 'UNKNOWN',
        replyMessage: 'UNKNOWN',
        quoteMessage: 'UNKNOWN',
        orchestrationLock: false,
        returnsHistory: false,
        debugEnabled: false,
        sourceWithContent: false,
        isGenAiRagAnswer: true,
        streamedResponse: false
      }
    }
  },
  {
    datasetId: 'abcde5f6g7h8i9j0k1l2m3',
    runId: '1b2c323456789ad4aabb1122',
    questionId: '22222222-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
    state: DatasetRunActionState.COMPLETED,
    retryCount: 0,
    action: {
      playerId: {
        id: 'test_bot_696f5ce07c254b2c0160891d_fr',
        type: 'bot',
        clientId: null
      },
      recipientId: {
        id: 'test_696f5ce07c254b2c0160891d_fr__16i8g2o13',
        type: 'user',
        clientId: null
      },
      date: '2026-03-02T09:28:44.053Z',
      message: {
        text: 'Pour valider un JSON avec un schéma, le standard le plus courant est JSON Schema. En pratique, on utilise une librairie de validation comme Ajv en TypeScript/Vue.\n\nVoici les étapes typiques:\n\n1) Installer Ajv\n\n```bash\nnpm install ajv\n```\n\n2) Définir votre schéma JSON Schema (ex: Draft 2020-12)\n\n```ts\n// user.schema.ts\nexport const userSchema = {\n  $id: "https://example.com/schemas/user.json",\n  type: "object",\n  properties: {\n    id: { type: "string" },\n    age: { type: "number", minimum: 0 },\n    email: { type: "string", format: "email" },\n    roles: { type: "array", items: { type: "string" }, minItems: 1 }\n  },\n  required: ["id", "email"],\n  additionalProperties: false\n} as const;\n```\n\n3) Valider un objet JSON avec Ajv (TypeScript)\n\n```ts\n// validate.ts\nimport Ajv, {JSONSchemaType} from "ajv";\nimport { userSchema } from "./user.schema";\n\nconst ajv = new Ajv({ allErrors: true, strict: true });\nconst validate = ajv.compile(userSchema as unknown as JSONSchemaType<any>);\n\nconst data = {\n  id: "u_123",\n  email: "john@example.com",\n  age: 30,\n  roles: ["admin"]\n};\n\nif (validate(data)) {\n  console.log("Valide ✅");\n} else {\n  console.error("Invalide ❌", validate.errors);\n}\n```\n\n4) Gestion des erreurs lisibles\n\n```ts\nimport { ErrorObject } from "ajv";\n\nfunction formatErrors(errors: ErrorObject[] = []) {\n  return errors.map(e => `path=${e.instancePath || "/"} | rule=${e.keyword} | ${e.message}`).join("\\n");\n}\n\nif (!validate(data)) {\n  console.error(formatErrors(validate.errors));\n}\n```\n\n5) Exemple dans un composant Vue (validation au submit)\n\n```ts\n<script setup lang="ts">\nimport { ref } from "vue";\nimport Ajv from "ajv";\nimport { userSchema } from "./user.schema";\n\nconst form = ref({ id: "", email: "", age: 0, roles: [] as string[] });\nconst errors = ref<string[]>([]);\n\nconst ajv = new Ajv({ allErrors: true });\nconst validate = ajv.compile(userSchema);\n\nfunction submit() {\n  errors.value = [];\n  if (validate(form.value)) {\n    // envoyer\n    console.log("Form OK");\n  } else {\n    errors.value = (validate.errors || []).map(e => `${e.instancePath} ${e.message}`);\n  }\n}\n</script>\n```\n\n6) Validation côté CLI (utile en CI)\n\n```bash\n# Option 1: écrire un petit script Node qui lit un JSON et le valide\nnode validate.js path/to/data.json\n\n# validate.js (exemple minimal)\n// node validate.js <file>\nimport fs from "node:fs";\nimport Ajv from "ajv";\nimport { userSchema } from "./user.schema.js";\n\nconst file = process.argv[2];\nconst json = JSON.parse(fs.readFileSync(file, "utf-8"));\nconst ajv = new Ajv({ allErrors: true });\nconst validate = ajv.compile(userSchema);\n\nif (!validate(json)) {\n  console.error("Invalid", validate.errors);\n  process.exit(1);\n}\nconsole.log("Valid");\n```\n\nBonnes pratiques:\n- Définir `additionalProperties: false` pour éviter les champs inattendus.\n- Utiliser `format` (email, uri, date-time) quand pertinent.\n- Centraliser les schémas et réutiliser via `$ref` si vous avez plusieurs modèles.\n- En TypeScript, vous pouvez typer vos données à partir du schéma avec des outils dédiés (par ex. génération de types), ou maintenir les types à la main et valider au runtime.\n\nSi vous utilisez le stack SoFA/Vue, Ajv s’intègre facilement côté front et services. Dites-moi votre cas d’usage (formulaires, configuration, API) et je vous propose un exemple adapté.',
        footnotes: [],
        delay: 0,
        eventType: 'sentenceWithFootnotes',
        isSimpleMessage: false
      },
      userInterfaceType: 'textChat',
      test: true,
      id: '69a5584cbbb74993c8c3cbf1',
      applicationId: 'test-libs',
      metadata: {
        lastAnswer: false,
        priority: 'normal',
        visibility: 'UNKNOWN',
        replyMessage: 'UNKNOWN',
        quoteMessage: 'UNKNOWN',
        orchestrationLock: false,
        returnsHistory: false,
        debugEnabled: false,
        sourceWithContent: false,
        isGenAiRagAnswer: true,
        streamedResponse: false
      }
    }
  },
  {
    datasetId: 'abcde5f6g7h8i9j0k1l2m3',
    runId: 'z9y8x7w6v5u4t3s2r1q0p9o8',
    questionId: '22222222-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
    state: DatasetRunActionState.COMPLETED,
    retryCount: 0,
    action: {
      playerId: {
        id: 'test_bot_696f5ce07c254b2c0160891d_fr',
        type: 'bot',
        clientId: null
      },
      recipientId: {
        id: 'test_696f5ce07c254b2c0160891d_fr__xy3zldbg6',
        type: 'user',
        clientId: null
      },
      date: '2026-03-03T16:57:32.096Z',
      message: {
        text: 'Voici une approche simple et standard pour valider un JSON avec un JSON Schema en TypeScript, en utilisant Ajv (l’un des validateurs JSON Schema les plus utilisés).\n\n## 1) Installer Ajv\n```bash\nnpm i ajv\n# (optionnel) pour les mots-clés/formats utiles\nnpm i ajv-formats\n```\n\n## 2) Définir le schema (JSON Schema draft-07+)\n```ts\n// schema/user.schema.ts\nexport const userSchema = {\n  $id: "https://example.com/schemas/user.json",\n  type: "object",\n  additionalProperties: false,\n  required: ["id", "email", "roles"],\n  properties: {\n    id: { type: "string", minLength: 1 },\n    email: { type: "string", format: "email" },\n    age: { type: "integer", minimum: 0 },\n    roles: { type: "array", items: { type: "string" }, minItems: 1 }\n  }\n} as const;\n```\n\n## 3) Valider un objet contre le schema\n```ts\n// validateUser.ts\nimport Ajv from "ajv";\nimport addFormats from "ajv-formats";\nimport { userSchema } from "./schema/user.schema";\n\nconst ajv = new Ajv({ allErrors: true, strict: false });\naddFormats(ajv); // active format: "email", "uri", etc.\n\n// Optionnel : enregistrer le schéma pour réutilisation\najv.addSchema(userSchema);\n\nexport function validateUser(data: unknown) {\n  const validate = ajv.getSchema(userSchema.$id!) || ajv.compile(userSchema);\n  const valid = validate(data);\n  if (valid) return { valid: true as const, errors: [] as const };\n  return { valid: false as const, errors: validate.errors ?? [] };\n}\n```\n\n## 4) Utilisation et gestion des erreurs\n```ts\nimport { validateUser } from "./validateUser";\n\nconst candidate = {\n  id: "u_123",\n  email: "not-an-email",\n  roles: []\n};\n\nconst res = validateUser(candidate);\nif (!res.valid) {\n  // Formatage simple des erreurs\n  const messages = res.errors.map(e => `${e.instancePath || \'/\'} ${e.message}`).join("\\n");\n  console.error("Validation error:\\n" + messages);\n} else {\n  console.log("JSON valide !");\n}\n```\n\n## 5) Intégration rapide dans un composant Vue (exemple)\n```ts\n<script setup lang="ts">\nimport { ref } from "vue";\nimport { validateUser } from "./validateUser";\n\nconst formData = ref({ id: "", email: "", roles: ["user"] });\nconst errors = ref<string[]>([]);\n\nfunction submit() {\n  const res = validateUser(formData.value);\n  if (!res.valid) {\n    errors.value = (res.errors || []).map(e => `${e.instancePath || \'/\'} ${e.message}`);\n    return;\n  }\n  // continuer: appel API, etc.\n}\n</script>\n\n<template>\n  <form @submit.prevent="submit">\n    <!-- champs ... -->\n    <ul v-if="errors.length">\n      <li v-for="e in errors" :key="e">{{ e }}</li>\n    </ul>\n    <button type="submit">Valider</button>\n  </form>\n</template>\n```\n\n## 6) Bonnes pratiques\n- Précompiler les schémas (ajv.compile) une seule fois et réutiliser la fonction `validate` pour de meilleures performances.\n- Activer `allErrors: true` pendant le développement pour lister toutes les erreurs, puis le désactiver en prod si besoin.\n- Utiliser `additionalProperties: false` pour éviter les champs inattendus.\n- Organiser des schémas réutilisables (décomposer et référencer avec `$id` et `$ref`).\n\nSi tu préfères un style “schema en code” (au lieu de JSON Schema), des libs comme Zod ou Superstruct peuvent aussi valider des objets, mais elles ne consomment pas directement des fichiers JSON Schema. Ici, Ajv est le bon choix pour du JSON Schema standard.',
        footnotes: [],
        delay: 0,
        eventType: 'sentenceWithFootnotes',
        isSimpleMessage: false
      },
      userInterfaceType: 'textChat',
      test: true,
      id: '69a712fca4b90031572c7ab9',
      applicationId: 'test-libs',
      metadata: {
        lastAnswer: false,
        priority: 'normal',
        visibility: 'UNKNOWN',
        replyMessage: 'UNKNOWN',
        quoteMessage: 'UNKNOWN',
        orchestrationLock: false,
        returnsHistory: false,
        debugEnabled: false,
        sourceWithContent: false,
        isGenAiRagAnswer: true,
        streamedResponse: false
      }
    }
  },
  {
    datasetId: 'abcde5f6g7h8i9j0k1l2m3',
    runId: '1b2c323456789ad4aabb1121',
    questionId: '3333333-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
    state: DatasetRunActionState.COMPLETED,
    retryCount: 0,
    action: null
  },
  {
    datasetId: 'abcde5f6g7h8i9j0k1l2m3',
    runId: '1b2c323456789ad4aabb1122',
    questionId: '3333333-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
    state: DatasetRunActionState.FAILED,
    retryCount: 0,
    action: null
  }
];
