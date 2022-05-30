import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FaqManagementComponent } from './faq-management.component';

describe('FaqManagementComponent', () => {
  let component: FaqManagementComponent;
  let fixture: ComponentFixture<FaqManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqManagementComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FaqManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
