import { Directive, ElementRef } from '@angular/core';

/**
 * Hack: fix https://github.com/akveo/nebular/issues/2723
 */
@Directive({
  selector: '[tock-delay]'
})
export class DelayDirective {
  constructor(el: ElementRef) {
    el.nativeElement.style.display = 'none';
    setTimeout(() => (el.nativeElement.style.display = 'inherit'), 20);
  }
}
