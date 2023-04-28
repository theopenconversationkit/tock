import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbDialogRef,
  NbDialogService,
  NbIconModule,
  NbSelectModule,
  NbSpinnerModule,
  NbToastrService,
  NbTooltipModule
} from '@nebular/theme';
import { of } from 'rxjs';

import { BotService } from '../../../bot/bot-service';
import { StateService } from '../../../core-nlp/state.service';
import { FormControlComponent } from '../../../shared/components';
import { FaqService } from '../../services/faq.service';
import { FaqManagementSettingsComponent } from './faq-management-settings.component';
import { StoryDefinitionConfigurationSummary } from '../../../bot/model/story';
import { Settings } from '../../models';
import { TestingModule } from '../../../../testing';
import { StubNbDialogService, StubNbToastrService, StubStateService } from '../../../../testing/stubs';

const mockStories = [
  { _id: '1', name: 'story 1', category: 'category' } as StoryDefinitionConfigurationSummary,
  { _id: '2', name: 'story 2', category: 'category' } as StoryDefinitionConfigurationSummary,
  { _id: '3', name: 'story 3', category: 'faq' } as StoryDefinitionConfigurationSummary,
  { _id: '4', name: 'story 4', category: 'scenario' } as StoryDefinitionConfigurationSummary
];

const mockSettings = {
  satisfactionEnabled: true,
  satisfactionStoryId: '1'
};

class BotServiceMock {
  searchStories() {
    return of(mockStories);
  }
}

class FaqServiceMock {
  getSettings() {
    return of(mockSettings);
  }
}

describe('FaqManagementSettingsComponent', () => {
  let component: FaqManagementSettingsComponent;
  let fixture: ComponentFixture<FaqManagementSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqManagementSettingsComponent, FormControlComponent],
      imports: [
        TestingModule,
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
        { provide: StateService, useClass: StubStateService },
        { provide: FaqService, useClass: FaqServiceMock },
        { provide: NbDialogService, useClass: StubNbDialogService },
        { provide: NbToastrService, useClass: StubNbToastrService }
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

  it('should initialize a form and update value with the api response', () => {
    expect(component.form.value).toEqual(mockSettings);
  });

  it('should initialize available stories and update value with api response without stories having "faq" category', () => {
    expect(component.availableStories).toHaveSize(3);

    component.availableStories.forEach((story) => {
      expect(story.category).not.toBe('faq');
    });
  });

  it('should associate validators to the satisfaction story id field and enable it when the satisfaction enabled field is true', () => {
    expect(component.form.value).toEqual(mockSettings);

    // set satisfactionEnabled to false
    component.satisfactionEnabled.setValue(false);
    expect(component.form.valid).toBeTruthy();
    expect(component.satisfactionStoryId.disabled).toBeTrue();
    expect(component.satisfactionStoryId.value).toBeFalsy();

    // set satisfactionEnabled to true
    component.satisfactionEnabled.setValue(true);
    expect(component.form.valid).toBeFalsy();
    expect(component.satisfactionStoryId.value).toBeFalsy();
    expect(component.satisfactionStoryId.enabled).toBeTrue();
    expect(component.satisfactionStoryId.errors.required).toBeTruthy();

    // set satisfactionId to something correct
    component.satisfactionEnabled.setValue(true);
    component.satisfactionStoryId.setValue(mockStories[0]._id);
    expect(component.form.valid).toBeTruthy();
    expect(component.satisfactionStoryId.value).toBe(mockStories[0]._id);
    expect(component.satisfactionStoryId.enabled).toBeTrue();
    expect(component.satisfactionStoryId.errors).toBeFalsy();
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
    it('should call the method to save settings after displaying a confirmation request message and confirm if the satisfaction enabled field is false', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('yes') } as NbDialogRef<any>);
      spyOn(component, 'saveSettings');

      component.satisfactionEnabled.setValue(false);
      component.save();

      expect(component['nbDialogService'].open).toHaveBeenCalled();
      expect(component.saveSettings).toHaveBeenCalledOnceWith(component.form.value as Settings);
    });

    it('should not call the method to save settings after displaying a confirmation request message and cancel if the satisfaction enabled field is false', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
      spyOn(component, 'saveSettings');

      component.satisfactionEnabled.setValue(false);
      component.save();

      expect(component['nbDialogService'].open).toHaveBeenCalled();
      expect(component.saveSettings).not.toHaveBeenCalled();
    });

    it('should call the method to save settings if the satisfaction enabled field is true', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
      spyOn(component, 'saveSettings');

      component.satisfactionEnabled.setValue(true);
      component.satisfactionStoryId.setValue('1');
      component.save();

      expect(component['nbDialogService'].open).not.toHaveBeenCalled();
      expect(component.saveSettings).toHaveBeenCalledOnceWith(component.form.value as Settings);
    });
  });
});
