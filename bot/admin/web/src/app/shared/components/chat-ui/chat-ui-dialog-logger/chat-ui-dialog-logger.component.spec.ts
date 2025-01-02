import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChatUiDialogLoggerComponent } from './chat-ui-dialog-logger.component';

describe('ChatUiDialogLoggerComponent', () => {
  let component: ChatUiDialogLoggerComponent;
  let fixture: ComponentFixture<ChatUiDialogLoggerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatUiDialogLoggerComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ChatUiDialogLoggerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
