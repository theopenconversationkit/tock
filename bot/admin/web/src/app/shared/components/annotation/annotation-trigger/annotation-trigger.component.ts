import { Component, Input, inject } from '@angular/core';
import { NbComponentShape, NbComponentSize, NbDialogService } from '@nebular/theme';

import { ActionReport, DialogReport } from '../../../model/dialog-data';
import { AnnotationComponent } from '../annotation.component';

/**
 * Presentational trigger that opens the annotation modal for a given dialog action.
 *
 * It only decides between the "create" and "view" appearance (based on whether the
 * action already carries an annotation) and opens {@link AnnotationComponent}. The
 * modal mutates `actionReport.annotation` in place on save, so no refresh is needed:
 * `hasAnnotation` re-evaluates and the button switches to its "view" state on its own.
 *
 * The appearance inputs (`ghost` / `size` / `shape`) let each host tune prominence to
 * match its surrounding control cluster.
 */
@Component({
    selector: 'tock-annotation-trigger',
    templateUrl: './annotation-trigger.component.html',
    styleUrl: './annotation-trigger.component.scss',
    standalone: false
})
export class AnnotationTriggerComponent {
  @Input() dialogReport: DialogReport;
  @Input() actionReport: ActionReport;

  @Input() ghost = true;
  @Input() size: NbComponentSize = 'small';
  @Input() shape?: NbComponentShape;

  private nbDialogService = inject(NbDialogService);

  get hasAnnotation(): boolean {
    return !!this.actionReport?.annotation;
  }

  open(): void {
    this.nbDialogService.open(AnnotationComponent, {
      context: {
        dialogReport: this.dialogReport,
        actionReport: this.actionReport
      }
    });
  }
}
