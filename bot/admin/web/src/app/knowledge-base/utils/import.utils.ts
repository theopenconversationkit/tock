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
 * Reading and mapping of the two accepted import shapes.
 *
 * Pure functions, no server knowledge: duplicate detection against existing entries is the
 * service's job. A file matching neither shape is rejected rather than interpreted at best
 * effort, because mixing up a FAQ export with a knowledge base export is a likely mistake
 * and a half successful import is worse than a clear refusal.
 */

import {
  KNOWLEDGE_BASE_EXPORT_FORMAT,
  KnowledgeBaseEntryPayload,
  KnowledgeBaseEntryStatus,
  KnowledgeBaseExportEnvelope,
  KnowledgeBaseImportSource
} from '../models';

/** Minimal shape of a FAQ export row. Declared locally so the module stays independent of the FAQ feature. */
interface RawFaqLocalizedLabel {
  locale?: string;
  label?: string;
  connectorId?: string | null;
}

interface RawFaqEntry {
  _id?: string;
  id?: string;
  title?: string;
  description?: string;
  utterances?: string[];
  tags?: string[];
  enabled?: boolean;
  language?: string;
  answer?: { i18n?: RawFaqLocalizedLabel[] };
  footnotes?: { url?: string | null }[];
}

export class KnowledgeBaseImportFormatError extends Error {
  constructor(public readonly reasonKey: string) {
    super(reasonKey);
  }
}

export interface ParsedImportRow {
  payload: KnowledgeBaseEntryPayload;
  sourceId: string | null;
  issues: string[];
  rejected: boolean;
  faqEnabled: boolean | null;
}

export interface ParsedImportFile {
  source: KnowledgeBaseImportSource;
  locales: string[];
  rows: ParsedImportRow[];
}

/**
 * Detects the shape of a file and maps it. `locale` only applies to a FAQ export; pass the
 * locale the caller settled on after seeing the detected list.
 */
export function parseImportFile(raw: string, locale?: string | null): ParsedImportFile {
  let parsed: unknown;

  try {
    parsed = JSON.parse(raw);
  } catch {
    throw new KnowledgeBaseImportFormatError('knowledge-base.import.error_not_json');
  }

  if (isKnowledgeBaseEnvelope(parsed)) {
    return { source: KnowledgeBaseImportSource.KNOWLEDGE_BASE, locales: [], rows: mapKnowledgeBaseEnvelope(parsed) };
  }

  if (isFaqExport(parsed)) {
    const rows = parsed as RawFaqEntry[];
    return {
      source: KnowledgeBaseImportSource.FAQ,
      locales: collectLocales(rows),
      rows: locale ? rows.map((row) => mapFaqEntry(row, locale)) : []
    };
  }

  throw new KnowledgeBaseImportFormatError('knowledge-base.import.error_unknown_format');
}

// -------------------------------------------------------------------- Detection

function isKnowledgeBaseEnvelope(value: unknown): value is KnowledgeBaseExportEnvelope {
  const envelope = value as KnowledgeBaseExportEnvelope;
  return !!envelope && typeof envelope === 'object' && envelope.format === KNOWLEDGE_BASE_EXPORT_FORMAT && Array.isArray(envelope.entries);
}

/** A FAQ export is a bare array whose rows carry utterances and a localized content. */
function isFaqExport(value: unknown): boolean {
  if (!Array.isArray(value) || !value.length) return false;

  return value.every(
    (row) => !!row && typeof row === 'object' && Array.isArray((row as RawFaqEntry).utterances) && !!(row as RawFaqEntry).answer
  );
}

// -------------------------------------------------------------------- Mapping

function mapKnowledgeBaseEnvelope(envelope: KnowledgeBaseExportEnvelope): ParsedImportRow[] {
  return envelope.entries.map((entry) => {
    const issues: string[] = [];
    const title = (entry.title ?? '').trim();
    const content = (entry.content ?? '').trim();

    if (!title || !content) issues.push('knowledge-base.import.issue_missing_title_or_content');

    return {
      payload: {
        title,
        searchHints: (entry.searchHints ?? []).map((hint) => hint.trim()).filter((hint) => !!hint),
        content,
        sourceUrl: httpUrlOrNull(entry.sourceUrl),
        tags: entry.tags ?? [],
        // Imported entries are always drafts, whatever the source file says.
        status: KnowledgeBaseEntryStatus.DRAFT
      },
      sourceId: entry.sourceId ?? null,
      issues,
      rejected: !title || !content,
      faqEnabled: null
    };
  });
}

/**
 * The first utterance becomes the entry title: it is the natural phrasing, where the FAQ
 * title is only a short internal label. The remaining utterances become matching terms.
 */
function mapFaqEntry(row: RawFaqEntry, locale: string): ParsedImportRow {
  const issues: string[] = [];

  const utterances = (row.utterances ?? []).map((utterance) => (utterance ?? '').trim()).filter((utterance) => !!utterance);
  const title = utterances[0] ?? '';
  const content = pickLocalizedLabel(row, locale);

  if (!title) issues.push('knowledge-base.import.issue_no_utterance');
  if (!content) issues.push('knowledge-base.import.issue_no_content_for_locale');

  const sourceUrl = httpUrlOrNull(row.footnotes?.find((footnote) => httpUrlOrNull(footnote?.url))?.url ?? null);
  if (row.footnotes?.length && !sourceUrl) issues.push('knowledge-base.import.issue_footnote_without_url');

  return {
    payload: {
      title,
      searchHints: utterances.slice(1),
      content: content ?? '',
      sourceUrl,
      tags: row.tags ?? [],
      status: KnowledgeBaseEntryStatus.DRAFT
    },
    sourceId: row._id ?? row.id ?? null,
    issues,
    rejected: !title || !content,
    faqEnabled: typeof row.enabled === 'boolean' ? row.enabled : null
  };
}

/**
 * A FAQ may hold several labels for the same locale, one per interface or connector.
 * The generic one, without connector, is preferred; otherwise the first non empty label wins.
 */
function pickLocalizedLabel(row: RawFaqEntry, locale: string): string | null {
  const labels = (row.answer?.i18n ?? []).filter((label) => label?.locale === locale && !!label.label?.trim());
  if (!labels.length) return null;

  const generic = labels.find((label) => !label.connectorId);
  return (generic ?? labels[0]).label.trim();
}

function collectLocales(rows: RawFaqEntry[]): string[] {
  const locales = new Set<string>();

  rows.forEach((row) => {
    (row.answer?.i18n ?? []).forEach((label) => {
      if (label?.locale && label.label?.trim()) locales.add(label.locale);
    });
    if (row.language) locales.add(row.language);
  });

  return [...locales].sort((a, b) => a.localeCompare(b));
}

function httpUrlOrNull(value: string | null | undefined): string | null {
  const url = (value ?? '').trim();
  return /^https?:\/\/.+/.test(url) ? url : null;
}

/** Matching key for duplicate detection: the title, case and whitespace insensitive. */
export function normalizeTitle(title: string): string {
  return title.trim().toLowerCase().replace(/\s+/g, ' ');
}
