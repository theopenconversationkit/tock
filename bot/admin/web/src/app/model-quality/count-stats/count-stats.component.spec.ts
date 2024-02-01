import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CountStatsComponent } from './count-stats.component';

describe('CountStatsComponent', () => {
  let component: CountStatsComponent;
  let fixture: ComponentFixture<CountStatsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CountStatsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CountStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
