import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogService } from '@nebular/theme';
import { Debug } from '../../../../model/dialog-data';

import { ChatUiMessageDebugComponent } from './chat-ui-message-debug.component';

const mockDebugMessage = {
  text: 'title1',
  data: {
    level: 'INFO',
    message: 'This is a debug message',
    errors: ['error 578', 'error 24']
  },
  eventType: 'debug',
  delay: 0
};

describe('ChatUiMessageDebugComponent', () => {
  let component: ChatUiMessageDebugComponent;
  let fixture: ComponentFixture<ChatUiMessageDebugComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ChatUiMessageDebugComponent],
      providers: [
        {
          provide: NbDialogService,
          useValue: {
            open: () => {}
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatUiMessageDebugComponent);
    component = fixture.componentInstance;
    component.message = mockDebugMessage as Debug;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
