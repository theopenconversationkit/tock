import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import {
  NbCardModule,
  NbDialogRef,
  NbDialogService,
  NbIconModule,
  NbInputModule,
  NbSelectModule,
  NbSpinnerModule,
  NbToastrService,
  NbTooltipModule
} from '@nebular/theme';
import { of, throwError } from 'rxjs';

import { StoryDefinitionConfigurationSummary } from '../../../bot/model/story';
import { BotService } from '../../../bot/bot-service';
import { ScenarioSettings } from '../../models';
import { ScenarioSettingsService } from '../../services';
import { ScenariosSettingsComponent } from './scenarios-settings.component';
import { StateService } from '../../../core-nlp/state.service';
import { FormControlComponent } from '../../../shared/components';
import { TestingModule } from '../../../../testing';
import { StubNbDialogService, StubNbToastrService, StubStateService } from '../../../../testing/stubs';

const mock: { stories: StoryDefinitionConfigurationSummary[]; settings: ScenarioSettings } = {
  stories: [
    { _id: '1', name: 'story 1', category: 'category' } as StoryDefinitionConfigurationSummary,
    { _id: '2', name: 'story 2', category: 'category' } as StoryDefinitionConfigurationSummary,
    { _id: '3', name: 'story 3', category: 'faq' } as StoryDefinitionConfigurationSummary,
    { _id: '4', name: 'story 4', category: 'scenario' } as StoryDefinitionConfigurationSummary
  ],
  settings: {
    actionRepetitionNumber: 4,
    redirectStoryId: '1'
  }
};

class BotServiceMock {
  searchStories() {
    return of(mock.stories);
  }
}

class ScenarioSettingsServiceMock {
  getSettings() {
    return of(mock.settings);
  }

  saveSettings() {}
}

