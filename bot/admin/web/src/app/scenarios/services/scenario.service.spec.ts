import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { Scenario, SCENARIO_MODE, SCENARIO_STATE } from '../models';
import { ScenarioApiService } from './scenario.api.service';
import { ScenarioService } from './scenario.service';

const mockScenarios: Scenario[] = [
  {
    id: '1',
    name: 'Scenario 1',
    description: 'Description 1',
    category: '',
    tags: ['tag1', ''],
    createDate: '12/01/1980',
    updateDate: null,
    data: {
      mode: SCENARIO_MODE.writing,
      scenarioItems: []
    },
    applicationId: '1',
    state: SCENARIO_STATE.draft
  },
  {
    id: '2',
    name: 'Scenario 2',
    description: 'Description 2',
    category: 'default',
    tags: ['test'],
    createDate: '01/01/1970',
    updateDate: '01/01/1970',
    data: {
      mode: SCENARIO_MODE.writing,
      scenarioItems: []
    },
    applicationId: '1',
    state: SCENARIO_STATE.draft
  },
  {
    id: '3',
    name: 'Scenario 3',
    description: 'Description 3',
    category: null,
    tags: ['tag1', 'tag2'],
    createDate: '01/01/1970',
    data: {
      mode: SCENARIO_MODE.writing,
      scenarioItems: []
    },
    applicationId: '1',
    state: SCENARIO_STATE.draft
  },
  {
    id: '4',
    name: 'Scenario 4',
    description: 'Description 4',
    category: 'scenario',
    tags: [],
    createDate: '12/01/1980',
    data: {
      mode: SCENARIO_MODE.writing,
      scenarioItems: []
    },
    applicationId: '1',
    state: SCENARIO_STATE.draft
  },
  {
    id: '5',
    name: 'Scenario 5',
    description: 'Description 5',
    category: 'scenario',
    tags: null,
    createDate: '12/01/1980',
    data: {
      mode: SCENARIO_MODE.writing,
      scenarioItems: []
    },
    applicationId: '1',
    state: SCENARIO_STATE.draft
  }
];

const initialState = {
  loaded: false,
  loading: false,
  scenarios: [],
  sagas: [],
  tags: [],
  categories: []
};

const newScenario: Scenario = {
  id: null,
  createDate: '12/01/1980',
  name: 'New scenario',
  applicationId: '1',
  state: SCENARIO_STATE.draft
};

const updatedScenario: Scenario = {
  ...mockScenarios[1],
  category: 'new category',
  description: 'Description updated',
  tags: ['test', 'new tag', 'JCVD forever']
};

