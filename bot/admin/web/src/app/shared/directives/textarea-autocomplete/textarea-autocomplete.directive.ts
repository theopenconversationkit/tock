import { ChangeDetectorRef, Directive, ElementRef, EventEmitter, Input, Output, Renderer2 } from '@angular/core';
import {
  NbAdjustableConnectedPositionStrategy,
  NbAdjustment,
  NbAutocompleteDirective,
  NbFocusKeyManagerFactoryService,
  NbOptionComponent,
  NbOverlayService,
  NbPosition,
  NbPositionBuilderService,
  NbTriggerStrategyBuilderService
} from '@nebular/theme';
import { takeUntil } from 'rxjs';

@Directive({
  selector: 'textarea[nbAutocomplete]'
})
export class TextareaAutocompleteDirective<T> extends NbAutocompleteDirective<T> {
  @Input() position: 'top' | 'bottom' = 'bottom';
  @Output() onInputValueChange = new EventEmitter<string>();

  constructor(
    protected override hostRef: ElementRef,
    protected override overlay: NbOverlayService,
    protected override cd: ChangeDetectorRef,
    protected override triggerStrategyBuilder: NbTriggerStrategyBuilderService,
    protected override positionBuilder: NbPositionBuilderService,
    protected nbFocusKeyManagerFactoryService: NbFocusKeyManagerFactoryService<NbOptionComponent<T>>,
    protected override renderer: Renderer2
  ) {
    super(hostRef, overlay, cd, triggerStrategyBuilder, positionBuilder, nbFocusKeyManagerFactoryService, renderer);
  }

  protected override handleInputValueUpdate(value: T, focusInput: boolean = false) {
    super.handleInputValueUpdate(value, focusInput);
    this.onInputValueChange.emit(this.hostRef.nativeElement.value);
  }

  protected override createPositionStrategy(): NbAdjustableConnectedPositionStrategy {
    return this.positionBuilder
      .connectedTo(this.customOverlayHost || this.hostRef)
      .position(this.position === 'top' ? NbPosition.TOP : NbPosition.BOTTOM)
      .offset(this.overlayOffset)
      .adjustment(NbAdjustment.VERTICAL);
  }

  updatePosition(): void {
    this.hide();
    this.show();
  }
}
