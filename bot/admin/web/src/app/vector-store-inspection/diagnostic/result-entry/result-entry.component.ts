import { Component, inject, Input } from '@angular/core';

import { SearchResultChunk } from '../../models/vector-store-inspection.models';
import { VectorStoreInspectionStateService } from '../../services/vector-store-inspection-state.service';

/**
 * One chunk row in the diagnostic results, expandable to show its content.
 *
 * A null rank in a channel is information rather than missing data: it means
 * that channel did not return the chunk at all, which is often the answer the
 * user came for.
 */
@Component({
  selector: 'tock-vector-store-result-entry',
  templateUrl: './result-entry.component.html',
  styleUrl: './result-entry.component.scss',
  standalone: false
})
export class ResultEntryComponent {
  @Input() result!: SearchResultChunk;
  @Input() usesVector: boolean = false;
  @Input() usesFts: boolean = false;
  @Input() isHybrid: boolean = false;
  @Input() showCompressor: boolean = false;

  displayContent: boolean = false;

  private readonly state = inject(VectorStoreInspectionStateService);

  switchContentDetail(): void {
    this.displayContent = !this.displayContent;
  }

  togglePin(): void {
    this.state.togglePin(this.result.chunkId);
  }

  get pinned(): boolean {
    return this.state.isPinned(this.result.chunkId);
  }

  get canPin(): boolean {
    return this.state.canPin(this.result.chunkId);
  }

  get outcomeStatus(): string {
    switch (this.result.outcome) {
      case 'kept':
        return 'success';
      case 'filled_below_threshold':
        // Survived despite an insufficient score, not on merit: worth its own
        // colour so it is not read as a plain success.
        return 'info';
      case 'not_retrieved':
        return 'danger';
      default:
        return 'warning';
    }
  }
}
