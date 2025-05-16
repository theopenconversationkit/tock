/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
