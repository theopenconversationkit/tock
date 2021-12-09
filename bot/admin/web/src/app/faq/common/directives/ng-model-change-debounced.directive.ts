import { Directive, Output, EventEmitter, HostListener, Input, OnDestroy  } from '@angular/core';
import { Subscription } from 'rxjs';
import {NgModel} from '@angular/forms';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';

@Directive({
  selector: '[ngModelChangeDebounced]',
})
export class NgModelChangeDebouncedDirective implements OnDestroy {
  @Output()
  ngModelChangeDebounced = new EventEmitter<any>();
  @Input()
  ngModelChangeDebounceTime = 500; // optional, 500 default

  subscription: Subscription;
  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  constructor(private ngModel: NgModel) {
    this.subscription = this.ngModel.control.valueChanges.pipe(
      skip(1), // skip initial value
      distinctUntilChanged(),
      debounceTime(this.ngModelChangeDebounceTime)
    ).subscribe((value) => this.ngModelChangeDebounced.emit(value));
  }
}
