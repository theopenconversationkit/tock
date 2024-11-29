import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CompressorSettingsComponent } from './compressor-settings.component';

describe('CompressorSettingsComponent', () => {
  let component: CompressorSettingsComponent;
  let fixture: ComponentFixture<CompressorSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CompressorSettingsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CompressorSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
