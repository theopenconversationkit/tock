import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentenceTrainingEntryComponent } from './sentence-training-entry.component';

describe('SentenceTrainingListEntryComponent', () => {
  let component: SentenceTrainingEntryComponent;
  let fixture: ComponentFixture<SentenceTrainingEntryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SentenceTrainingEntryComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(SentenceTrainingEntryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
