import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StateService } from '../../core-nlp/state.service';

import { FaqTabsComponent } from './faq-tabs.component';

describe('FaqTabsComponent', () => {
  let component: FaqTabsComponent;
  let fixture: ComponentFixture<FaqTabsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqTabsComponent],
      providers: [
        {
          provide: StateService,
          useValue: {
            hasRole: () => true
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(FaqTabsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
