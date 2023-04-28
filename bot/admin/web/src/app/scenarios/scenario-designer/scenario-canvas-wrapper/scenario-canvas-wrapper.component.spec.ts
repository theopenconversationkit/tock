import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbIconModule, NbTooltipModule } from '@nebular/theme';

import { ScenarioCanvasWrapperComponent } from './scenario-canvas-wrapper.component';
import { TestingModule } from '../../../../testing';

describe('ScenarioCanvasWrapperComponent', () => {
  let component: ScenarioCanvasWrapperComponent;
  let fixture: ComponentFixture<ScenarioCanvasWrapperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioCanvasWrapperComponent],
      imports: [TestingModule, NbIconModule, NbTooltipModule]
    }).compileComponents();
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
