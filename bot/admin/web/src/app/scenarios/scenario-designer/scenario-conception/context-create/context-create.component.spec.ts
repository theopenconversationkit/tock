import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbDialogRef } from '@nebular/theme';

import { ContextCreateComponent } from './context-create.component';
import { TestingModule } from '../../../../../testing';

describe('ContextCreateComponent', () => {
  let component: ContextCreateComponent;
  let fixture: ComponentFixture<ContextCreateComponent>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ContextCreateComponent],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {}
        }
      ],
      imports: [TestingModule],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('Should create', () => {
    expect(component).toBeTruthy();
  });

  it('Should format name in snake case on blur', () => {
    let nameinput = fixture.debugElement.query(By.css('[data-testid="name"]'));
    nameinput.nativeElement.value = 'test action';
    nameinput.nativeElement.dispatchEvent(new Event('input'));
    nameinput.nativeElement.dispatchEvent(new Event('blur'));
    fixture.detectChanges();
    expect(component.name.value).toEqual('TEST_ACTION');
  });

  it('Should format name in snake case on save', () => {
    let nameinput = fixture.debugElement.query(By.css('[data-testid="name"]'));
    nameinput.nativeElement.value = 'test action';
    nameinput.nativeElement.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    component.save();
    expect(component.form.value).toEqual({ name: 'TEST_ACTION' });
  });
});
