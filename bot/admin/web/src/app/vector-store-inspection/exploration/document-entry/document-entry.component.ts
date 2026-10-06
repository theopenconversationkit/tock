import { Component, inject, Input } from '@angular/core';

import { ChunkId, InspectedChunk, InspectedDocument } from '../../models/vector-store-inspection.models';
import { VectorStoreInspectionStateService } from '../../services/vector-store-inspection-state.service';

/** Below this length a chunk is treated as empty noise rather than content. */
const NEAR_EMPTY_LENGTH = 50;

/**
 * One document row in the exploration list, expandable to show its chunks.
 *
 * The document / chunks hierarchy does not exist in the store — there are only
 * chunks carrying an `id` and a `chunk` of the form `n/N`. The grouping is done
 * server side; this component only renders it.
 */
@Component({
  selector: 'tock-vector-store-document-entry',
  templateUrl: './document-entry.component.html',
  styleUrl: './document-entry.component.scss',
  standalone: false
})
export class DocumentEntryComponent {
  readonly state = inject(VectorStoreInspectionStateService);

  @Input() document!: InspectedDocument;

  displayChunks: boolean = false;

  switchChunksDetail(): void {
    this.displayChunks = !this.displayChunks;
  }

  togglePin(chunk: InspectedChunk): void {
    this.state.togglePin(chunk.chunkId);
  }

  isPinned(chunkId: ChunkId): boolean {
    return this.state.isPinned(chunkId);
  }

  isNearEmpty(chunk: InspectedChunk): boolean {
    return chunk.contentLength < NEAR_EMPTY_LENGTH;
  }

  get hasWebSource(): boolean {
    return !!this.document.source && this.document.source.startsWith('http');
  }
}
