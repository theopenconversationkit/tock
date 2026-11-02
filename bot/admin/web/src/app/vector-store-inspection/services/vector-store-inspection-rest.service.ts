import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
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
import { VectorStoreInspectionService } from './vector-store-inspection.service';

/**
 * REST implementation of the inspection service.
 *
 * Talks to the admin server routes under
 * /gen-ai/bots/:botId/vector-store/*, which relay to the orchestrator through
 * orchestrator-client. The admin server resolves the vector store, embedding
 * and compressor settings from Mongo and injects them into the orchestrator
 * call, so the studio never sends credentials — the request bodies here are
 * deliberately lighter than the orchestrator contract.
 *
 * botId is the current application name: GenAIVerticle passes app.namespace and
 * app.name to every Gen AI service, and app.name is the botId on the backend.
 *
 */
@Injectable()
export class VectorStoreInspectionRestService extends VectorStoreInspectionService {
  private readonly rest = inject(RestService);
  private readonly state = inject(StateService);

  constructor() {
    super();
  }

  private get botId(): string {
    return this.state.currentApplication.name;
  }

  private baseUrl(path: string): string {
    return `/gen-ai/bots/${this.botId}/vector-store${path}`;
  }

  getCapabilities(): Observable<VectorStoreCapabilities> {
    // The provider is resolved server side from the bot's vector store
    // configuration, so no identifier is passed here.
    return this.rest.get<VectorStoreCapabilities>(this.baseUrl('/capabilities'), (capabilities: VectorStoreCapabilities) => capabilities);
  }

  getIndexes(): Observable<IndexListResponse> {
    return this.rest.get<IndexListResponse>(this.baseUrl('/indexes'), (response: IndexListResponse) => response);
  }

  getDocuments(request: DocumentsRequest): Observable<DocumentsResponse> {
    return this.rest.post<DocumentsRequest, DocumentsResponse>(
      this.baseUrl('/documents'),
      request,
      (response: DocumentsResponse) => response
    );
  }

  condense(request: CondenseRequest): Observable<CondenseResponse> {
    return this.rest.post<CondenseRequest, CondenseResponse>(this.baseUrl('/condense'), request, (response: CondenseResponse) => response);
  }

  search(request: SearchRequest): Observable<SearchResponse> {
    return this.rest.post<SearchRequest, SearchResponse>(this.baseUrl('/search'), request, (response: SearchResponse) => response);
  }

  getRetrievalSettings(): Observable<RetrievalSettings | null> {
    // The bot's effective retrieval settings (search type, max documents
    // retrieved, compressor state). Resolved server side; the compressor
    // endpoint is never part of the payload. Null when no RAG config exists.
    return this.rest.get<RetrievalSettings | null>(this.baseUrl('/retrieval-settings'), (settings: RetrievalSettings) =>
      settings && Object.keys(settings).length ? settings : null
    );
  }
}
