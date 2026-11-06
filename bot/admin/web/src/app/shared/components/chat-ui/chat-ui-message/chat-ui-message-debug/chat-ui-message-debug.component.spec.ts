import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogService } from '@nebular/theme';
import { ChatUiMessageDebugComponent } from './chat-ui-message-debug.component';
import { TestSharedModule } from '../../../../test-shared.module';

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
      imports: [TestSharedModule],
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
    // component.message = mockDebugMessage as Debug;
    component.action = { ragDebug: undefined } as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should count truncated chunks and show the tag even when condensed', () => {
    fixture.componentRef.setInput('action', {
      ragDebug: { documents: [{ metadata: { truncated: true } }, { metadata: {} }, { metadata: { truncated: true } }] }
    });
    fixture.componentRef.setInput('condensed', true);
    fixture.detectChanges();
    expect(component.truncatedCount).toBe(2);
    expect(fixture.nativeElement.textContent).toContain('Truncated chunks');
  });

  describe('status tag', () => {
    function setRagDebug(ragDebug: any): void {
      fixture.componentRef.setInput('action', { ragDebug });
      fixture.detectChanges();
    }

    it('should display the status label when answer.status is provided', () => {
      setRagDebug({ answer: { status: 'found_in_context' } });
      expect(component.showStatusTag).toBeTrue();
      expect(component.getStatusClassName()).toBe('status-success');
      expect(fixture.nativeElement.textContent).toContain('Found in context');
    });

    it('should display "Technical error" when the engine reports a technical_error status', () => {
      setRagDebug({ answer: { status: 'technical_error' }, error: { errorMessage: 'boom' } });
      expect(fixture.nativeElement.textContent).toContain('Technical error');
    });

    it('should display "Error" for legacy debug data carrying an error without answer.status', () => {
      setRagDebug({ error: { errorMessage: 'boom', errorDetail: {} } });
      expect(component.showStatusTag).toBeTrue();
      expect(component.getStatusClassName()).toBe('status-error');
      expect(fixture.nativeElement.textContent).toContain('Status | Error');
    });

    it('should not display any status tag for legacy debug data without answer.status nor error', () => {
      setRagDebug({ user_question: 'legacy question', answer: 'legacy plain text answer', documents: [] });
      expect(component.showStatusTag).toBeFalse();
      expect(fixture.nativeElement.textContent).not.toContain('Status |');
    });
  });
});
