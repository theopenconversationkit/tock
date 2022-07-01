import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { ErrorHelperComponent } from '../error-helper/error-helper.component';
import { FormControlComponent } from './form-control.component';

@Component({
  template: `<tock-form-control><small>Hello world</small></tock-form-control>`
})
export class TestComponent {}

describe('FormControlComponent', () => {
  let component: FormControlComponent;
  let fixture: ComponentFixture<FormControlComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FormControlComponent, TestComponent, ErrorHelperComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FormControlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display label if is defined', () => {
    component.label = undefined;
    fixture.detectChanges();
    let labelElement = fixture.debugElement.query(By.css('label'));

    expect(labelElement).toBeFalsy();

    component.label = 'Title';
    fixture.detectChanges();
    labelElement = fixture.debugElement.query(By.css('label'));

    expect(labelElement).toBeTruthy();
    expect(labelElement.nativeElement.textContent.trim()).toBe(component.label);
  });

  it('should display asterisk and the label must have the "required" class if the control is required', () => {
    component.label = 'Title';
    fixture.detectChanges();
    const labelElement: HTMLElement = fixture.debugElement.query(By.css('label')).nativeElement;

    component.required = false;
    fixture.detectChanges();
    let abbrElement = labelElement.children[0];

    expect(labelElement).not.toHaveClass('required');
    expect(abbrElement).toBeFalsy();

    component.required = true;
    fixture.detectChanges();
    abbrElement = labelElement.children[0];

    expect(labelElement).toHaveClass('required');
    expect(abbrElement).toBeTruthy();
    expect(abbrElement.textContent.trim()).toBe('*');
  });

  it('should display error component if show error is true', () => {
    component.showError = false;
    fixture.detectChanges();
    let helperElement = fixture.debugElement.query(By.css('tock-error-helper'));

    expect(helperElement).toBeFalsy();

    component.showError = true;
    fixture.detectChanges();
    helperElement = fixture.debugElement.query(By.css('tock-error-helper'));

    expect(helperElement.nativeElement).toBeTruthy();
  });

  it('should display form control in column', () => {
    const element: HTMLElement = fixture.debugElement.query(By.css('div')).nativeElement;

    expect(element).toHaveClass('flex-column');
  });

  it('should render ng content nodes', () => {
    const testFixture = TestBed.createComponent(TestComponent);
    const content: HTMLElement = testFixture.debugElement.query(By.css('small')).nativeElement;

    expect(content).toBeTruthy();
    expect(content.textContent.trim().toLowerCase()).toBe('hello world');
  });
});
