import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentenceNewComponent } from './sentence-new.component';

describe('SentenceNewComponent', () => {
  let component: SentenceNewComponent;
  let fixture: ComponentFixture<SentenceNewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SentenceNewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SentenceNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
