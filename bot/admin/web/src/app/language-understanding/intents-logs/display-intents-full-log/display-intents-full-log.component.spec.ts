import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DisplayIntentFullLogComponent } from './display-intents-full-log.component';

describe('DisplayIntentFullLogComponent', () => {
  let component: DisplayIntentFullLogComponent;
  let fixture: ComponentFixture<DisplayIntentFullLogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DisplayIntentFullLogComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(DisplayIntentFullLogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