describe('ScenariosSettingsComponent', () => {
  let component: ScenariosSettingsComponent;
  let fixture: ComponentFixture<ScenariosSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenariosSettingsComponent, FormControlComponent],
      imports: [TestingModule, NbCardModule, NbIconModule, NbInputModule, NbSelectModule, NbSpinnerModule, NbTooltipModule],
      providers: [
        { provide: BotService, useClass: BotServiceMock },
        { provide: StateService, useClass: StubStateService },
        { provide: ScenarioSettingsService, useClass: ScenarioSettingsServiceMock },
        { provide: NbDialogService, useClass: StubNbDialogService },
        { provide: NbToastrService, useClass: StubNbToastrService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenariosSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize a form and update value with the api response', () => {
    expect(component.form.value).toEqual(mock.settings);
  });

  it('should initialize available stories and update value with api response without stories having "faq" category', () => {
    expect(component.availableStories).toHaveSize(3);

    component.availableStories.forEach((story) => {
      expect(story.category).not.toBe('faq');
    });
  });

  it('should associate validators to the satisfaction repetition field', () => {
    // initialize the field
    component.actionRepetitionNumber.setValue(null);

    expect(component.actionRepetitionNumber.valid).toBeFalse();

    // actionRepetitionNumber is required
    expect(component.actionRepetitionNumber.errors.required).toBeTruthy();
    expect(component.actionRepetitionNumber.errors.min).toBeFalsy();

    // set actionRepetitionNumber to a value lower than the minimum (2)
    component.actionRepetitionNumber.setValue(-1);
    expect(component.actionRepetitionNumber.errors.required).toBeFalsy();
    expect(component.actionRepetitionNumber.errors.min).toBeTruthy();

    // set actionRepetitionNumber to something correct
    component.actionRepetitionNumber.setValue(2);
    expect(component.actionRepetitionNumber.errors).toBeFalsy();
    expect(component.actionRepetitionNumber.valid).toBeTrue();
  });

  it('should associate validators to the satisfaction story id field and enble it when the number of repetition is upper than 0', () => {
    // initialize the field
    component.redirectStoryId.setValue(null);

    // set actionRepetitionNumber to 0
    component.actionRepetitionNumber.setValue(0);
    expect(component.form.valid).toBeTrue();
    expect(component.redirectStoryId.disabled).toBeTrue();
    expect(component.redirectStoryId.errors).toBeFalsy();

    // set actionRepetitionNumber to a value upper than 0
    component.actionRepetitionNumber.setValue(2);
    expect(component.form.valid).toBeFalse();
    expect(component.redirectStoryId.enabled).toBeTrue();
    expect(component.redirectStoryId.errors.required).toBeTruthy();

    // set redirectStoryId to something correct
    component.actionRepetitionNumber.setValue(2);
    component.redirectStoryId.setValue('test');
    expect(component.form.valid).toBeTrue();
    expect(component.redirectStoryId.errors).toBeFalsy();
    expect(component.redirectStoryId.valid).toBeTrue();
  });

  describe('#close', () => {
    it('should call the method when the close button in header is clicked', () => {
      spyOn(component, 'close');
      const closeElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="close-button"]')).nativeElement;

      closeElement.click();

      expect(component.close).toHaveBeenCalledTimes(1);
    });

    it('should call the method when the cancel button in footer is clicked', () => {
      spyOn(component, 'close');
      const cancelElement: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="cancel-button"]')).nativeElement;

      cancelElement.click();

      expect(component.close).toHaveBeenCalledTimes(1);
    });

    it('should call the onClose method without displaying a confirmation request message when the form is not dirty', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('yes') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      component.close();

      expect(component['nbDialogService'].open).not.toHaveBeenCalled();
      expect(component.onClose.emit).toHaveBeenCalledOnceWith(true);
    });

    it('should call the onClose method after displaying a confirmation request message and confirm when the form is dirty', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('yes') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      // To display the confirmation message, the form must have been modified
      component.form.markAsDirty();
      component.close();

      expect(component['nbDialogService'].open).toHaveBeenCalled();
      expect(component.onClose.emit).toHaveBeenCalledOnceWith(true);
    });

    it('should not call the onClose method after displaying a confirmation request message and cancel when the form is dirty', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      // To display the confirmation message, the form must have been modified
      component.form.markAsDirty();
      component.close();

      expect(component['nbDialogService'].open).toHaveBeenCalled();
      expect(component.onClose.emit).not.toHaveBeenCalled();
    });
  });

  describe('#save', () => {
    it('should not save settings when the form is incorrect', () => {
      spyOn(component['scenarioSettingsService'], 'saveSettings').and.returnValue(of(mock.settings));

      component.actionRepetitionNumber.setValue(null);
      component.redirectStoryId.setValue('test');
      component.save();

      expect(component['scenarioSettingsService'].saveSettings).not.toHaveBeenCalled();
    });

    it('should save settings when the form is correct', () => {
      spyOn(component['scenarioSettingsService'], 'saveSettings').and.returnValue(of(mock.settings));

      component.actionRepetitionNumber.setValue(4);
      component.redirectStoryId.setValue('test');
      component.save();

      expect(component['scenarioSettingsService'].saveSettings).toHaveBeenCalledOnceWith(component['stateService'].currentApplication._id, {
        actionRepetitionNumber: 4,
        redirectStoryId: 'test'
      });
    });

    it('should not emit event to close the settings panel when save fails', () => {
      spyOn(component.onClose, 'emit');
      spyOn(component['scenarioSettingsService'], 'saveSettings').and.returnValue(throwError(new Error()));

      component.actionRepetitionNumber.setValue(4);
      component.redirectStoryId.setValue('1');
      component.save();

      expect(component.onClose.emit).not.toHaveBeenCalled();
    });

    it('should emit event to close the settings panel when save successfully', () => {
      spyOn(component.onClose, 'emit');
      spyOn(component['scenarioSettingsService'], 'saveSettings').and.returnValue(of(mock.settings));

      component.actionRepetitionNumber.setValue(4);
      component.redirectStoryId.setValue('1');
      component.save();

      expect(component.onClose.emit).toHaveBeenCalledTimes(1);
    });
  });
});
