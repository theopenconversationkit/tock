import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainComponent } from './train.component';

describe('TrainComponent', () => {
  let component: TrainComponent;
  let fixture: ComponentFixture<TrainComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TrainComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TrainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
