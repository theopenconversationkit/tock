import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiSettingsEngineConfigParamInputComponent } from './ai-settings-engine-config-param-input.component';

describe('AiSettingsEngineConfigParamInputComponent', () => {
  let component: AiSettingsEngineConfigParamInputComponent;
  let fixture: ComponentFixture<AiSettingsEngineConfigParamInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AiSettingsEngineConfigParamInputComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AiSettingsEngineConfigParamInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
