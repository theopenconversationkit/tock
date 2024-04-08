import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentencesGenerationOptionsComponent } from './sentences-generation-options.component';

describe('SentencesGenerationOptionsComponent', () => {
  let component: SentencesGenerationOptionsComponent;
  let fixture: ComponentFixture<SentencesGenerationOptionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SentencesGenerationOptionsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SentencesGenerationOptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
