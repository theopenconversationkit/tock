import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScenarioCanvasWrapperComponent } from './scenario-canvas-wrapper.component';

describe('ScenarioCanvasWrapperComponent', () => {
  let component: ScenarioCanvasWrapperComponent;
  let fixture: ComponentFixture<ScenarioCanvasWrapperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScenarioCanvasWrapperComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioCanvasWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
