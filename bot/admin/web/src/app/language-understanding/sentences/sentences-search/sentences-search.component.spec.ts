import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentencesSearchComponent } from './sentences-search.component';

describe('SentencesSearchComponent', () => {
  let component: SentencesSearchComponent;
  let fixture: ComponentFixture<SentencesSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SentencesSearchComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SentencesSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
