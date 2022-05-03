import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScenarioListSimpleComponent } from './scenario-list-simple.component';

describe('ScenarioListSimpleComponent', () => {
  let component: ScenarioListSimpleComponent;
  let fixture: ComponentFixture<ScenarioListSimpleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScenarioListSimpleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioListSimpleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
