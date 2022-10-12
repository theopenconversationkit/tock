import { AfterContentInit, Directive, ElementRef, Input } from '@angular/core';

@Directive({
  selector: '[autofocusElement]'
})
export class AutofocusDirective implements AfterContentInit {
  @Input() autofocusElement: boolean | string = true;

  constructor(private host: ElementRef) {}

  ngAfterContentInit(): void {
    if (this.autofocusElement || this.autofocusElement === '') this.host.nativeElement.focus();
  }
}