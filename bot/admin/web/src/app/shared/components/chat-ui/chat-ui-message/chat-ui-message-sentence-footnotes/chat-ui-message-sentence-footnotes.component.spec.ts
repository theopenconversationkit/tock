import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChatUiMessageSentenceFootnotesComponent } from './chat-ui-message-sentence-footnotes.component';

describe('ChatUiMessageSentenceFootnotesComponent', () => {
  let component: ChatUiMessageSentenceFootnotesComponent;
  let fixture: ComponentFixture<ChatUiMessageSentenceFootnotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ChatUiMessageSentenceFootnotesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChatUiMessageSentenceFootnotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
