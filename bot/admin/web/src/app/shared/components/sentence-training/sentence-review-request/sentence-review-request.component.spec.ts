import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentenceReviewRequestComponent } from './sentence-review-request.component';

describe('SentenceReviewRequestComponent', () => {
  let component: SentenceReviewRequestComponent;
  let fixture: ComponentFixture<SentenceReviewRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SentenceReviewRequestComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SentenceReviewRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
