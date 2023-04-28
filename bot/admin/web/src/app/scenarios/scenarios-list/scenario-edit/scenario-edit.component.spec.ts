import { SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbDialogRef,
  NbDialogService,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbSpinnerModule,
  NbTagModule,
  NbTooltipModule
} from '@nebular/theme';
import { of } from 'rxjs';

import { ScenarioGroup } from '../../models';
import { ScenarioService } from '../../services';
import { ScenarioEditComponent } from './scenario-edit.component';
import { AutocompleteInputComponent, FormControlComponent } from '../../../shared/components';
import { StateService } from '../../../core-nlp/state.service';
import { I18nLabel, I18nLabels } from '../../../bot/model/i18n';
import { UserInterfaceType } from '../../../core/model/configuration';
import { TestingModule } from '../../../../testing';
import { StubNbDialogService, StubStateService } from '../../../../testing/stubs';
import { SpyOnCustomMatchers } from '../../../../testing/matchers';
import { By } from '@angular/platform-browser';
import { BotService } from '../../../bot/bot-service';

class StubScenarioService {
  getState() {
    return {
      categories: ['category 1', 'category 2'],
      tags: ['tag 1', 'tag 2', 'tag 3']
    };
  }
}

class StubBotService {
  i18nLabel() {
    return of(mockI18nLabel);
  }
}

const mockScenarioGroup = {
  id: 'abc',
  name: 'scenario',
  category: 'technology',
  description: 'description of scenario',
  tags: ['tag1', 'tag2'],
  unknownAnswerId: 'app_test_1'
} as ScenarioGroup;

const mockI18nLabel = {
  _id: 'app_test_1',
  namespace: 'app',
  category: 'test',
  i18n: [
    {
      locale: 'en',
      interfaceType: UserInterfaceType.textChat,
      label: 'mockI18nLabels_2',
      validated: true,
      alternatives: []
    },
    {
      locale: 'fr',
      interfaceType: UserInterfaceType.textChat,
      label: 'mockI18nLabels_1',
      validated: true,
      alternatives: []
    },
    {
      locale: 'fr',
      interfaceType: UserInterfaceType.voiceAssistant,
      label: 'mockI18nLabels_3',
      validated: true,
      alternatives: []
    }
  ]
} as I18nLabel;

