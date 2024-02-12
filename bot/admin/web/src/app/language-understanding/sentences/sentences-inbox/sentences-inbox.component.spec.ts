import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentencesInboxComponent } from './sentences-inbox.component';

describe('SentencesInboxComponent', () => {
  let component: SentencesInboxComponent;
  let fixture: ComponentFixture<SentencesInboxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SentencesInboxComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SentencesInboxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
