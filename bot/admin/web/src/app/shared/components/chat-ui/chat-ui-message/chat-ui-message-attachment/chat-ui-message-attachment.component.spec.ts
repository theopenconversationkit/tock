import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Attachment } from '../../../../model/dialog-data';

import { ChatUiMessageAttachmentComponent } from './chat-ui-message-attachment.component';

const attachment = new Attachment(0, 'http://bot_api:8080/f/appf728cff0-40ee-456b-88d3-41a873f696e4.txt', 3);

describe('ChatUiMessageAttachmentComponent', () => {
  let component: ChatUiMessageAttachmentComponent;
  let fixture: ComponentFixture<ChatUiMessageAttachmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ChatUiMessageAttachmentComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatUiMessageAttachmentComponent);
    component = fixture.componentInstance;
    component.attachment = attachment;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
