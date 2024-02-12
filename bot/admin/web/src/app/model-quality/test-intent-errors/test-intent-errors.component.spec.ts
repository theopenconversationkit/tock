import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestIntentErrorsComponent } from './test-intent-errors.component';

describe('TestIntentErrorsComponent', () => {
  let component: TestIntentErrorsComponent;
  let fixture: ComponentFixture<TestIntentErrorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TestIntentErrorsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TestIntentErrorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
