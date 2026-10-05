import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { AiSettingsEngineConfigParamInputComponent } from './ai-settings-engine-config-param-input.component';
import { TestSharedModule } from '../../test-shared.module';

describe('AiSettingsEngineConfigParamInputComponent', () => {
  let component: AiSettingsEngineConfigParamInputComponent;
  let fixture: ComponentFixture<AiSettingsEngineConfigParamInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AiSettingsEngineConfigParamInputComponent],
      imports: [TestSharedModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(AiSettingsEngineConfigParamInputComponent);
    component = fixture.componentInstance;

    component.parentGroup = 'setting';
    component.configurationParam = { key: 'temperature', label: 'Temperature', type: 'text' } as any;
    component.form = new FormGroup({
      setting: new FormGroup({
        temperature: new FormControl('')
      })
    });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
