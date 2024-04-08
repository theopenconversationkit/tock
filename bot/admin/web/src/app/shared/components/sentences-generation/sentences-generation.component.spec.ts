import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentencesGenerationComponent } from './sentences-generation.component';

describe('SentencesGenerationComponent', () => {
  let component: SentencesGenerationComponent;
  let fixture: ComponentFixture<SentencesGenerationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SentencesGenerationComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SentencesGenerationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
