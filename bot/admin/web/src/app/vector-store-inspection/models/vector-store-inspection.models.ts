/**
 * TypeScript mirror of the vector store inspection API contract.
 * See docs/vector-store-inspection-api.md
 *
 * Field names are camelCase: the orchestrator client's Jackson mapper already
 * handles the SNAKE_CASE conversion.
 */

import { VectorDbProvider } from '../../configuration/vector-db-settings/models/providers-configuration';
import { PaginatedResult } from '../../model/nlp';
import { DocumentSearchType } from '../../rag/rag-settings/models/engines-configurations';

/** Chunk identifier as exposed to the model: `{documentId}:{chunk}` */
export type ChunkId = string;

// ---------------------------------------------------------------------------
// Capabilities
// ---------------------------------------------------------------------------

export interface VectorStoreCapabilities {
  provider: VectorDbProvider;
  searchTypes: DocumentSearchType[];
  supportsScores: boolean;
  supportsIndexListing: boolean;
  supportsMetadataFilter: boolean;
  /** e.g. 'hybrid_and_fts_not_implemented' */
  notes: string[];
}

// ---------------------------------------------------------------------------
// Index listing
// ---------------------------------------------------------------------------

export interface VectorStoreIndex {
  indexName: string;
  indexSessionId: string;
  indexDatetime: string;
  documentCount: number;
  chunkCount: number;
  /** Computed by the admin server against the BotRAGConfiguration */
  isCurrent: boolean;
}

export interface IndexListResponse {
  indexes: VectorStoreIndex[];
}

// ---------------------------------------------------------------------------
// Exploration
// ---------------------------------------------------------------------------

export enum AnomalyCode {
  NEAR_EMPTY_CHUNK = 'near_empty_chunk',
  NON_URL_SOURCE = 'non_url_source',
  DUPLICATE_TITLE = 'duplicate_title'
}

export type AnomalySeverity = 'warning' | 'info';

export interface IndexAnomaly {
  code: AnomalyCode;
  count: number;
  severity: AnomalySeverity;
}

export interface IndexStats {
  documentCount: number;
  chunkCount: number;
  chunksPerDocumentAvg: number;
  chunkLengthMedian: number;
  indexDatetime: string;
}

export interface InspectedChunk {
  chunkId: ChunkId;
  /** Raw `n/N` form */
  chunk: string;
  /** Content with the title prefix stripped (get_source_content) */
  content: string;
  contentLength: number;
  metadata: Record<string, unknown>;
}

export interface InspectedDocument {
  documentId: string;
  title: string;
  source: string | null;
  chunkCount: number;
  indexSessionId: string;
  /** Omitted when includeChunks is false */
  chunks?: InspectedChunk[];
}

export interface DocumentsFilter {
  text?: string | null;
  documentId?: string | null;
  anomaly?: AnomalyCode | null;
}

export interface DocumentsRequest {
  indexName: string;
  filter?: DocumentsFilter;
  /** Offset, matching the shared Pagination contract */
  start: number;
  size: number;
  includeStats?: boolean;
  includeChunks?: boolean;
}

/**
 * `rows` holds documents, not chunks: grouping happens server side, so `total`
 * counts groups and pagination stays coherent.
 */
export interface DocumentsResponse extends PaginatedResult<InspectedDocument> {
  stats: IndexStats;
  anomalies: IndexAnomaly[];
}

// ---------------------------------------------------------------------------
// Condensation
// ---------------------------------------------------------------------------

export interface CondenseRequest {
  question: string;
}

export interface CondenseResponse {
  condensedQuestion: string;
  keyWords: string[];
  effectivePrompt: string;
  duration: number;
}

// ---------------------------------------------------------------------------
// Compression
// ---------------------------------------------------------------------------

/**
 * Where the compression stage sits relative to the top-k cut.
 *
 * `afterCut` mirrors the runtime, where the compressor wraps an
 * already-truncated retriever and can therefore only remove documents.
 * `beforeCut` lets the reranker sort the whole fetched window, which is what it
 * would do if the runtime dissociated fetchK from k. The UI labels the latter
 * as not matching production.
 */
export type CompressionStage = 'afterCut' | 'beforeCut';

/**
 * Compressor thresholds as overridden for a single diagnostic run.
 *
 * Not a duplicate of CompressorSetting: that one describes the bot's stored
 * configuration (provider, endpoint, label), which the tool must never change.
 * Only these three knobs are meaningful to tweak while diagnosing, and they
 * are what produce the below_min_score / reranked_out / filled_below_threshold
 * outcomes.
 */
