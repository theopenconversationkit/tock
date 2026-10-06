import { Component, EventEmitter, Input, Output, QueryList, ViewChildren } from '@angular/core';
import { NbTooltipDirective } from '@nebular/theme';

import { VectorStoreIndex } from '../../models/vector-store-inspection.models';

/**
 * Index selector shared by the exploration and diagnostic views.
 *
 * Both views select an index the same way, hit the same nb-select traps (the
 * orphaned tooltip on close, the selection dropped when options arrive after
 * the value) and want the same presentation (green check for the current
 * index, ingestion date). Keeping it in one place means fixing those once.
 *
 * The component is presentational: it takes the list and the selection as
 * inputs and emits the chosen index. State lives in the feature state service,
 * which the parent wires.
 */
@Component({
  selector: 'tock-vector-store-index-selector',
  templateUrl: './index-selector.component.html',
  styleUrl: './index-selector.component.scss',
  standalone: false
})
export class IndexSelectorComponent {
  @ViewChildren(NbTooltipDirective) tooltips: QueryList<NbTooltipDirective>;

  @Input() indexes: VectorStoreIndex[] = [];
  @Input() selected: VectorStoreIndex | null = null;
  @Input() label: string = '';
  @Input() placeholder: string = '';

  @Output() indexChange = new EventEmitter<VectorStoreIndex>();

  onSelect(index: VectorStoreIndex): void {
    // Nebular mounts tooltips in a detached overlay; when the option list
    // closes, the option is destroyed without a mouseleave, leaving the
    // tooltip on screen. Closing them by hand on every change is the only
    // reliable fix.
    this.hideTooltips();
    this.indexChange.emit(index);
  }

  hideTooltips(): void {
    this.tooltips?.forEach((tooltip) => tooltip.hide());
  }
}
