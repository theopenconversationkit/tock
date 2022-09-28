import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, Validators } from '@angular/forms';

import { ErrorHelperComponent } from './error-helper.component';

describe('ErrorHelperComponent', () => {
  let component: ErrorHelperComponent;
  let fixture: ComponentFixture<ErrorHelperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ErrorHelperComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorHelperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should render required error if the field has a required error', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const textContent = 'This field is required'.toUpperCase();
    const field = new FormControl('', Validators.required);

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent.toUpperCase().trim()).toBe(textContent);

    field.setValue('test');
    fixture.detectChanges();

    expect(errorElement.textContent).toBeFalsy();
  });

  it('should render min length error if the field has a min length error', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const minLength = 5;
    const textContent = `This field must contain at least ${minLength} characters`.toUpperCase();
    const field = new FormControl('test', Validators.minLength(minLength));

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent.toUpperCase().trim()).toBe(textContent);

    field.setValue('test min');
    fixture.detectChanges();

    expect(errorElement.textContent).toBeFalsy();
  });

  it('should render max length error if the field has a max length error', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const maxLength = 10;
    const textContent = `This field is limited to ${maxLength} characters`.toUpperCase();
    const field = new FormControl('test with a long text', Validators.maxLength(maxLength));

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent.toUpperCase().trim()).toBe(textContent);

    field.setValue('test');
    fixture.detectChanges();

    expect(errorElement.textContent).toBeFalsy();
  });
});
