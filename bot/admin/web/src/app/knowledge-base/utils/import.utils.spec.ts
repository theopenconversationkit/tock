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
import { parseImportFile, KnowledgeBaseImportFormatError } from './import.utils';
import { KnowledgeBaseEntryStatus } from '../models';

describe('Knowledge base import', () => {
  const envelope = (entries: unknown[], version = 1) => JSON.stringify({ format: 'tock-knowledge-base', version, entries });
  it('refuses unknown and future formats', () => {
    for (const raw of ['oops', '{}', '[]', envelope([], 2)])
      expect(() => parseImportFile(raw)).toThrowError(KnowledgeBaseImportFormatError);
  });
  it('imports publications as drafts and preserves the portable source id', () => {
    const row = parseImportFile(
      envelope([
        {
          title: 'Titre',
          content: 'Texte',
          status: 'PUBLISHED',
          sourceId: 'origin',
          sourceUrl: 'https://example.org',
          searchHints: [' autre ']
        }
      ])
    ).rows[0];
    expect(row.payload.status).toBe(KnowledgeBaseEntryStatus.DRAFT);
    expect(row.sourceId).toBe('origin');
    expect(row.payload.searchHints).toEqual(['autre']);
  });
  it('reports malformed rows without crashing the whole file', () => {
    const rows = parseImportFile(envelope([null, { title: 1, content: [], tags: {}, searchHints: [null, 12, 'valid'] }])).rows;
    expect(rows.every((row) => row.rejected)).toBeTrue();
    expect(rows[1].payload.searchHints).toEqual(['valid']);
  });
  it('selects the chosen FAQ locale and generic chat text rather than a voice or connector label', () => {
    const raw = JSON.stringify([
      {
        utterances: ['Question', 'Alternative'],
        enabled: false,
        answer: {
          i18n: [
            { locale: 'en', label: 'English' },
            { locale: 'fr', label: 'Voix', interfaceType: 'voice' },
            { locale: 'fr', label: 'Connecteur', interfaceType: 'textChat', connectorId: 'id' },
            { locale: 'fr', label: 'Texte', interfaceType: 'textChat' }
          ]
        },
        footnotes: [{ url: 'https://example.org' }]
      }
    ]);
    expect(parseImportFile(raw).locales).toEqual(['en', 'fr']);
    const row = parseImportFile(raw, 'fr').rows[0];
    expect(row.payload.content).toBe('Texte');
    expect(row.payload.title).toBe('Question');
    expect(row.payload.searchHints).toEqual(['Alternative']);
    expect(row.faqEnabled).toBeFalse();
    expect(row.payload.sourceUrl).toBe('https://example.org');
    expect(parseImportFile(raw, 'de').rows[0].rejected).toBeTrue();
  });
  it('rejects unsafe source URLs and tolerates malformed FAQ footnotes', () => {
    expect(
      parseImportFile(envelope([{ title: 'Titre', content: 'Texte', sourceUrl: 'javascript:alert(1)' }])).rows[0].payload.sourceUrl
    ).toBeNull();
    const raw = JSON.stringify([
      { utterances: ['Question'], language: 1, answer: { i18n: [{ locale: 'fr', label: 'Texte' }] }, footnotes: {} }
    ]);
    expect(parseImportFile(raw, 'fr').rows[0].payload.sourceUrl).toBeNull();
  });
});
