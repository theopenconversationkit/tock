import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainGridItemComponent } from './train-grid-item.component';

describe('TrainGridItemComponent', () => {
  let component: TrainGridItemComponent;
  let fixture: ComponentFixture<TrainGridItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TrainGridItemComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TrainGridItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
