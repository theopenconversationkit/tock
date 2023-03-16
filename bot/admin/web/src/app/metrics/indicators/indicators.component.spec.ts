import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbAlertModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NbToastrService } from '@nebular/theme';
import { of } from 'rxjs';

import { DialogService } from '../../core-nlp/dialog.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { TestSharedModule } from '../../shared/test-shared.module';
import { IndicatorsComponent } from './indicators.component';

describe('IndicatorsComponent', () => {
  let component: IndicatorsComponent;
  let fixture: ComponentFixture<IndicatorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule, NbButtonModule, NbAlertModule, NbSpinnerModule, NbCardModule, NbIconModule, NbSpinnerModule],
      declarations: [IndicatorsComponent],
      providers: [
        { provide: BotConfigurationService, useValue: { configurations: of([]) } },
        { provide: StateService, useValue: {} },
        { provide: RestService, useValue: {} },
        { provide: NbToastrService, useValue: {} },
        { provide: DialogService, useValue: {} }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IndicatorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
