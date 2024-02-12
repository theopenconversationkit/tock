import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentencesUnknownComponent } from './sentences-unknown.component';

describe('SentencesUnknownComponent', () => {
  let component: SentencesUnknownComponent;
  let fixture: ComponentFixture<SentencesUnknownComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SentencesUnknownComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SentencesUnknownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
