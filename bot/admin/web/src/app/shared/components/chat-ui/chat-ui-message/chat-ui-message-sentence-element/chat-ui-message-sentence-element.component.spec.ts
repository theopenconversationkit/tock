import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChatUiMessageSentenceElementComponent } from './chat-ui-message-sentence-element.component';

describe('ChatUiMessageSentenceElementComponent', () => {
  let component: ChatUiMessageSentenceElementComponent;
  let fixture: ComponentFixture<ChatUiMessageSentenceElementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ChatUiMessageSentenceElementComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatUiMessageSentenceElementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
