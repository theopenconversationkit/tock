import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';

import { NlpStatsDisplayComponent } from './nlp-stats-display.component';

describe('NlpStatsDisplayComponent', () => {
  let component: NlpStatsDisplayComponent;
  let fixture: ComponentFixture<NlpStatsDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NlpStatsDisplayComponent],
      providers: [{ provide: NbDialogRef, useValue: { close: () => {} } }]
    }).compileComponents();

    fixture = TestBed.createComponent(NlpStatsDisplayComponent);
    component = fixture.componentInstance;
    component.data = {
      request: {},
      response: {}
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
