import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';
import { of } from 'rxjs';

import { IntentsSearchComponent } from './intents-search.component';
import { ScenarioService } from '../../../services';
import { StateService } from '../../../../core-nlp/state.service';
import { NlpService } from '../../../../nlp-tabs/nlp.service';
import { TestingModule } from '../../../../../testing';

describe('IntentsSearchComponent', () => {
  let component: IntentsSearchComponent;
  let fixture: ComponentFixture<IntentsSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IntentsSearchComponent],
      imports: [TestingModule],
      providers: [
        { provide: NbDialogRef, useValue: {} },
        {
          provide: ScenarioService,
          useValue: {
            createSearchIntentsQuery: () => {}
          }
        },
        { provide: StateService, useValue: {} },
        {
          provide: NlpService,
          useValue: {
            searchSentences: () => of({ rows: [] })
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IntentsSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('Should destroy', () => {
    expect(component.ngOnDestroy).toBeDefined();
    expect(component.destroy.isStopped).toBeFalsy();
    component.ngOnDestroy();
    expect(component.destroy.isStopped).toBeTruthy();
  });
});
