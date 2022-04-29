import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScenarioEditComponent } from './scenario-edit.component';

describe('ScenarioEditComponent', () => {
  let component: ScenarioEditComponent;
  let fixture: ComponentFixture<ScenarioEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScenarioEditComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
