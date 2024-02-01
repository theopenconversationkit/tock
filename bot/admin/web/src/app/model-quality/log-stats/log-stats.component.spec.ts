import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogStatsComponent } from './log-stats.component';

describe('LogStatsComponent', () => {
  let component: LogStatsComponent;
  let fixture: ComponentFixture<LogStatsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LogStatsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LogStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
