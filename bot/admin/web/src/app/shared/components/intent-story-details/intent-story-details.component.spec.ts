import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IntentStoryDetailsComponent } from './intent-story-details.component';

describe('IntentStoryDetailsComponent', () => {
  let component: IntentStoryDetailsComponent;
  let fixture: ComponentFixture<IntentStoryDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IntentStoryDetailsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IntentStoryDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