describe('ScenarioEditComponent', () => {
  let component: ScenarioEditComponent;
  let fixture: ComponentFixture<ScenarioEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioEditComponent, AutocompleteInputComponent, FormControlComponent],
      imports: [
        TestingModule,
        NbAutocompleteModule,
        NbButtonModule,
        NbCardModule,
        NbFormFieldModule,
        NbIconModule,
        NbInputModule,
        NbSpinnerModule,
        NbTagModule,
        NbTooltipModule
      ],
      providers: [
        { provide: NbDialogService, useClass: StubNbDialogService },
        { provide: ScenarioService, useClass: StubScenarioService },
        { provide: StateService, useClass: StubStateService },
        { provide: BotService, useClass: StubBotService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    jasmine.addMatchers(SpyOnCustomMatchers);
    fixture = TestBed.createComponent(ScenarioEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize an empty form when creating a new scenario group', () => {
    const scenarioGroup = {
      id: null,
      name: '',
      category: '',
      description: '',
      tags: [],
      unknownAnswerId: ''
    } as ScenarioGroup;

    component.scenarioGroup = scenarioGroup;
    component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
    fixture.detectChanges();

    expect(component.form.valid).toBeFalse();
    expect(component.form.value).toEqual({
      name: '',
      category: '',
      description: '',
      tags: [],
      unknownAnswers: [
        {
          locale: 'fr',
          answer: null,
          interfaceType: 0
        },
        {
          locale: 'en',
          answer: null,
          interfaceType: 0
        }
      ]
    });
  });

  it('should initialize a form when editing a group of scenarios', () => {
    component.scenarioGroup = mockScenarioGroup;
    component.ngOnChanges({ scenarioGroup: new SimpleChange(null, mockScenarioGroup, true) });
    fixture.detectChanges();

    expect(component.form.valid).toBeTrue();
    expect(component.form.value).toEqual({
      name: 'scenario',
      category: 'technology',
      description: 'description of scenario',
      tags: ['tag1', 'tag2'],
      unknownAnswers: [
        { locale: 'fr', interfaceType: 0, answer: 'mockI18nLabels_1' },
        { locale: 'en', interfaceType: 0, answer: 'mockI18nLabels_2' }
      ]
    });
  });

  it('should associate validators to the name', () => {
    expect(component.name.valid).toBeFalse();

    // name is required
    expect(component.name.errors.required).toBeTruthy();

    // set name to something correct
    component.name.setValue('title');
    expect(component.name.errors).toBeFalsy();
    expect(component.name.valid).toBeTrue();
  });

  it('should associate validators to the answer for the unknown answers', () => {
    const scenarioGroup = {
      id: null,
      name: '',
      category: '',
      description: '',
      tags: [],
      unknownAnswerId: ''
    } as ScenarioGroup;
    component.scenarioGroup = scenarioGroup;
    component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
    fixture.detectChanges();

    expect(component.unknownAnswers.valid).toBeFalse();

    // answer is required
    component.unknownAnswers.controls.forEach((unknownAnswer) => {
      expect(unknownAnswer.get('answer').errors.required).toBeTruthy();
    });

    // set one answer to something correct
    component.unknownAnswers.controls[0].get('answer').setValue('test 1');
    expect(component.unknownAnswers.controls[0].get('answer').errors).toBeFalsy();
    expect(component.unknownAnswers.controls[1].get('answer').errors).toBeTruthy();

    // set all fields to something correct
    component.unknownAnswers.controls[1].get('answer').setValue('test 2');
    component.unknownAnswers.controls.forEach((unknownAnswer) => {
      expect(unknownAnswer.get('answer').errors).toBeFalsy();
    });
    expect(component.unknownAnswers.valid).toBeTrue();
  });

  describe('#close', () => {
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
    it('should not call the onSave method when the form is not valid', () => {
      spyOn(component.onSave, 'emit');
      const scenarioGroup = {
        name: '',
        category: 'scenario',
        description: 'description',
        tags: [],
        unknownAnswerId: ''
      } as ScenarioGroup;

      component.scenarioGroup = scenarioGroup;
      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
      fixture.detectChanges();

      expect(component.form.valid).toBeFalse();
      expect(component.onSave.emit).not.toHaveBeenCalled();
    });

    it('should call the onSave method when the form is valid and not redirect if not specified', () => {
      spyOn(component.onSave, 'emit');
      const scenarioGroup = {
        id: null,
        name: 'scenario 1',
        category: 'scenario',
        description: 'description',
        enabled: false,
        tags: [],
        unknownAnswerId: 'app_test_1'
      } as ScenarioGroup;
      const unknownAnswers = [
        {
          locale: 'fr',
          interfaceType: UserInterfaceType.textChat,
          answer: 'mockI18nLabels_1'
        },
        {
          locale: 'en',
          interfaceType: UserInterfaceType.textChat,
          answer: 'mockI18nLabels_2'
        }
      ];
      component.scenarioGroup = scenarioGroup;
      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
      fixture.detectChanges();

      component.save();

      expect(component.form.valid).toBeTrue();
      expect(component.onSave.emit).toHaveBeenCalledOnceWithDeepEquality({
        redirect: false,
        scenarioGroup,
        unknownAnswers,
        i18nLabel: mockI18nLabel
      });
    });

    it('should call the onSave method when the form is valid and redirect if specified', () => {
      spyOn(component.onSave, 'emit');
      const scenarioGroup = {
        id: null,
        name: 'scenario 1',
        category: 'scenario',
        description: 'description',
        enabled: false,
        tags: [],
        unknownAnswerId: 'app_test_1'
      } as ScenarioGroup;
      const unknownAnswers = [
        {
          locale: 'fr',
          interfaceType: UserInterfaceType.textChat,
          answer: 'mockI18nLabels_1'
        },
        {
          locale: 'en',
          interfaceType: UserInterfaceType.textChat,
          answer: 'mockI18nLabels_2'
        }
      ];
      component.scenarioGroup = scenarioGroup;
      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
      fixture.detectChanges();

      component.save(true);

      expect(component.form.valid).toBeTrue();
      expect(component.onSave.emit).toHaveBeenCalledOnceWithDeepEquality({
        redirect: true,
        scenarioGroup,
        unknownAnswers,
        i18nLabel: mockI18nLabel
      });
    });
  });

  it('should populate the categories array for the autocompletion with the elements stored in the state of the scenario when the component is initialized', () => {
    component.ngOnChanges({ scenarioGroup: new SimpleChange(null, null, true) });
    fixture.detectChanges();

    expect(component.categories).toHaveSize(2);
    expect(component.categories).toEqual(['category 1', 'category 2']);
  });

  it('should populate the tags array for the autocompletion with the elements stored in the state of the scenario when the component is initialized', (done) => {
    component.ngOnChanges({ scenarioGroup: new SimpleChange(null, null, true) });
    fixture.detectChanges();

    component.tagsAutocompleteValues.subscribe((v) => {
      expect(v).toHaveSize(3);
      expect(v).toEqual(['tag 1', 'tag 2', 'tag 3']);
      done();
    });
  });

  describe('unknown answers', () => {
    class StubStateServiceSupportedLocalesExtended extends StubStateService {
      currentApplication = {
        namespace: 'namespace/test',
        name: 'test',
        _id: '1',
        supportedLocales: ['fr', 'en', 'es']
      };
    }

    const scenarioGroup = {
      id: null,
      name: 'scenario 1',
      category: 'scenario',
      description: 'description',
      enabled: false,
      tags: [],
      unknownAnswerId: 'app_test_1'
    } as ScenarioGroup;

    it('should create as many fields as there are existing locales in the application at the initialization of the component', () => {
      component.scenarioGroup = scenarioGroup;
      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
      fixture.detectChanges();
      const unknownAnswersList = fixture.debugElement.queryAll(By.css('[data-testid="unknownAnswers"] [data-testid="unknownAnswer"]'));

      expect(unknownAnswersList).toHaveSize(component['stateService'].currentApplication.supportedLocales.length);
    });

    it('should not fill the fields if the value unknownAnswerId is not set', () => {
      const scenarioGroupCopy = { ...scenarioGroup, unknownAnswerId: '' };
      const expectedUnknownAnswersValue = [
        {
          locale: 'fr',
          interfaceType: UserInterfaceType.textChat,
          answer: null
        },
        {
          locale: 'en',
          interfaceType: UserInterfaceType.textChat,
          answer: null
        }
      ];
      component.scenarioGroup = scenarioGroupCopy;
      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroupCopy, true) });
      fixture.detectChanges();
      const inputElements: HTMLInputElement[] = fixture.debugElement
        .queryAll(By.css('[data-testid="unknownAnswers"] [data-testid="unknownAnswer"] input'))
        .map((t) => t.nativeElement);

      expect(component.unknownAnswers.value).toEqual(expectedUnknownAnswersValue);
      expect(inputElements[0].value).toBe('');
      expect(inputElements[1].value).toBe('');
    });

    it('should fill all fields if the value unknownAnswerId is defined and all responses associated with the locales are defined', () => {
      const expectedUnknownAnswersValue = [
        {
          locale: 'fr',
          interfaceType: UserInterfaceType.textChat,
          answer: 'mockI18nLabels_1'
        },
        {
          locale: 'en',
          interfaceType: UserInterfaceType.textChat,
          answer: 'mockI18nLabels_2'
        }
      ];
      component.scenarioGroup = scenarioGroup;
      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
      fixture.detectChanges();
      const inputElements: HTMLInputElement[] = fixture.debugElement
        .queryAll(By.css('[data-testid="unknownAnswers"] [data-testid="unknownAnswer"] input'))
        .map((t) => t.nativeElement);

      expect(component.unknownAnswers.value).toEqual(expectedUnknownAnswersValue);
      expect(inputElements[0].value).toBe('mockI18nLabels_1');
      expect(inputElements[1].value).toBe('mockI18nLabels_2');
    });

    it('should fill the fields if the value unknownAnswerId is defined and according to the answers associated with the defined locales', () => {
      component['stateService'] = new StubStateServiceSupportedLocalesExtended() as never;
      const expectedUnknownAnswersValue = [
        {
          locale: 'fr',
          interfaceType: UserInterfaceType.textChat,
          answer: 'mockI18nLabels_1'
        },
        {
          locale: 'en',
          interfaceType: UserInterfaceType.textChat,
          answer: 'mockI18nLabels_2'
        },
        {
          locale: 'es',
          interfaceType: UserInterfaceType.textChat,
          answer: null
        }
      ];
      component.scenarioGroup = scenarioGroup;
      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
      fixture.detectChanges();
      const inputElements: HTMLInputElement[] = fixture.debugElement
        .queryAll(By.css('[data-testid="unknownAnswers"] [data-testid="unknownAnswer"] input'))
        .map((t) => t.nativeElement);

      expect(component.unknownAnswers.value).toEqual(expectedUnknownAnswersValue);
      expect(inputElements[0].value).toBe('mockI18nLabels_1');
      expect(inputElements[1].value).toBe('mockI18nLabels_2');
      expect(inputElements[2].value).toBe('');
    });

    it('should reset the corresponding input field when the button is clicked', () => {
      component.scenarioGroup = scenarioGroup;
      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
      fixture.detectChanges();
      const unknownAnswersList = fixture.debugElement.queryAll(By.css('[data-testid="unknownAnswers"] [data-testid="unknownAnswer"]'));
      const inputElement: HTMLInputElement = unknownAnswersList[0].nativeElement.querySelector('input');
      const firstFieldResetButton: HTMLButtonElement = unknownAnswersList[0].nativeElement.querySelector(
        '[data-testid="resetUnknownAnswer"]'
      );

      expect(component.unknownAnswers.controls).toHaveSize(2);
      expect(inputElement.value).toBe('mockI18nLabels_1');
      expect(component.unknownAnswers.controls[0].get('answer').value).toBe('mockI18nLabels_1');
      expect(component.unknownAnswers.controls[1].get('answer').value).toBe('mockI18nLabels_2');

      firstFieldResetButton.click();

      expect(component.unknownAnswers.controls).toHaveSize(2);
      expect(inputElement.value).toBe('');
      expect(component.unknownAnswers.controls[0].get('answer').value).toBeNull();
      expect(component.unknownAnswers.controls[1].get('answer').value).toBe('mockI18nLabels_2');
    });
  });
});
