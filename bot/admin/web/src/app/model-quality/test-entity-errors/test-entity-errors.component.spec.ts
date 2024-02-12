import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestEntityErrorsComponent } from './test-entity-errors.component';

describe('TestEntityErrorsComponent', () => {
  let component: TestEntityErrorsComponent;
  let fixture: ComponentFixture<TestEntityErrorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TestEntityErrorsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TestEntityErrorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
