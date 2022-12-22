import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { first } from 'rxjs/operators';

import { ScenarioSettingsState, Settings } from '../models';
import { ScenarioSettingsService } from './scenario-settings.service';
import { ScenarioApiService } from './scenario.api.service';

const mock: { settings: Settings; initialState: ScenarioSettingsState; applicationId: string } = {
  settings: {
    actionRepetitionNumber: 4,
    redirectStoryId: '1'
  },
  initialState: {
    loaded: false,
    settings: {
      actionRepetitionNumber: 2,
      redirectStoryId: null
    }
  },
  applicationId: '1'
};

describe('ScenarioSettingsService', () => {
  let service: ScenarioSettingsService;
  const mockedScenarioApiService: jasmine.SpyObj<ScenarioApiService> = jasmine.createSpyObj('ScenarioApiService', [
    'getSettings',
    'saveSettings'
  ]);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ScenarioSettingsService,
        {
          provide: ScenarioApiService,
          useValue: mockedScenarioApiService
        }
      ]
    });
    service = TestBed.inject(ScenarioSettingsService);
    service.setState(mock.initialState);
  });

  afterEach(() => {
    mockedScenarioApiService.getSettings.calls.reset();
    mockedScenarioApiService.saveSettings.calls.reset();
  });

  it('should populate the state with the result when loading settings successfully', (done) => {
    mockedScenarioApiService.getSettings.and.returnValue(of(mock.settings));

    expect(service.getState()).toEqual(mock.initialState);

    service
      .getSettings(mock.applicationId)
      .pipe(first())
      .subscribe(() => {
        expect(mockedScenarioApiService.getSettings).toHaveBeenCalledOnceWith(mock.applicationId);
        expect(service.getState().loaded).toBeTrue();
        expect(service.getState().settings).toEqual(mock.settings);
        done();
      });
  });

  it('should not update state when loading settings fails', (done) => {
    mockedScenarioApiService.getSettings.and.returnValue(throwError(new Error()));

    expect(service.getState()).toEqual(mock.initialState);

    service.getSettings(mock.applicationId).subscribe({
      error: () => {
        expect(mockedScenarioApiService.getSettings).toHaveBeenCalledOnceWith(mock.applicationId);
        expect(service.getState()).toEqual(mock.initialState);
        done();
      }
    });
  });

  it('should update settings in the state when published successfully', (done) => {
    mockedScenarioApiService.saveSettings.and.returnValue(of(mock.settings));

    expect(service.getState()).toEqual(mock.initialState);

    service.saveSettings(mock.applicationId, mock.settings).subscribe(() => {
      expect(mockedScenarioApiService.saveSettings).toHaveBeenCalledOnceWith(mock.applicationId, mock.settings);
      expect(service.getState().settings).toEqual(mock.settings);
      done();
    });
  });

  it('should not update state when post settings fails', (done) => {
    mockedScenarioApiService.saveSettings.and.returnValue(throwError(new Error()));

    expect(service.getState()).toEqual(mock.initialState);

    service.saveSettings(mock.applicationId, mock.settings).subscribe({
      error: () => {
        expect(mockedScenarioApiService.saveSettings).toHaveBeenCalledOnceWith(mock.applicationId, mock.settings);
        expect(service.getState().settings).toEqual(mock.initialState.settings);
        done();
      }
    });
  });
});
