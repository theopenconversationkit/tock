import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestDialogComponent } from './test-dialog.component';

describe('TestDialogComponent', () => {
  let component: TestDialogComponent;
  let fixture: ComponentFixture<TestDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TestDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
