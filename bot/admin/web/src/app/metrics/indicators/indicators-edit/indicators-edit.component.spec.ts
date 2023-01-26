import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IndicatorsEditComponent } from './indicators-edit.component';

describe('IndicatorsEditComponent', () => {
  let component: IndicatorsEditComponent;
  let fixture: ComponentFixture<IndicatorsEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IndicatorsEditComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IndicatorsEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