describe('ScenarioService', () => {
  let service: ScenarioService;
  const mockedScenarioApiService: jasmine.SpyObj<ScenarioApiService> = jasmine.createSpyObj(
    'ScenarioApiService',
    ['getScenarios', 'postScenario', 'putScenario', 'deleteScenario']
  );

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: ScenarioApiService,
          useValue: mockedScenarioApiService
        },
        { provide: ScenarioService, useClass: ScenarioService }
      ]
    });
    service = TestBed.inject(ScenarioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should render initial state without modification when created', () => {
    expect(service.getState()).toEqual(initialState);
  });

  it('should populate the state with the result when loading scenarios successfully', (done) => {
    mockedScenarioApiService.getScenarios.and.returnValue(of(mockScenarios));

    expect(service.getState()).toEqual(initialState);

    service.getScenarios().subscribe(() => {
      const state = service.getState();
      expect(mockedScenarioApiService.getScenarios).toHaveBeenCalled();
      expect(state.loaded).toBeTrue();
      expect(state.scenarios).toHaveSize(mockScenarios.length);
      expect(state.scenarios).toEqual(mockScenarios);
      expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
      expect(state.categories).toEqual(['default', 'scenario']);
      done();
    });
  });

  it('should not update state when loading scenarios fails', (done) => {
    mockedScenarioApiService.getScenarios.and.returnValue(throwError(new Error()));

    expect(service.getState()).toEqual(initialState);

    service.getScenarios().subscribe({
      error: () => {
        const state = service.getState();
        expect(mockedScenarioApiService.getScenarios).toHaveBeenCalled();
        expect(state.loaded).toBeFalse();
        expect(state.scenarios).toHaveSize(0);
        expect(state.tags).toHaveSize(0);
        expect(state.categories).toHaveSize(0);
        done();
      }
    });
  });

  it('should add a new scenario in the state and update tags and categories cache when published successfully', (done) => {
    mockedScenarioApiService.postScenario.and.returnValue(of({ ...newScenario, id: '1' }));

    expect(service.getState()).toEqual(initialState);
    service.setScenariosData(mockScenarios);

    service.postScenario(newScenario).subscribe(() => {
      const state = service.getState();
      expect(mockedScenarioApiService.postScenario).toHaveBeenCalledOnceWith(newScenario);
      expect(state.scenarios).toHaveSize(mockScenarios.length + 1);
      expect(state.scenarios).toEqual([...mockScenarios, { ...newScenario, id: '1' }]);
      expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
      expect(state.categories).toEqual(['default', 'scenario']);
      done();
    });
  });

  /**
   * TODO: Fix the test. We don't pass in the error block
   */
  xit('should not update state when post scenario fails', (done) => {
    mockedScenarioApiService.postScenario.and.returnValue(throwError(new Error()));

    expect(service.getState()).toEqual(initialState);

    service.getScenarios().subscribe({
      next: () => {
        console.log('success');
      },
      error: () => {
        console.log('error');
        const state = service.getState();
        expect(mockedScenarioApiService.postScenario).toHaveBeenCalled();
        expect(state.loaded).toBeFalse();
        expect(state.scenarios).toHaveSize(0);
        expect(state.tags).toHaveSize(0);
        expect(state.categories).toHaveSize(0);
        done();
      }
    });
  });

  it('should update an existing scenario and update tags and categories cache from the state when the scenario is successfully updated', (done) => {
    mockedScenarioApiService.putScenario.and.returnValue(of(updatedScenario));
    const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));

    service.setScenariosData(mockScenariosCopy);
    const state = service.getState();

    expect(state.scenarios).toHaveSize(mockScenariosCopy.length);
    expect(state.scenarios.find((s) => s.id === '2')).not.toEqual(updatedScenario);
    expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
    expect(state.categories).toEqual(['default', 'scenario']);

    service.putScenario('2', updatedScenario).subscribe(() => {
      expect(mockedScenarioApiService.putScenario).toHaveBeenCalled();
      expect(state.scenarios).toHaveSize(mockScenariosCopy.length);
      expect(state.scenarios.find((s) => s.id === '2')).toEqual(updatedScenario);
      expect(state.tags).toEqual(['JCVD forever', 'new tag', 'tag1', 'tag2', 'test']);
      expect(state.categories).toEqual(['new category', 'scenario']);
      done();
    });
  });

  it('should not update state when put scenario fails', (done) => {
    mockedScenarioApiService.putScenario.and.returnValue(throwError(new Error()));
    const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));

    service.setScenariosData(mockScenariosCopy);
    const state = service.getState();

    expect(state.scenarios).toHaveSize(mockScenariosCopy.length);
    expect(state.scenarios).toEqual(mockScenarios);
    expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
    expect(state.categories).toEqual(['default', 'scenario']);

    service.putScenario('2', updatedScenario).subscribe({
      error: () => {
        expect(mockedScenarioApiService.putScenario).toHaveBeenCalled();
        expect(state.scenarios).toHaveSize(mockScenariosCopy.length);
        expect(state.scenarios.find((s) => s.id === '2')).not.toEqual(updatedScenario);
        expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
        expect(state.categories).toEqual(['default', 'scenario']);
        done();
      }
    });
  });

  it('should remove an existing scenario and update tags and categories cache from the state when the scenario is successfully deleted', (done) => {
    mockedScenarioApiService.deleteScenario.and.returnValue(of({}));
    const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));
    service.setScenariosData(mockScenariosCopy);
    const state = service.getState();

    expect(state.scenarios.length).toBe(mockScenariosCopy.length);
    expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
    expect(state.categories).toEqual(['default', 'scenario']);

    service.deleteScenario('2').subscribe(() => {
      expect(mockedScenarioApiService.deleteScenario).toHaveBeenCalled();
      expect(state.scenarios.length).toBe(--mockScenariosCopy.length);
      expect(state.scenarios.find((s) => s.id === '2')).toBe(undefined);
      expect(state.tags).toEqual(['tag1', 'tag2']);
      expect(state.categories).toEqual(['scenario']);
      done();
    });
  });

  it('should not update state when delete scenario fails', (done) => {
    mockedScenarioApiService.deleteScenario.and.returnValue(throwError(new Error()));
    const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));
    service.setScenariosData(mockScenariosCopy);
    const state = service.getState();

    expect(state.scenarios.length).toBe(mockScenariosCopy.length);
    expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
    expect(state.categories).toEqual(['default', 'scenario']);

    service.deleteScenario('2').subscribe({
      error: () => {
        expect(mockedScenarioApiService.deleteScenario).toHaveBeenCalled();
        expect(state.scenarios.length).toBe(mockScenariosCopy.length);
        expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
        expect(state.categories).toEqual(['default', 'scenario']);
        done();
      }
    });
  });

  it('should build an array of unique categories not falsy sorted alphabetically from scenarios', () => {
    const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));

    expect(service.getState().categories).toEqual([]);

    service.setScenariosData(mockScenariosCopy);

    expect(service.getState().categories).toEqual(['default', 'scenario']);

    mockScenariosCopy.push({ ...newScenario, category: 'new category' });
    service.setScenariosData(mockScenariosCopy);

    expect(service.getState().categories).toEqual(['default', 'new category', 'scenario']);

    mockScenariosCopy.splice(1, 1);
    service.setScenariosData(mockScenariosCopy);

    expect(service.getState().categories).toEqual(['new category', 'scenario']);

    mockScenariosCopy[mockScenariosCopy.length - 1].category = 'updated category';
    service.setScenariosData(mockScenariosCopy);

    expect(service.getState().categories).toEqual(['scenario', 'updated category']);
  });

  it('should build an array of unique tags not falsy sorted alphabetically from scenarios', () => {
    const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));

    expect(service.getState().tags).toEqual([]);

    service.setScenariosData(mockScenariosCopy);

    expect(service.getState().tags).toEqual(['tag1', 'tag2', 'test']);

    mockScenariosCopy.push({ ...newScenario, tags: ['new tag', 'abc'] });
    service.setScenariosData(mockScenariosCopy);

    expect(service.getState().tags).toEqual(['abc', 'new tag', 'tag1', 'tag2', 'test']);

    mockScenariosCopy.splice(1, 1);
    service.setScenariosData(mockScenariosCopy);

    expect(service.getState().tags).toEqual(['abc', 'new tag', 'tag1', 'tag2']);

    mockScenariosCopy[mockScenariosCopy.length - 1].tags = ['zyx', 'abc'];
    service.setScenariosData(mockScenariosCopy);

    expect(service.getState().tags).toEqual(['abc', 'tag1', 'tag2', 'zyx']);
  });
});
