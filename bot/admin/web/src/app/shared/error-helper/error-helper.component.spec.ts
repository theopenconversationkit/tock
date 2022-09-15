import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, Validators } from '@angular/forms';

import { ErrorHelperComponent } from './error-helper.component';
import { FileValidators } from '../validators';

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

  it('should render required error', () => {
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

  it('should render min length error', () => {
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

  it('should render max length error', () => {
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

  it('should render files with wrong type error', () => {
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

    field.setValue([files[0]]);
    component.field = field;
    fixture.detectChanges();

    expect(errorElement.textContent).toBeFalsy();
  });
});
