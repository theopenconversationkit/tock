import {
  AzureOpenAiApiVersionsList,
  EnginesConfiguration,
  AiEngineProvider,
  OllamaLlmModelsList,
  OpenAIModelsList
} from '../../../shared/model/ai-settings';

export const DefaultPrompt: string = `# Sentences generation instructions

## Task description

Given the base sentences provided below, generate a list of {{nb_sentences}} unique sentences that convey the same meaning but vary in their presentation. Ensure that all generated sentences remain intelligible and their meaning can be easily understood, despite the variations introduced.

## Variations

The variations should reflect a diverse range of alterations, including but not limited to:

{% if options.spelling_mistakes %}- Spelling Mistakes: Introduce common and uncommon spelling errors that do not hinder the overall comprehension of the sentence.{% endif %}
{% if options.sms_language %}- Incorporation of Non-Standard Language Features: Where appropriate, use features like onomatopoeia, mimetic words, or linguistic innovations unique to digital communication.{% endif %}
{% if options.abbreviated_language %}- Abbreviations and DM (Direct Message) Language: Transform parts of the sentence using popular text messaging abbreviations, internet slang, and shorthand commonly found in online and informal communication.{% endif %}

(if nothing is listed in this 'Variations' entry, find some)

## Generated sentences language

Answer in '{{locale}}' (language locale).

## Format

{{format_instructions}}

## Base sentences

{% for sentence in sentences %}
- {{sentence}}{% endfor %}

## Generated sentences
`;

export const EngineConfigurations: EnginesConfiguration[] = [
  {
    label: 'OpenAi',
    key: AiEngineProvider.OpenAI,
    params: [
      { key: 'apiKey', label: 'Api key', type: 'obfuscated', confirmExport: true },
      { key: 'baseUrl', label: 'Base url', type: 'text', defaultValue: 'https://api.openai.com/v1' },
      { key: 'model', label: 'Model name', type: 'openlist', source: OpenAIModelsList },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth', min: 0, max: 1, step: 0.05 },
      { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
    ]
  },
  {
    label: 'Azure OpenAi',
    key: AiEngineProvider.AzureOpenAIService,
    params: [
      { key: 'apiKey', label: 'Api key', type: 'obfuscated', confirmExport: true },
      { key: 'apiVersion', label: 'Api version', type: 'openlist', source: AzureOpenAiApiVersionsList },
      { key: 'deploymentName', label: 'Deployment name', type: 'text' },
      { key: 'model', label: 'Model name', type: 'openlist', source: OpenAIModelsList },
      { key: 'apiBase', label: 'Base url', type: 'obfuscated' },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth', min: 0, max: 1, step: 0.05 },
      { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
    ]
  },
  {
    label: 'Ollama',
    key: AiEngineProvider.Ollama,
    params: [
      { key: 'baseUrl', label: 'Base url', type: 'text', defaultValue: 'http://localhost:11434' },
      { key: 'model', label: 'Model', type: 'openlist', source: OllamaLlmModelsList, defaultValue: 'llama2' },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth', min: 0, max: 1, step: 0.05, defaultValue: 0.7 },
      { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
    ]
  }
];
