import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { AutofocusDirective } from './autofocus.directive';

@Component({
  template: `<div>
    <input
      id="focusElement"
      tockAutofocusElement
    />
  </div>`
})
class TestComponent {}

describe('AutofocusDirective', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AutofocusDirective, TestComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should focus the element after initializing the view', () => {
    const inputElement: Element = fixture.debugElement.query(By.css('#focusElement')).nativeElement;

    expect(inputElement).toBe(document.activeElement);
  });
});
