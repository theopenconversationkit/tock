import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ObservabilitySettingsComponent } from './observability-settings.component';

describe('ObservabilitySettingsComponent', () => {
  let component: ObservabilitySettingsComponent;
  let fixture: ComponentFixture<ObservabilitySettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ObservabilitySettingsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ObservabilitySettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
