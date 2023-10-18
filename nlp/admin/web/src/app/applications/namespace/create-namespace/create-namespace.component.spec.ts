import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateNamespaceComponent } from './create-namespace.component';
import { NbDialogRef } from '@nebular/theme';

describe('CreateNamespaceComponent', () => {
  let component: CreateNamespaceComponent;
  let fixture: ComponentFixture<CreateNamespaceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateNamespaceComponent],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {}
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateNamespaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
