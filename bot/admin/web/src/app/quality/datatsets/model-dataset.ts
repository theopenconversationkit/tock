import { AiEngineProvider, PromptDefinitionFormatter } from '../../shared/model/ai-settings';
import { Dataset, DatasetRunState } from './models';

export const mock_datasets: Dataset[] = [
  {
    id: '12345d9d6c3d79671caab6c5',
    name: 'Résiliations & réclamations',
    description: 'Processus de résiliation, délais légaux, recours',
    createdAt: '2024-06-01T10:00:00Z',
    createdBy: 'LS661',
    updatedAt: null,
    updatedBy: null,
    questions: [
      {
        id: 'd9e5c8b0-1a2b-4c3d-9e4f-5a6b7c8d9e0f',
        question: 'Quels sont les délais légaux pour résilier un contrat d’assurance habitation ?',
        groundTruth:
          'En général, vous pouvez résilier votre contrat d’assurance habitation à tout moment après la première année de souscription, en respectant un préavis de 30 jours. Cependant, il existe des exceptions, notamment en cas de changement de situation personnelle ou professionnelle.'
      },
      {
        id: 'a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6',
        question: 'Comment contester une décision de refus de remboursement ?',
        groundTruth:
          'Pour contester une décision de refus de remboursement, vous devez d’abord contacter votre assureur pour obtenir une explication détaillée du motif du refus. Ensuite, vous pouvez envoyer une lettre recommandée avec accusé de réception à votre assureur en exposant clairement les raisons pour lesquelles vous contestez la décision, en joignant tous les documents pertinents (contrat, échanges avec l’assureur, etc.). Si la contestation n’aboutit pas, vous pouvez saisir le médiateur de l’assurance ou engager une action en justice.'
      },
      {
        id: 'b2c3d4e5-f6g7-8h9i-0j1k-l2m3n4o5p6q7',
        question: 'Puis-je résilier mon assurance auto si je vends mon véhicule ?',
        groundTruth:
          'Oui, la vente de votre véhicule est un motif légitime de résiliation de votre contrat d’assurance auto. Vous devez informer votre assureur par lettre recommandée avec accusé de réception, en joignant une copie du certificat de cession du véhicule. La résiliation prendra effet à la date de vente.'
      }
    ],
    runs: [
      {
        id: '123456789a1b2c3d4aabb112',
        state: DatasetRunState.COMPLETED,
        startTime: '2024-06-01T10:00:00Z',
        endTime: '2024-06-01T10:05:00Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: '68500d9c6c2d73676cdab6b1',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.4,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given a chat history and the latest user question which might reference context in the chat history, formulate a standalone question which can be understood without the chat history. Do NOT answer the question, just reformulate it if needed and otherwise return it as is.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.4,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## General Context\nYou are “Hey Mo”, a chatbot designed to provide concise and informative responses to questions from Crédit Mutuel Arkea assistants in a support structure specialized in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.).\nYour primary role is to assist assistants in finding the appropriate information and resources available in the internal FAQ and documentation related to credit in order to answer questions they have from counselors of Crédit Mutuel de Bretagne (CMB) or Credit Mutuel du Sud Ouest (CMSO) on a helpline. \nYou specialize in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.) and your responses should remain strictly within this scope. You should never make any commercial propositions, advise on banking transactions or rates, or make assumptions about client\'s financial situations.\n\n## Style of the response\nYour tone should be empathetic, informative, polite, and formal, always using "vous" to address assistants.\nResponses should be concise, approximately 250 characters, and employ bullet points when appropriate. Use simple, accessible language.\n\n## Guidelines\nThe Documents below corresponds to document extracts that have been retrieved according to the question posed, indicating that some of them may not be as useful as others. \n\n## Instructions\nBased on this context, answer in {{locale}} the question below.\nIf the question asked is not understandable, ask the user to reformulate it.\nIf a question goes beyond the scope of the documentation provided or outside the credit domain simply answer with "{{ no_answer }}"\n\n## Verification Steps\nBefore responding, consider:\n- Can you provide a confident answer based on the available documentation?\n- Does the documentation align with the question posed?\n\nIf uncertain or if the topic is not covered in the documentation, answer with "{{ no_answer }}"\n\n## Documents\nUse the following documents to answer the question:\n{{context}}\n\n## Question\n{{question}}'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: '68d1f943-685e-4618-83fd-9ab2ff0dcd95',
          indexName: 'ns_heymo_credit_bot_heymo_session_68d1f943_685e_4618_83fd_9ab2ff0dcd95',
          debugEnabled: true,
          maxDocumentsRetrieved: 4,
          maxMessagesFromHistory: 5
        }
      },
      {
        id: '567891011a1b2c3d4aabb112',
        state: DatasetRunState.RUNNING,
        startTime: new Date(Date.now() - 23.997 * 60 * 60 * 1000).toISOString().replace(/\.\d{3}Z$/, 'Z'),
        endTime: null,
        startedBy: 'LS661',
        settingsSnapshot: {
          id: '68500d9c6c2d73676cdab6b2',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: false,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.4,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given a chat history and the latest user question which might reference context in the chat history, formulate a standalone question which can be understood without the chat history. Do NOT answer the question, just reformulate it if needed and otherwise return it as is.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.4,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## General Context\nYou are “Hey Mo”, a chatbot designed to provide concise and informative responses to questions from Crédit Mutuel Arkea assistants in a support structure specialized in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.).\nYour primary role is to assist assistants in finding the appropriate information and resources available in the internal FAQ and documentation related to credit in order to answer questions they have from counselors of Crédit Mutuel de Bretagne (CMB) or Credit Mutuel du Sud Ouest (CMSO) on a helpline. \nYou specialize in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.) and your responses should remain strictly within this scope. You should never make any commercial propositions, advise on banking transactions or rates, or make assumptions about client\'s financial situations.\n\n## Style of the response\nYour tone should be empathetic, informative, polite, and formal, always using "vous" to address assistants.\nResponses should be concise, approximately 250 characters, and employ bullet points when appropriate. Use simple, accessible language.\n\n## Guidelines\nThe Documents below corresponds to document extracts that have been retrieved according to the question posed, indicating that some of them may not be as useful as others. \n\n## Instructions\nBased on this context, answer in {{locale}} the question below.\nIf the question asked is not understandable, ask the user to reformulate it.\nIf a question goes beyond the scope of the documentation provided or outside the credit domain simply answer with "{{ no_answer }}"\n\n## Verification Steps\nBefore responding, consider:\n- Can you provide a confident answer based on the available documentation?\n- Does the documentation align with the question posed?\n\nIf uncertain or if the topic is not covered in the documentation, answer with "{{ no_answer }}"\n\n## Documents\nUse the following documents to answer the question:\n{{context}}\n\n## Question\n{{question}}'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: '68d1f943-685e-4618-83fd-9ab2ff0dcd96',
          indexName: 'ns_heymo_credit_bot_heymo_session_68d1f943_685e_4618_83fd_9ab2ff0dcd96',
          debugEnabled: false,
          maxDocumentsRetrieved: 4,
          maxMessagesFromHistory: 5
        }
      }
    ]
  },
  {
    id: '67890e1f2a3b4c5d6e7f8a9b',
    name: 'Prêts immobiliers',
    description: 'Taux, conditions, simulation, remboursement anticipé',
    createdAt: '2024-06-01T10:00:00Z',
    createdBy: 'LS661',
    updatedAt: null,
    updatedBy: null,
    questions: [
      {
        id: 'c3d4e5f6-g7h8-9i0j-k1l2-m3n4o5p6q7r8',
        question: 'Quels sont les frais en cas de remboursement anticipé d’un prêt immobilier ?',
        groundTruth:
          'Les frais de remboursement anticipé d’un prêt immobilier sont plafonnés par la loi. Ils ne peuvent excéder 1% du capital remboursé anticipativement si le remboursement intervient dans les 12 premiers mois suivant le premier remboursement, et 0,5% ensuite. Certains contrats peuvent prévoir des conditions plus avantageuses.'
      },
      {
        id: 'd4e5f6g7-h8i9-0j1k-l2m3-n4o5p6q7r8s9',
        question: 'Comment obtenir une simulation de prêt immobilier en ligne ?',
        groundTruth:
          'Pour obtenir une simulation de prêt immobilier en ligne, vous pouvez utiliser le simulateur disponible sur le site de votre banque ou sur des plateformes spécialisées. Il vous faudra renseigner le montant du projet, la durée souhaitée, votre apport personnel et vos revenus. Le résultat vous donnera une estimation du taux, de la mensualité et du coût total du crédit.'
      }
    ],
    runs: [
      {
        id: '1b2c3d4aabb112123456789a',
        state: DatasetRunState.COMPLETED,
        startTime: '2026-03-01T10:00:00Z',
        endTime: '2026-03-01T10:12:45Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: '68500d9c6c2d73676cdab6b3',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.3,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given a chat history and the latest user question which might reference context in the chat history, formulate a standalone question which can be understood without the chat history. Do NOT answer the question, just reformulate it if needed and otherwise return it as is.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.3,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## General Context\nYou are “Hey Mo”, a chatbot designed to provide concise and informative responses to questions from Crédit Mutuel Arkea assistants in a support structure specialized in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.).\nYour primary role is to assist assistants in finding the appropriate information and resources available in the internal FAQ and documentation related to credit in order to answer questions they have from counselors of Crédit Mutuel de Bretagne (CMB) or Credit Mutuel du Sud Ouest (CMSO) on a helpline. \nYou specialize in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.) and your responses should remain strictly within this scope. You should never make any commercial propositions, advise on banking transactions or rates, or make assumptions about client\'s financial situations.\n\n## Style of the response\nYour tone should be empathetic, informative, polite, and formal, always using "vous" to address assistants.\nResponses should be concise, approximately 250 characters, and employ bullet points when appropriate. Use simple, accessible language.\n\n## Guidelines\nThe Documents below corresponds to document extracts that have been retrieved according to the question posed, indicating that some of them may not be as useful as others. \n\n## Instructions\nBased on this context, answer in {{locale}} the question below.\nIf the question asked is not understandable, ask the user to reformulate it.\nIf a question goes beyond the scope of the documentation provided or outside the credit domain simply answer with "{{ no_answer }}"\n\n## Verification Steps\nBefore responding, consider:\n- Can you provide a confident answer based on the available documentation?\n- Does the documentation align with the question posed?\n\nIf uncertain or if the topic is not covered in the documentation, answer with "{{ no_answer }}"\n\n## Documents\nUse the following documents to answer the question:\n{{context}}\n\n## Question\n{{question}}'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: '68d1f943-685e-4618-83fd-9ab2ff0dcd97',
          indexName: 'ns_heymo_credit_bot_heymo_session_68d1f943_685e_4618_83fd_9ab2ff0dcd97',
          debugEnabled: true,
          maxDocumentsRetrieved: 6,
          maxMessagesFromHistory: 3
        }
      }
    ]
  },
  {
    id: 'abcde5f6g7h8i9j0k1l2m3',
    name: 'Assurances emprunteur',
    description: 'Garanties, exclusions, changement d’assurance',
    createdAt: '2024-06-01T10:00:00Z',
    createdBy: 'LS661',
    updatedAt: null,
    updatedBy: null,
    questions: [
      {
        id: 'e5f6g7h8-i9j0-1k2l-3m4n-5o6p7q8r9s0t',
        question: 'Puis-je changer d’assurance emprunteur après la signature du prêt ?',
        groundTruth:
          'Oui, depuis la loi Lemoine de 2022, vous pouvez changer d’assurance emprunteur à tout moment, sans frais ni pénalité, sous réserve que le nouveau contrat présente un niveau de garantie équivalent à celui exigé par votre banque. Vous devez informer votre banque de ce changement au moins 15 jours avant la date d’effet souhaitée.'
      },
      {
        id: '22222222-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question: 'comment valider un json avec un schema ?',
        groundTruth: null
      },
      {
        id: 'f6g7h8i9-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question: 'Quelles sont les garanties obligatoires dans une assurance emprunteur ?',
        groundTruth:
          'Les garanties obligatoires dans une assurance emprunteur sont généralement le décès et la perte totale et irréversible d’autonomie (PTIA). Selon les contrats et les profils, d’autres garanties peuvent être exigées, comme l’incapacité temporaire ou permanente de travail (ITT/IPT), ou l’invalidité permanente (IP).'
      },
      {
        id: '11111111-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question: "j'ai eu un vol dans mon véhicule avec dégradation du vehicule",
        groundTruth: null
      },
      {
        id: '22222222-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question: 'Patrimoine taux est-il éligible aux personnes morales',
        groundTruth: null
      },
      {
        id: '3333333-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question: 'alertes graphtux après restart, mrtg dit ok mais thruk toujours rouge → j’escale ou j’attends ?',
        groundTruth: null
      },
      {
        id: '444444-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question:
          'je dois générer les surveillances nagios pour paiement-3, c’est bien le script nagios_stecard.sh sur livrabletux-1-a ? et est-ce que je dois lancer les sous-scripts manuellement ou c’est automatique ?',
        groundTruth: null
      },
      {
        id: '555555-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question:
          'Quelle est la procédure pour arrêter et relancer complètement l’application Stecard sur un serveur (par exemple, pfotux) ?',
        groundTruth: null
      },
      {
        id: '666666-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question: 'Quels sont les services concernés par la supervision des processus RTG et quel est l’impact d’une défaillance ?',
        groundTruth: null
      },
      {
        id: '777777-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question:
          'J’ai perdu mon tel, j’ai installé l’appli sur un nouveau mais ça me dit "compte déjà associé à un autre appareil"… Comment je fais pour transférer ?!',
        groundTruth: null
      },
      {
        id: '888888-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question: 'compte perso ou compte joint, c’est quoi le plus intéressant ? Parce que moi et mon mari on sait pas trop quoi choisir…',
        groundTruth: null
      },
      {
        id: '999999-j0k1-2l3m-4n5o-6p7q8r9s0t1u',
        question: 'Puis-je changer d’assurance emprunteur après la signature du prêt ?',
        groundTruth: null
      }
    ],
    runs: [
      // {
      //   id: '987654321987654321',
      //   state: DatasetRunState.QUEUED,
      //   startTime: new Date(Date.now() - 0.1 * 60 * 60 * 1000).toISOString().replace(/\.\d{3}Z$/, 'Z'),
      //   endTime: null
      // startedBy:'LS661',
      // },
      {
        id: '1b2c323456789ad4aabb1121',
        state: DatasetRunState.COMPLETED,
        startTime: '2024-06-25T18:22:22Z',
        endTime: '2024-06-25T18:49:54Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: '68500d9c6c2d73676cdab6b4',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          // @ts-ignore
          addedAttribute: 'added attribute value',
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.5,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given a chat history and the latest user question which might reference context in the chat history, formulate a standalone question which can be understood without the chat history. Do NOT answer the question, just reformulate it if needed and otherwise return it as is.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.5,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template: `## General Context
You are “Hey Mo”, a nice chatbot designed to provide concise and informative responses to questions from Crédit Mutuel Arkea assistants in a support structure specialized in credit-related topics (personal loans, mortgages, guarantees, insurance, customers relations, etc.).
Your primary role is to assist assistants in finding the appropriate information and resources available in the internal FAQs and documentation related to credit in order to answer questions they have from counselors of Crédit Mutuel de Bretagne (CMB) or Credit Mutuel du Sud Ouest (CMSO) on a helpline.
You specialize in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.) and your responses should remain strictly within this scope. You should never make any commercial propositions, advise on banking transactions or rates, or make assumptions about client\'s financial situations.
## Style of the response
Your tone should be empathetic, informative, polite, and formal, always using "vous" to address assistants.
Responses should be concise, approximately 250 characters, and employ bullet points when appropriate. Use simple, accessible language.
## Guidelines
The Documents below corresponds to document extracts that have been retrieved according to the question posed, indicating that some of them may not be as useful as others.
## Instructions
Based on this context, answer in {{locale}} the question below.
If the question asked is not understandable, ask the user to reformulate it.
If a question goes beyond the scope of the documentation provided or outside the credit domain simply answer with "{{ no_answer }}"
## Verification Steps
Before responding, wait a minute, breath and then consider:
- Can you provide a confident answer based on the available documentation?
- Does the documentation align with the question posed?
If uncertain or if the topic is not covered in the documentation, answer with "{{ no_answer }}"
## Documents
Use the following documents to answer the question:
{{context}}
## Question
{{question}}`
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: '68d1f943-685e-4618-83fd-9ab2ff0dcd98',
          indexName: 'ns_heymo_credit_bot_heymo_session_68d1f943_685e_4618_83fd_9ab2ff0dcd98',
          debugEnabled: false,
          maxDocumentsRetrieved: 3,
          maxMessagesFromHistory: 7
        }
      },
      {
        id: 'a1b2c3d4e5f67890ghij1234',
        state: DatasetRunState.CANCELLED,
        startTime: '2024-06-23T17:15:30Z',
        endTime: '2024-06-23T17:32:10Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: '5a6b7c8d9e0f1g2h3i4j5k6',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.3,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given a chat history and the latest user question which might reference context in the chat history, formulate a standalone question which can be understood without the chat history. Do NOT answer the question, just reformulate it if needed and otherwise return it as is.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.7,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## General Context\nYou are “Hey Mo”, a beautiful chatbot designed to provide concise, precise and informative responses to questions from Crédit Mutuel Arkea assistants in a support structure specialized in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.).\nYour primary role is to assist assistants in finding the appropriate information and resources available in the internal FAQ and documentation related to credit in order to answer questions they have from counselors of Crédit Mutuel de Bretagne (CMB) or Credit Mutuel du Sud Ouest (CMSO) on a helpline. \nYou specialize in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.) and your responses should remain strictly within this scope. You should never make any commercial propositions, advise on banking transactions or rates, or make assumptions about client\'s financial situations.\n\n## Style of the response\nYour tone should be empathetic, informative, polite, and formal, always using "vous" to address assistants.\nResponses should be concise, approximately 250 characters, and employ bullet points when appropriate. Use simple, accessible language.\n\n## Guidelines\nThe Documents below corresponds to document extracts that have been retrieved according to the question posed, indicating that some of them may not be as useful as others. \n\n## Instructions\nBased on this context, answer in {{locale}} the question below.\nIf the question asked is not understandable, ask the user to reformulate it.\nIf a question goes beyond the scope of the documentation provided or outside the credit domain simply answer with "{{ no_answer }}"\n\n## Verification Steps\nBefore responding, consider:\n- Can you provide a confident answer based on the available documentation?\n- Does the documentation align with the question posed?\n\nIf uncertain or if the topic is not covered in the documentation, answer with "{{ no_answer }}"\n\n## Documents\nUse the following documents to answer the question:\n{{context}}\n\n## Question\n{{question}}'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: 'a1b2c3d4-e5f6-7890-ghij-1234567890ab',
          indexName: 'ns_heymo_credit_bot_heymo_session_a1b2c3d4_e5f6_7890_ghij_1234567890ab',
          debugEnabled: true,
          maxDocumentsRetrieved: 5,
          maxMessagesFromHistory: 5
        }
      },
      {
        id: 'z9y8x7w6v5u4t3s2r1q0p9o8',
        state: DatasetRunState.COMPLETED,
        startTime: '2024-06-18T09:45:00Z',
        endTime: '2024-06-18T10:12:30Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: '9z8y7x6w5v4u3t2s1r0q9p8o',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.4,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt35-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-3.5-turbo',
            provider: AiEngineProvider.AzureOpenAIService,
            // @ts-ignore
            testDeletedAttribute: 'deleted attr'
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given a chat history and the latest user question which might reference context in the chat history, formulate a standalone question which can be understood without the chat history. Do NOT answer the question, just reformulate it if needed and otherwise return it as is.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.4,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt35-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-3.5-turbo',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template: `## General Context
You are “Hey Mo”, a chatbot designed to provide concise and informative responses to questions from Crédit Mutuel Arkea assistants in a support structure specialized in credit-related topics (personal loans, mortgages, guarantees, insurance, customers relations, etc.).
Your primary role is to assist assistants in finding the appropriate information and resources available in the internal FAQs and documentation related to credit in order to answer questions they have from counselors of Crédit Mutuel de Bretagne (CMB) or Credit Mutuel du Sud Ouest (CMSO) on a helpline.
You specialize in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.) and your responses should remain strictly within this scope. You should never make any commercial propositions, advise on banking transactions or rates, or make assumptions about client\'s financial situations.
## Style of the response
Your tone should be empathetic, informative, empathetic, polite, and formal, always using "vous" to address assistants.
Responses should be concise, approximately 500 characters, and employ bullet points when appropriate. Use simple, accessible language.
## Guidelines
The Documents below corresponds to document extracts that have been retrieved according to the question posed by the chatbot user, indicating that some of them may not be as useful as others.
## Instructions
Based on this context, answer in {{locale}} language the question below.
If the question asked is not understandable, ask the user to reformulate it.
If a question goes beyond the scope of the documentation provided or outside the credit domain simply answer with "{{ no_answer }}"
## Verification Steps
Before responding, wait a minute, breath and then consider:
- Can you provide a confident answer based on the available documentation?
- Does the documentation precisely align with the question posed?
If uncertain or if the topic is not covered in the documentation, answer with "{{ no_answer }}"
## Documents
Use the following documents to answer the question:
{{context}}