export interface CompressionOverride {
  minScore: number;
  maxDocuments: number;
  /** Absent from the shared CompressorSetting interface, though the settings
   *  form exposes it and the backend reranker honours it. */
  fillToMaxDocuments: boolean;
}

// ---------------------------------------------------------------------------
// Search / diagnostic
// ---------------------------------------------------------------------------

export type FunnelStageStatus = 'applied' | 'skipped' | 'disabled' | 'failed_fallback';

export interface FunnelStage {
  status: FunnelStageStatus;
  count: number | null;
  /** Populated notably on failed_fallback */
  reason?: string | null;
  /** topKCut only */
  discarded?: number;
}

export interface SearchFunnel {
  vector: FunnelStage;
  fts: FunnelStage;
  rrf: FunnelStage;
  topKCut: FunnelStage;
  compression: FunnelStage;
}

/** `null` means "not found by this channel" — information, not missing data. */
export interface ChannelRanks {
  vector: number | null;
  fts: number | null;
  rrf: number | null;
}

export interface ChannelScores {
  vector: number | null;
  fts: number | null;
  rrf: number | null;
  compressor: number | null;
}

/**
 * The middle three only occur when compression ran.
 *
 * `filled_below_threshold` deserves distinct treatment in the UI: such a chunk
 * survived despite an insufficient score, courtesy of fill_to_max_documents,
 * which is not the same as being kept on merit.
 */
export type ChunkOutcome = 'kept' | 'cut_by_top_k' | 'below_min_score' | 'reranked_out' | 'filled_below_threshold' | 'not_retrieved';

export interface SearchResultChunk {
  chunkId: ChunkId;
  documentId: string;
  title: string;
  chunk: string;
  content: string;
  ranks: ChannelRanks;
  scores: ChannelScores;
  outcome: ChunkOutcome;
  pinned: boolean;
  metadata?: Record<string, unknown>;
}

export type PinnedRankStrategy = 'truncated' | 'score_only' | 'exact_rank';

export interface SearchRequest {
  indexName: string;
  searchType: DocumentSearchType;
  /** Raw or condensed question — never recomputed server side */
  query: string;
  /** Required for FULL_TEXT_SEARCH and HYBRID_SEARCH */
  keyWords?: string[];
  /** Candidates pulled from the store */
  fetchK: number;
  /** Survivors after the cut */
  k: number;
  /** False disables the compression stage entirely */
  compressionEnabled: boolean;
  compressionStage: CompressionStage;
  compressionOverride?: CompressionOverride;
  pinnedChunkIds?: ChunkId[];
  pinnedRankStrategy?: PinnedRankStrategy;
}

export interface SearchResponse {
  funnel: SearchFunnel;
  compressionStage: CompressionStage;
  results: SearchResultChunk[];
  duration: number;
}

// ---------------------------------------------------------------------------
// Run comparison — assembled client side, no endpoint
// ---------------------------------------------------------------------------

/** A completed run, kept in memory to serve as a comparison baseline. */
export interface SearchRun {
  request: SearchRequest;
  response: SearchResponse;
  /** Label shown in the comparison banner, e.g. the index short name */
  label: string;
  ranAt: Date;
}

/** Which parameters differ between the reference run and the current one. */
export type RunDiffField = 'index' | 'searchType' | 'query' | 'keyWords' | 'fetchK' | 'k' | 'compression';

/**
 * Why a chunk present in the reference run is missing from the current one.
 * An ingestion problem must only be reported when the current index was
 * explicitly checked. Otherwise the cause remains unknown.
 */
export type AbsenceReason = 'absent_from_index' | 'outside_fetch_k' | 'unknown';

export type RunDelta = 'gained' | 'lost' | 'moved' | 'stable';

export interface RunComparisonRow {
  chunkId: ChunkId;
  title: string;
  chunk: string;
  rankReference: number | null;
  rankCurrent: number | null;
  delta: RunDelta;
  /** Rank movement, used for sorting; 0 when gained, lost or stable */
  magnitude: number;
  /** Only set when delta is 'lost' */
  absenceReason?: AbsenceReason;
  pinned: boolean;
}

export interface RunComparison {
  reference: SearchRun;
  current: SearchRun;
  /** Named in the banner; the more entries, the less interpretable the delta */
  changedFields: RunDiffField[];
  rows: RunComparisonRow[];
  commonInTopK: number;
  lost: number;
  gained: number;
}
