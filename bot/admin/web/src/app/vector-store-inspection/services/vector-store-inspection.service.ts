import { Observable } from 'rxjs';

import {
  CondenseRequest,
  CondenseResponse,
  DocumentsRequest,
  DocumentsResponse,
  IndexListResponse,
  RetrievalSettings,
  SearchRequest,
  SearchResponse,
  VectorStoreCapabilities
} from '../models/vector-store-inspection.models';
/**
 * API surface for vector store inspection.
 *
 * Declared as an abstract class rather than an interface so it can be used as
 * an Angular injection token. Its implementation calls the admin routes under
 * /gen-ai/bots/:botId/vector-store/*.
 */
export abstract class VectorStoreInspectionService {
  /**
   * Declared capabilities of the store backing the current bot. The UI builds
   * its controls from this rather than hardcoding provider conditions, so that
   * a provider gaining hybrid search needs no frontend change.
   */
  abstract getCapabilities(): Observable<VectorStoreCapabilities>;

  /** Indexes belonging to the current namespace / bot pair. */
  abstract getIndexes(): Observable<IndexListResponse>;

  /** Ingestion report and a page of documents, grouped server side. */
  abstract getDocuments(request: DocumentsRequest): Observable<DocumentsResponse>;

  /**
   * Runs the question condensation step alone. Optional: the diagnostic view
   * works without it, and comparing runs with and without is one of the cases
   * the comparison mode covers.
   */
  abstract condense(request: CondenseRequest): Observable<CondenseResponse>;

  /**
   * Runs a retrieval and returns the full funnel. The query and keywords are
   * always supplied by the caller and never recomputed server side, which is
   * what makes condensation optional and run comparison possible.
   */
  abstract search(request: SearchRequest): Observable<SearchResponse>;

  /**
   * The bot's effective retrieval settings (search type, max documents
   * retrieved, compressor state + thresholds + provider/model), used to seed the
   * diagnostic form and flag divergences. Resolved server side; the compressor
   * endpoint is never exposed. Returns null when the bot has no RAG config.
   */
  abstract getRetrievalSettings(): Observable<RetrievalSettings | null>;
}
