import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RagSettingsInputComponent } from './rag-settings-input.component';

describe('RagSettingsInputComponent', () => {
  let component: RagSettingsInputComponent;
  let fixture: ComponentFixture<RagSettingsInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RagSettingsInputComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RagSettingsInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
