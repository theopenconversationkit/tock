import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentenceTrainingSentenceComponent } from './sentence-training-sentence.component';

describe('SentenceTrainingSentenceComponent', () => {
  let component: SentenceTrainingSentenceComponent;
  let fixture: ComponentFixture<SentenceTrainingSentenceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SentenceTrainingSentenceComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SentenceTrainingSentenceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
