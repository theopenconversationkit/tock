import { Directive, EventEmitter, HostListener, Output } from '@angular/core';

@Directive({
  selector: '[tockMouseWheelListner]'
})
export class MouseWheelListnerDirective {
  @Output() onMouseWheel: EventEmitter<number> = new EventEmitter<number>();

  @HostListener('wheel', ['$event'])
  public onEvent(event) {
    this.onMouseWheel.emit(event);
  }
}
