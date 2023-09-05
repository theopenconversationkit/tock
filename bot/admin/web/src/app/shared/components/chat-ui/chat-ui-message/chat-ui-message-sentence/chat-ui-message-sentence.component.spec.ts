import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Sentence } from '../../../../model/dialog-data';

import { ChatUiMessageSentenceComponent } from './chat-ui-message-sentence.component';

const sentence = new Sentence(0, []);

describe('ChatUiMessageSentenceComponent', () => {
  let component: ChatUiMessageSentenceComponent;
  let fixture: ComponentFixture<ChatUiMessageSentenceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ChatUiMessageSentenceComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatUiMessageSentenceComponent);
    component = fixture.componentInstance;
    component.sentence = sentence;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
