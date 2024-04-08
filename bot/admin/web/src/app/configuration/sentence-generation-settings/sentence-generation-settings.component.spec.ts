import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentenceGenerationSettingsComponent } from './sentence-generation-settings.component';

describe('SentenceGenerationSettingsComponent', () => {
  let component: SentenceGenerationSettingsComponent;
  let fixture: ComponentFixture<SentenceGenerationSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SentenceGenerationSettingsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SentenceGenerationSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
