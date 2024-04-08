import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentencesGenerationListComponent } from './sentences-generation-list.component';

describe('SentencesGenerationListComponent', () => {
  let component: SentencesGenerationListComponent;
  let fixture: ComponentFixture<SentencesGenerationListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SentencesGenerationListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SentencesGenerationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
