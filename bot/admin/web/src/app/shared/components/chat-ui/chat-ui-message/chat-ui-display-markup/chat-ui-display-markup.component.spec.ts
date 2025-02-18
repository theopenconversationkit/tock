import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChatUiDisplayMarkupComponent } from './chat-ui-display-markup.component';

describe('ChatUiDisplayMarkupComponent', () => {
  let component: ChatUiDisplayMarkupComponent;
  let fixture: ComponentFixture<ChatUiDisplayMarkupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatUiDisplayMarkupComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ChatUiDisplayMarkupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
