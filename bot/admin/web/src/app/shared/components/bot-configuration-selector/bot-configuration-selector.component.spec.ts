import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BotConfigurationSelectorComponent } from './bot-configuration-selector.component';

describe('BotConfigurationSelectorComponent', () => {
  let component: BotConfigurationSelectorComponent;
  let fixture: ComponentFixture<BotConfigurationSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BotConfigurationSelectorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BotConfigurationSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
