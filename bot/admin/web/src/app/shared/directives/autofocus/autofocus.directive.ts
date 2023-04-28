import { AfterContentInit, Directive, ElementRef, Input } from '@angular/core';

@Directive({
  selector: '[tockAutofocusElement]'
})
export class AutofocusDirective implements AfterContentInit {
  @Input() tockAutofocusElement: boolean | string = true;

  constructor(private host: ElementRef) {}

  ngAfterContentInit(): void {
    if (this.tockAutofocusElement || this.tockAutofocusElement === '') this.host.nativeElement.focus();
  }
}
