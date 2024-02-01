import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IntentQualityComponent } from './intent-quality.component';

describe('IntentQualityComponent', () => {
  let component: IntentQualityComponent;
  let fixture: ComponentFixture<IntentQualityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IntentQualityComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IntentQualityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
