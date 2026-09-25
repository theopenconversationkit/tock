import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SampleDetailComponent } from './sample-detail.component';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { of } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { StateServiceMock } from '../../../shared/test-shared/state-service.mock';
import { StateService } from '../../../core-nlp/state.service';

describe('SampleDetailComponent', () => {
  let component: SampleDetailComponent;
  let fixture: ComponentFixture<SampleDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SampleDetailComponent],
      imports: [TestSharedModule],
      providers: [
        {
          provide: BotConfigurationService,
          useValue: {
            configurations: of([]),
            restConfigurations: of([]),
            hasRestConfigurations: of(false),
            supportedConnectors: of([]),
            bots: of([])
          }
        },
        { provide: StateService, useClass: StateServiceMock }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(SampleDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
