import { Component, Input, inject } from '@angular/core';
import { NbComponentShape, NbComponentSize, NbComponentStatus, NbDialogService } from '@nebular/theme';

import { ActionReport, DialogReport } from '../../../model/dialog-data';
import { AnnotationComponent } from '../annotation.component';
import { AnnotationState } from '../annotations';

/**
 * Presentational trigger that opens the annotation modal for a given dialog action.
 *
 * It decides the button appearance from the action's annotation state and opens
 * {@link AnnotationComponent}. The modal mutates `actionReport.annotation` in place on
 * save, so no refresh is needed: the getters re-evaluate and the button restyles itself.
 *
 * Three appearances:
 *  - no annotation → green "create" (prominent, invites annotating);
 *  - open annotation (ANOMALY / REVIEW_NEEDED) → orange, needs attention;
 *  - closed annotation (RESOLVED / WONT_FIX) → neutral grey, handled, no action left.
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

  private readonly openStates = [AnnotationState.ANOMALY, AnnotationState.REVIEW_NEEDED];

  get hasAnnotation(): boolean {
    return !!this.actionReport?.annotation;
  }

  private get isOpenAnnotation(): boolean {
    const state = this.actionReport?.annotation?.state;
    return !!state && this.openStates.includes(state);
  }

  get buttonStatus(): NbComponentStatus {
    if (!this.hasAnnotation) return 'success';
    return this.isOpenAnnotation ? 'warning' : 'basic';
  }

  get buttonIcon(): string {
    return this.hasAnnotation ? 'exclamation-triangle-fill' : 'exclamation-triangle';
  }

  get buttonTooltip(): string {
    return this.hasAnnotation
      ? 'shared.chat-ui-dialog-logger.display-annotation'
      : 'shared.chat-ui-dialog-logger.annotate-exchange';
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
