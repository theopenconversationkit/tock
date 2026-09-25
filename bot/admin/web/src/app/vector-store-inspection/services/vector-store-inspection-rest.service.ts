import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { CompressorSettings } from '../../configuration/compressor-settings/models/compressor-settings';
import {
  CondenseRequest,
  CondenseResponse,
  DocumentsRequest,
  DocumentsResponse,
  IndexListResponse,
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

  getCompressorSettings(): Observable<CompressorSettings | null> {
    // Existing route, reused as-is. Returns null when no compressor is
    // configured for the bot; the admin route answers an empty body, which the
    // parser maps to null rather than an empty object.
    return this.rest.get<CompressorSettings | null>(
      `/gen-ai/bots/${this.botId}/configuration/document-compressor`,
      (settings: CompressorSettings) => (settings && Object.keys(settings).length ? settings : null)
    );
  }
}
