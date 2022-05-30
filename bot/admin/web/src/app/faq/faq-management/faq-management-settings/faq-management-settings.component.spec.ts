import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbIconModule,
  NbSelectModule,
  NbSpinnerModule,
  NbToastrService,
  NbTooltipModule
} from '@nebular/theme';
import { of } from 'rxjs';

import { BotService } from '../../../bot/bot-service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { StateService } from '../../../core-nlp/state.service';
import { FormControlComponent } from '../../../shared/form-control/form-control.component';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { FaqService } from '../../services/faq.service';
import { FaqManagementSettingsComponent } from './faq-management-settings.component';

class BotServiceMock {
  searchStories(story) {
    return of([]);
  }
}

class FaqServiceMock {
  getSettings() {
    return of([]);
  }
}

class StateServiceMock {
  currentApplication = {
    namespace: 'namespace/test',
    name: 'test'
  };

  currentLocal = 'fr';
}

describe('FaqManagementSettingsComponent', () => {
  let component: FaqManagementSettingsComponent;
  let fixture: ComponentFixture<FaqManagementSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqManagementSettingsComponent, FormControlComponent],
      imports: [
        TestSharedModule,
        NbButtonModule,
        NbCardModule,
        NbCheckboxModule,
        NbIconModule,
        NbSelectModule,
        NbSpinnerModule,
        NbTooltipModule
      ],
      providers: [
        { provide: BotService, useClass: BotServiceMock },
        { provide: StateService, useClass: StateServiceMock },
        { provide: FaqService, useClass: FaqServiceMock },
        { provide: DialogService, useValue: {} },
        { provide: NbToastrService, useValue: {} }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FaqManagementSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
