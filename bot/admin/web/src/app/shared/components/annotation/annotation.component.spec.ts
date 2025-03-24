import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnnotationComponent } from './annotation.component';

describe('AnnotationComponent', () => {
  let component: AnnotationComponent;
  let fixture: ComponentFixture<AnnotationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnnotationComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(AnnotationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
