import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';

import { DialogService } from '../../core-nlp/dialog.service';
import { ScenarioService } from '../services/scenario.service';
import { ScenarioEditComponent } from './scenario-edit.component';

describe('ScenarioEditComponent', () => {
  let component: ScenarioEditComponent;
  let fixture: ComponentFixture<ScenarioEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioEditComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: DialogService, useValue: {} },
        { provide: ScenarioService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
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
