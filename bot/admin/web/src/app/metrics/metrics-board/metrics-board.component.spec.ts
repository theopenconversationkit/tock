import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetricsBoardComponent } from './metrics-board.component';

describe('MetricsBoardComponent', () => {
  let component: MetricsBoardComponent;
  let fixture: ComponentFixture<MetricsBoardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetricsBoardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricsBoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