## Question
{{question}}`
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: 'z9y8x7w6-v5u4-t3s2-r1q0-p9o8n7m6l5k4',
          indexName: 'ns_heymo_credit_bot_heymo_session_z9y8x7w6_v5u4_t3s2_r1q0_p9o8n7m6l5k4',
          debugEnabled: false,
          maxDocumentsRetrieved: 3,
          maxMessagesFromHistory: 10
        }
      },
      {
        id: 'n1m2b3v4c5x6z7l8k9j0h1g2',
        state: DatasetRunState.COMPLETED,
        startTime: '2024-05-20T14:30:00Z',
        endTime: '2024-05-20T14:55:00Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: 'f1e2d3c4b5a6g7h8i9j0k1l2',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.6,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given a chat history and the latest user question which might reference context in the chat history, formulate a standalone question which can be understood without the chat history. Do NOT answer the question, just reformulate it if needed and otherwise return it as is.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.6,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## General Context\nYou are “Hey Mo”, a chatbot designed to provide concise and informative responses to questions from Crédit Mutuel Arkea assistants in a support structure specialized in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.).\nYour primary role is to assist assistants in finding the appropriate information and resources available in the internal FAQ and documentation related to credit in order to answer questions they have from counselors of Crédit Mutuel de Bretagne (CMB) or Credit Mutuel du Sud Ouest (CMSO) on a helpline. \nYou specialize in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.) and your responses should remain strictly within this scope. You should never make any commercial propositions, advise on banking transactions or rates, or make assumptions about client\'s financial situations.\n\n## Style of the response\nYour tone should be empathetic, informative, polite, and formal, always using "vous" to address assistants.\nResponses should be concise, approximately 250 characters, and employ bullet points when appropriate. Use simple, accessible language.\n\n## Guidelines\nThe Documents below corresponds to document extracts that have been retrieved according to the question posed, indicating that some of them may not be as useful as others. \n\n## Instructions\nBased on this context, answer in {{locale}} the question below.\nIf the question asked is not understandable, ask the user to reformulate it.\nIf a question goes beyond the scope of the documentation provided or outside the credit domain simply answer with "{{ no_answer }}"\n\n## Verification Steps\nBefore responding, consider:\n- Can you provide a confident answer based on the available documentation?\n- Does the documentation align with the question posed?\n\nIf uncertain or if the topic is not covered in the documentation, answer with "{{ no_answer }}"\n\n## Documents\nUse the following documents to answer the question:\n{{context}}\n\n## Question\n{{question}}'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: 'n1m2b3v4-c5x6-z7l8-k9j0-h1g2f3d4c5b6',
          indexName: 'ns_heymo_credit_bot_heymo_session_n1m2b3v4_c5x6_z7l8_k9j0_h1g2f3d4c5b6',
          debugEnabled: false,
          maxDocumentsRetrieved: 2,
          maxMessagesFromHistory: 3
        }
      },

      {
        id: '1b2c323456789ad4aabb1122',
        state: DatasetRunState.COMPLETED,
        startTime: '2024-06-25T17:01:22Z',
        endTime: '2024-06-25T17:24:54Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: '68500d9c6c2d73676cdab6b4',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.5,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given a chat history and the latest user question which might reference context in the chat history, formulate a standalone question which can be understood without the chat history. Do NOT answer the question, just reformulate it if needed and otherwise return it as is.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.5,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template: `## General Context
