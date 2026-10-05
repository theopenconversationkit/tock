import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';
import { of } from 'rxjs';

import { SentenceReviewRequestComponent } from './sentence-review-request.component';
import { TestSharedModule } from '../../../test-shared.module';
import { StateService } from '../../../../core-nlp/state.service';

describe('SentenceReviewRequestComponent', () => {
  let component: SentenceReviewRequestComponent;
  let fixture: ComponentFixture<SentenceReviewRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SentenceReviewRequestComponent],
      imports: [TestSharedModule],
      providers: [
        { provide: NbDialogRef, useValue: { close: () => {} } },
        { provide: StateService, useValue: { currentIntentsCategories: of([]) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SentenceReviewRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
