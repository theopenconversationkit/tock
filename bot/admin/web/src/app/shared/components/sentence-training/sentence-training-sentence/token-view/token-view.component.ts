import { Component, Input, OnDestroy, ViewContainerRef, ViewChild, TemplateRef, Output, EventEmitter } from '@angular/core';
import { Token } from './token.model';
import { Subject, takeUntil } from 'rxjs';
import { FlexibleConnectedPositionStrategyOrigin, Overlay, OverlayRef } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { getContrastYIQ } from '../../../../utils';
import { StateService } from '../../../../../core-nlp/state.service';
import { SentenceTrainingService } from '../../sentence-training.service';

@Component({
  selector: 'tock-token-view',
  templateUrl: './token-view.component.html',
  styleUrls: ['./token-view.component.scss']
})
export class TokenViewComponent implements OnDestroy {
  destroy = new Subject();

  @Input() token: Token;

  @Output() deleteTokenEntity = new EventEmitter();

  @ViewChild('userMenu') userMenu: TemplateRef<any>;

  getContrastYIQ = getContrastYIQ;

  overlayRef: OverlayRef | null;

  constructor(
    private state: StateService,
    private sentenceTrainingService: SentenceTrainingService,
    private overlay: Overlay,
    private viewContainerRef: ViewContainerRef
  ) {
    this.sentenceTrainingService.communication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type === 'documentClick') {
        this.hideTokenMenu();
      }
    });
  }

  delete(token: Token): void {
    this.deleteTokenEntity.emit(token);
  }

  getEntityName(): string {
    return this.token.entity?.qualifiedName(this.state.user);
  }

  displayMenu(event: MouseEvent): void {
    if (this.token.entity) {
      this.sentenceTrainingService.documentClick(event);
      this.displayTokenMenu(event);
    }
  }

  hideTokenMenu(): void {
    if (this.overlayRef) this.overlayRef.detach();
  }

  displayTokenMenu(event: MouseEvent): void {
    event.stopPropagation();

    this.hideTokenMenu();

    const positionStrategy = this.overlay
      .position()
      .flexibleConnectedTo(event.target as FlexibleConnectedPositionStrategyOrigin)
      .withPositions([
        {
          originX: 'start',
          originY: 'bottom',
          overlayX: 'start',
          overlayY: 'top'
        },
        {
          originX: 'start',
          originY: 'center',
          overlayX: 'end',
          overlayY: 'center'
        },
        {
          originX: 'end',
          originY: 'center',
          overlayX: 'start',
          overlayY: 'center'
        }
      ]);

    this.overlayRef = this.overlay.create({
      positionStrategy,
      scrollStrategy: this.overlay.scrollStrategies.reposition()
    });

    this.overlayRef.attach(new TemplatePortal(this.userMenu, this.viewContainerRef));
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