You are “Hey Mo”, a nice chatbot designed to provide concise and informative responses to questions from Crédit Mutuel Arkea assistants in a support structure specialized in credit-related topics (personal loans, mortgages, guarantees, insurance, customers relations, etc.).
Your primary role is to assist assistants in finding the appropriate information and resources available in the internal FAQs and documentation related to credit in order to answer questions they have from counselors of Crédit Mutuel de Bretagne (CMB) or Credit Mutuel du Sud Ouest (CMSO) on a helpline.
You specialize in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.) and your responses should remain strictly within this scope. You should never make any commercial propositions, advise on banking transactions or rates, or make assumptions about client\'s financial situations.
## Style of the response
Your tone should be empathetic, informative, polite, and formal, always using "vous" to address assistants.
Responses should be concise, approximately 250 characters, and employ bullet points when appropriate. Use simple, accessible language.
## Guidelines
The Documents below corresponds to document extracts that have been retrieved according to the question posed, indicating that some of them may not be as useful as others.
## Instructions
Based on this context, answer in {{locale}} the question below.
If the question asked is not understandable, ask the user to reformulate it.
If a question goes beyond the scope of the documentation provided or outside the credit domain simply answer with "{{ no_answer }}"
## Verification Steps
Before responding, wait a minute, breath and then consider:
- Can you provide a confident answer based on the available documentation?
- Does the documentation align with the question posed?
If uncertain or if the topic is not covered in the documentation, answer with "{{ no_answer }}"
## Documents
Use the following documents to answer the question:
{{context}}
## Question
{{question}}`
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: '68d1f943-685e-4618-83fd-9ab2ff0dcd98',
          indexName: 'ns_heymo_credit_bot_heymo_session_68d1f943_685e_4618_83fd_9ab2ff0dcd98',
          debugEnabled: false,
          maxDocumentsRetrieved: 3,
          maxMessagesFromHistory: 7
        }
      },
      // --- 5 nouveaux runs plus anciens ---
      {
        id: 'k5l6m7n8o9p0q1r2s3t4u5v6',
        state: DatasetRunState.COMPLETED,
        startTime: '2024-05-10T08:15:00Z',
        endTime: '2024-05-10T08:40:00Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: 'w7x8y9z0a1b2c3d4e5f6g7h8',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.2,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given a chat history and the latest user question, rephrase the question to be self-contained, without any reference to previous messages. If the question is already clear, return it unchanged.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.8,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## General Context\nYou are “Hey Mo”, a chatbot specialized in credit-related topics for Crédit Mutuel Arkea assistants. Your answers must be based only on the provided documents.\n\n## Style of the response\nUse a formal and empathetic tone. Keep answers under 300 characters. Use bullet points for clarity.\n\n## Instructions\nAnswer the question in {{locale}} using only the following documents:\n{{context}}\n\nIf the question is unclear or outside the credit domain, respond with "{{ no_answer }}".\n\n## Question\n{{question}}'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: 'k5l6m7n8-o9p0-q1r2-s3t4-u5v6w7x8y9z0',
          indexName: 'ns_heymo_credit_bot_heymo_session_k5l6m7n8_o9p0_q1r2_s3t4_u5v6w7x8y9z0',
          debugEnabled: true,
          maxDocumentsRetrieved: 4,
          maxMessagesFromHistory: 6
        }
      },
      {
        id: 'w1x2y3z4a5b6c7d8e9f0g1h2',
        state: DatasetRunState.COMPLETED,
        startTime: '2024-04-25T11:30:00Z',
        endTime: '2024-04-25T11:55:00Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: 'i3j4k5l6m7n8o9p0q1r2s3t4',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.5,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt35-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-3.5-turbo',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Reformulate the latest user question so it can be understood without the chat history. If the question is already standalone, return it as is.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.3,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt35-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-3.5-turbo',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## Role\nYou are a credit support chatbot for Crédit Mutuel Arkea assistants. Only answer questions related to credit (loans, mortgages, guarantees, insurance).\n\n## Response Style\nBe concise, formal, and empathetic. Use bullet points for lists.\n\n## Documents\n{{context}}\n\n## Question\n{{question}}\n\nIf the question is not about credit or is unclear, respond with "{{ no_answer }}".'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: 'w1x2y3z4-a5b6-c7d8-e9f0-g1h2i3j4k5l6',
          indexName: 'ns_heymo_credit_bot_heymo_session_w1x2y3z4_a5b6_c7d8_e9f0_g1h2i3j4k5l6',
          debugEnabled: false,
          maxDocumentsRetrieved: 3,
          maxMessagesFromHistory: 4
        }
      },
      {
        id: 'i9j0k1l2m3n4o5p6q7r8s9t0',
        state: DatasetRunState.COMPLETED,
        startTime: '2024-04-15T13:00:00Z',
        endTime: '2024-04-15T13:25:00Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: 'u1v2w3x4y5z6a7b8c9d0e1f2',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.4,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given the chat history, extract the latest user question and ensure it is self-contained. If it references previous messages, rephrase it to be independent.'
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## Context\nYou are a chatbot for Crédit Mutuel Arkea assistants, specializing in credit-related topics. Your answers must be based on the provided documents.\n\n## Guidelines\n- Answer in {{locale}}.\n- If the question is unclear or outside the credit domain, respond with "{{ no_answer }}".\n- Use a formal and empathetic tone.\n\n## Documents\n{{context}}\n\n## Question\n{{question}}'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: 'i9j0k1l2-m3n4-o5p6-q7r8-s9t0u1v2w3x4',
          indexName: 'ns_heymo_credit_bot_heymo_session_i9j0k1l2_m3n4_o5p6_q7r8_s9t0u1v2w3x4',
          debugEnabled: true,
          maxDocumentsRetrieved: 5,
          maxMessagesFromHistory: 8
        }
      },
      {
        id: 'u3v4w5x6y7z8a9b0c1d2e3f4',
        state: DatasetRunState.COMPLETED,
        startTime: '2024-04-05T16:45:00Z',
        endTime: '2024-04-05T17:10:00Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: 'g5h6i7j8k9l0m1n2o3p4q5r6',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.6,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt35-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-3.5-turbo',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template: 'Rephrase the latest user question to be self-contained. If it is already clear, return it unchanged.'
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## Instructions\nYou are a credit support chatbot. Only answer questions related to credit (loans, mortgages, guarantees, insurance).\n\n## Response Style\n- Be concise and formal.\n- Use bullet points for lists.\n- Answer in {{locale}}.\n\n## Documents\n{{context}}\n\n## Question\n{{question}}\n\nIf the question is unclear or outside the credit domain, respond with "{{ no_answer }}".'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: 'u3v4w5x6-y7z8-a9b0-c1d2-e3f4g5h6i7j8',
          indexName: 'ns_heymo_credit_bot_heymo_session_u3v4w5x6_y7z8_a9b0_c1d2_e3f4g5h6i7j8',
          debugEnabled: false,
          maxDocumentsRetrieved: 2,
          maxMessagesFromHistory: 5
        }
      },
      {
        id: 'g7h8i9j0k1l2m3n4o5p6q7r8',
        state: DatasetRunState.COMPLETED,
        startTime: '2024-03-20T09:20:00Z',
        endTime: '2024-03-20T09:45:00Z',
        startedBy: 'LS661',
        settingsSnapshot: {
          id: 's9t0u1v2w3x4y5z6a7b8c9d0',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.3,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given the chat history, ensure the latest user question is self-contained. If it references previous messages, rephrase it to be independent.'
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## Context\nYou are a chatbot for Crédit Mutuel Arkea assistants, specializing in credit-related topics. Your answers must be based on the provided documents.\n\n## Guidelines\n- Answer in {{locale}}.\n- If the question is unclear or outside the credit domain, respond with "{{ no_answer }}".\n- Use a formal and empathetic tone.\n\n## Documents\n{{context}}\n\n## Question\n{{question}}'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: 'g7h8i9j0-k1l2-m3n4-o5p6-q7r8s9t0u1v2',
          indexName: 'ns_heymo_credit_bot_heymo_session_g7h8i9j0_k1l2_m3n4_o5p6_q7r8s9t0u1v2',
          debugEnabled: true,
          maxDocumentsRetrieved: 4,
          maxMessagesFromHistory: 7
        }
      }
    ]
  },
  {
    id: 'nopqr1s2t3u4v5w6x7y8z9',
    name: 'Crédits à la consommation',
    description: 'Prêts personnels, crédits renouvelables, taux, remboursement',
    createdAt: '2024-06-01T10:00:00Z',
    createdBy: 'LS661',
    updatedAt: null,
    updatedBy: null,
    questions: [
      {
        id: 'g7h8i9j0-k1l2-3m4n-5o6p-7q8r9s0t1u2v',
        question: 'Quelle est la différence entre un prêt personnel et un crédit renouvelable ?',
        groundTruth:
          'Un prêt personnel est un crédit affecté ou non à un projet précis, avec un montant, une durée et des mensualités fixes. Le crédit renouvelable, quant à lui, met à votre disposition une réserve d’argent que vous pouvez utiliser librement, avec des mensualités variables selon le montant utilisé. Le taux d’intérêt est généralement plus élevé pour un crédit renouvelable.'
      },
      {
        id: 'h8i9j0k1-l2m3-4n5o-6p7q-8r9s0t1u2v3w',
        question: 'Puis-je rembourser mon crédit à la consommation par anticipation ?',
        groundTruth:
          'Oui, vous avez le droit de rembourser votre crédit à la consommation par anticipation, partiellement ou totalement, à tout moment. Votre banque ne peut vous facturer de frais ou pénalités pour un remboursement anticipé, sauf si le montant remboursé est inférieur à 10 000 € sur une période de 12 mois.'
      }
    ],
    runs: [
      {
        id: '1121234561b2c3789ad4aabb',
        state: DatasetRunState.CANCELLED,
        startTime: '2024-06-01T12:00:00Z',
        endTime: null,
        startedBy: 'LS661',
        settingsSnapshot: {
          id: '68500d9c6c2d73676cdab6b5',
          namespace: 'heymo-credit',
          botId: 'heymo',
          enabled: true,
          questionCondensingLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.6,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionCondensingPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              'Given a chat history and the latest user question which might reference context in the chat history, formulate a standalone question which can be understood without the chat history. Do NOT answer the question, just reformulate it if needed and otherwise return it as is.'
          },
          questionAnsweringLlmSetting: {
            apiKey: '12345678910111213141516',
            temperature: 0.6,
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-tock-bot-gpt4o-prod-swct',
            apiVersion: '2023-09-01-preview',
            model: 'gpt-4o',
            provider: AiEngineProvider.AzureOpenAIService
          },
          questionAnsweringPrompt: {
            formatter: PromptDefinitionFormatter.jinja2,
            template:
              '## General Context\nYou are “Hey Mo”, a chatbot designed to provide concise and informative responses to questions from Crédit Mutuel Arkea assistants in a support structure specialized in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.).\nYour primary role is to assist assistants in finding the appropriate information and resources available in the internal FAQ and documentation related to credit in order to answer questions they have from counselors of Crédit Mutuel de Bretagne (CMB) or Credit Mutuel du Sud Ouest (CMSO) on a helpline. \nYou specialize in credit-related topics (personal loans, mortgages, guarantees, insurance, etc.) and your responses should remain strictly within this scope. You should never make any commercial propositions, advise on banking transactions or rates, or make assumptions about client\'s financial situations.\n\n## Style of the response\nYour tone should be empathetic, informative, polite, and formal, always using "vous" to address assistants.\nResponses should be concise, approximately 250 characters, and employ bullet points when appropriate. Use simple, accessible language.\n\n## Guidelines\nThe Documents below corresponds to document extracts that have been retrieved according to the question posed, indicating that some of them may not be as useful as others. \n\n## Instructions\nBased on this context, answer in {{locale}} the question below.\nIf the question asked is not understandable, ask the user to reformulate it.\nIf a question goes beyond the scope of the documentation provided or outside the credit domain simply answer with "{{ no_answer }}"\n\n## Verification Steps\nBefore responding, consider:\n- Can you provide a confident answer based on the available documentation?\n- Does the documentation align with the question posed?\n\nIf uncertain or if the topic is not covered in the documentation, answer with "{{ no_answer }}"\n\n## Documents\nUse the following documents to answer the question:\n{{context}}\n\n## Question\n{{question}}'
          },
          emSetting: {
            apiKey: '12345678910111213141516',
            apiBase: 'https://apim-ad06-prod-openai-frct.azure-api.net/nolb',
            deploymentName: 'cd-di01-ada-prod-swct',
            apiVersion: '2024-03-01-preview',
            model: 'text-embedding-ada-002',
            provider: AiEngineProvider.AzureOpenAIService
          },
          indexSessionId: '68d1f943-685e-4618-83fd-9ab2ff0dcd99',
          indexName: 'ns_heymo_credit_bot_heymo_session_68d1f943_685e_4618_83fd_9ab2ff0dcd99',
          debugEnabled: true,
          maxDocumentsRetrieved: 5,
          maxMessagesFromHistory: 4
        }
      }
    ]
  },
  {
    id: 'klmnop7q8r9s0t1u2v3w4x5',
    name: 'Garanties et cautions',
    description: 'Types de garanties, cautions personnelles, hypothèques',
    createdAt: '2024-06-01T10:00:00Z',
    createdBy: 'LS661',
    updatedAt: null,
    updatedBy: null,
    questions: [
      {
        id: 'i9j0k1l2-m3n4-5o6p-7q8r-9s0t1u2v3w4x',
        question: 'Quelle est la différence entre une caution simple et une caution solidaire ?',
        groundTruth:
          'Une caution simple permet au créancier de se retourner contre la caution uniquement après avoir épuisé tous les recours contre le débiteur principal. Une caution solidaire, en revanche, engage la caution dès le premier impayé, sans que le créancier ait à poursuivre d’abord le débiteur principal. La caution solidaire est donc plus engageante pour le cautionnaire.'
      },
      {
        id: 'j0k1l2m3-n4o5-6p7q-8r9s-0t1u2v3w4x5y',
        question: 'Dans quels cas une hypothèque est-elle obligatoire pour un prêt immobilier ?',
        groundTruth:
          'Une hypothèque n’est pas systématiquement obligatoire pour un prêt immobilier. Elle est généralement demandée par la banque pour sécuriser un prêt de montant élevé ou si votre profil emprunteur présente un risque (revenus irréguliers, apport insuffisant, etc.). Certaines banques peuvent aussi exiger une hypothèque pour les prêts sur une longue durée ou pour les résidences secondaires.'
      }
    ],
    runs: []
  }
];
