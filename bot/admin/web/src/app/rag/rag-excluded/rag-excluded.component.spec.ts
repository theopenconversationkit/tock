import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RagExcludedComponent } from './rag-excluded.component';
import { SentenceTrainingMode } from '../../shared/components/sentence-training/models';

describe('RagExcludedComponent', () => {
  let component: RagExcludedComponent;
  let fixture: ComponentFixture<RagExcludedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RagExcludedComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(RagExcludedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should use ragexcluded sentenceTrainingMode', () => {
    expect(component.mode).toEqual(SentenceTrainingMode.RAGEXCLUDED);
  });
});
