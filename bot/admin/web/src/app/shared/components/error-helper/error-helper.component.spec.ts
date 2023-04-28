import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, Validators } from '@angular/forms';

import { ErrorHelperComponent } from './error-helper.component';
import { FileValidators } from '../../validators';

describe('ErrorHelperComponent', () => {
  let component: ErrorHelperComponent;
  let fixture: ComponentFixture<ErrorHelperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ErrorHelperComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorHelperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should render required error when the field is incorrect', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const textContent = 'This field is required'.toUpperCase();
    const field = new FormControl('', Validators.required);

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent.toUpperCase().trim()).toBe(textContent);
  });

  it('should not render required error when the field is correct', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const field = new FormControl('test', Validators.required);

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent).toBeFalsy();
  });

  it('should render min value error when the field is incorrect', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const minValue = 5;
    const textContent = `The minimum value is ${minValue}`.toUpperCase();
    const field = new FormControl('2', Validators.min(minValue));

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent.toUpperCase().trim()).toBe(textContent);
  });

  it('should not render min value error when the field is correct', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const minLength = 5;
    const field = new FormControl('8', Validators.min(minLength));

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent).toBeFalsy();
  });

  it('should render max value error when the field is incorrect', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const maxValue = 10;
    const textContent = `The maximum value is ${maxValue}`.toUpperCase();
    const field = new FormControl('50', Validators.max(maxValue));

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent.toUpperCase().trim()).toBe(textContent);
  });

  it('should not render max value error when the field is correct', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const maxValue = 10;
    const field = new FormControl('5', Validators.max(maxValue));

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent).toBeFalsy();
  });

  it('should render min length error when the field is incorrect', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const minLength = 5;
    const textContent = `This field must contain at least ${minLength} characters`.toUpperCase();
    const field = new FormControl('test', Validators.minLength(minLength));

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent.toUpperCase().trim()).toBe(textContent);
  });

  it('should not render min length error when the field is correct', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const minLength = 5;
    const field = new FormControl('test min', Validators.minLength(minLength));

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent).toBeFalsy();
  });

  it('should render max length error when the field is incorrect', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const maxLength = 10;
    const textContent = `This field is limited to ${maxLength} characters`.toUpperCase();
    const field = new FormControl('test with a long text', Validators.maxLength(maxLength));

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent.toUpperCase().trim()).toBe(textContent);
  });

  it('should not render max length error when the field is correct', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const maxLength = 10;
    const textContent = `This field is limited to ${maxLength} characters`.toUpperCase();
    const field = new FormControl('test', Validators.maxLength(maxLength));

    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent).toBeFalsy();
  });

  it('should render files with wrong type error when the field is incorrect', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const field = new FormControl([], FileValidators.mimeTypeSupported(['application/json']));
    const files: File[] = [
      new File(['content'], 'file1.json', { type: 'application/json' }),
      new File(['content'], 'file2.xml', { type: 'application/xml' })
    ];

    field.setValue([...files]);
    component.field = field;
    fixture.detectChanges();
    const listElement: HTMLUListElement = errorElement.querySelector('ul');

    expect(errorElement.textContent.trim()).toMatch(/^The following files are not accepted file types:./);
    expect(listElement.children).toHaveSize(1);
    expect(listElement.children.item(0).textContent.trim()).toBe('file2.xml');
  });

  it('should not render files with wrong type error when the field is correct', () => {
    const errorElement: HTMLElement = fixture.debugElement.nativeElement;
    const field = new FormControl([], FileValidators.mimeTypeSupported(['application/json']));
    const files: File[] = [
      new File(['content'], 'file1.json', { type: 'application/json' }),
      new File(['content'], 'file2.json', { type: 'application/json' })
    ];

    field.setValue([...files]);
    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent).toBeFalsy();
  });
});
