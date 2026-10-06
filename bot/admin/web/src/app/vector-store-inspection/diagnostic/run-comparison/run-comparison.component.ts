import { Component, EventEmitter, Input, Output } from '@angular/core';

import { RunComparison, RunComparisonRow } from '../../models/vector-store-inspection.models';

/**
 * Renders the current run as a delta against a reference run.
 *
 * The comparison is purely client side: the reference lives in the feature
 * state service and is lost when the user leaves the screen. Nothing about it
 * reaches the backend.
 */
@Component({
  selector: 'tock-vector-store-run-comparison',
  templateUrl: './run-comparison.component.html',
  styleUrl: './run-comparison.component.scss',
  standalone: false
})
export class RunComparisonComponent {
  @Input() comparison!: RunComparison;

  @Output() onClear = new EventEmitter<void>();

  displayStable: boolean = false;

  get movedRows(): RunComparisonRow[] {
    return this.comparison.rows.filter((row) => row.delta !== 'stable');
  }

  get stableRows(): RunComparisonRow[] {
    return this.comparison.rows.filter((row) => row.delta === 'stable');
  }

  get visibleRows(): RunComparisonRow[] {
    return this.displayStable ? this.comparison.rows : this.movedRows;
  }

  switchStableDetail(): void {
    this.displayStable = !this.displayStable;
  }

  deltaStatus(row: RunComparisonRow): string {
    switch (row.delta) {
      case 'lost':
        return 'danger';
      case 'gained':
        return 'success';
      default:
        return 'basic';
    }
  }

  /** Signed movement, so the sign carries the direction rather than a label. */
  movement(row: RunComparisonRow): number {
    if (row.rankReference === null || row.rankCurrent === null) return 0;
    return row.rankReference - row.rankCurrent;
  }
}
