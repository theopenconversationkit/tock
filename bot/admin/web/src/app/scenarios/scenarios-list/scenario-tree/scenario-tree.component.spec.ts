import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbTreeGridModule } from '@nebular/theme';

import { ScenarioTreeComponent } from './scenario-tree.component';

describe('ScenarioTreeComponent', () => {
  let component: ScenarioTreeComponent;
  let fixture: ComponentFixture<ScenarioTreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioTreeComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
