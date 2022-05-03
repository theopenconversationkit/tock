import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScenarioTreeComponent } from './scenario-tree.component';

describe('ScenarioTreeComponent', () => {
  let component: ScenarioTreeComponent;
  let fixture: ComponentFixture<ScenarioTreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScenarioTreeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
