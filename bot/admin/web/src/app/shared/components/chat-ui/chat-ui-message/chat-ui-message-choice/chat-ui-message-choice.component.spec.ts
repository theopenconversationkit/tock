import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Choice } from '../../../../model/dialog-data';

import { ChatUiMessageChoiceComponent } from './chat-ui-message-choice.component';

const choice = new Choice(0, '', new Map());

describe('ChatUiMessageChoiceComponent', () => {
  let component: ChatUiMessageChoiceComponent;
  let fixture: ComponentFixture<ChatUiMessageChoiceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ChatUiMessageChoiceComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatUiMessageChoiceComponent);
    component = fixture.componentInstance;
    component.choice = choice;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
