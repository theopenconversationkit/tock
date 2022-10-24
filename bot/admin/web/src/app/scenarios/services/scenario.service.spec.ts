import { DatePipe } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ApplicationService } from '../../core-nlp/applications.service';

import { ScenarioGroup } from '../models';
import { ScenarioApiService } from './scenario.api.service';
import { ScenarioService } from './scenario.service';

const mockScenarios: ScenarioGroup[] = [
  {
    id: '1',
    name: 'Scenario 1',
    description: 'Description 1',
    category: '',
    tags: ['tag1', ''],
    creationDate: '12/01/1980',
    updateDate: null,
    versions: []
  },
  {
    id: '2',
    name: 'Scenario 2',
    description: 'Description 2',
    category: 'default',
    tags: ['test'],
    creationDate: '01/01/1970',
    updateDate: '01/01/1970',
    versions: []
  },
  {
    id: '3',
    name: 'Scenario 3',
    description: 'Description 3',
    category: null,
    tags: ['tag1', 'tag2'],
    creationDate: '01/01/1970',
    versions: []
  },
  {
    id: '4',
    name: 'Scenario 4',
    description: 'Description 4',
    category: 'scenario',
    tags: [],
    creationDate: '12/01/1980',
    versions: []
  },
  {
    id: '5',
    name: 'Scenario 5',
    description: 'Description 5',
    category: 'scenario',
    tags: null,
    creationDate: '12/01/1980',
    versions: []
  }
];

const initialState = {
  loaded: false,
  loading: false,
  scenariosGroups: [],
  tags: [],
  categories: []
};

const newScenario: ScenarioGroup = {
  id: null,
  creationDate: '12/01/1980',
  name: 'New scenario',
  tags: [],
  versions: []
};

const updatedScenario: ScenarioGroup = {
  ...mockScenarios[1],
  category: 'new category',
  description: 'Description updated',
  tags: ['test', 'new tag', 'JCVD forever']
};

describe('ScenarioService', () => {
  let service: ScenarioService;
  const mockedScenarioApiService: jasmine.SpyObj<ScenarioApiService> = jasmine.createSpyObj('ScenarioApiService', [
    'getScenariosGroups',
    'postScenarioGroup',
    'updateScenarioGroup',
    'deleteScenarioGroup'
  ]);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: ScenarioApiService,
          useValue: mockedScenarioApiService
        },
        { provide: ScenarioService, useClass: ScenarioService },
        {
          provide: ApplicationService,
          useValue: {
            retrieveCurrentApplication: () => {
              name: 'testApp';
            }
          }
        },
        {
          provide: DatePipe,
          useValue: {
            transform: (date) => date
          }
        }
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

  describe('#getScenariosGroups', () => {
    it('should populate the state with the result when loading scenarios groups successfully', (done) => {
      mockedScenarioApiService.getScenariosGroups.and.returnValue(of(mockScenarios));

      expect(service.getState()).toEqual(initialState);

      service.getScenariosGroups().subscribe(() => {
        const state = service.getState();
        expect(mockedScenarioApiService.getScenariosGroups).toHaveBeenCalled();
        expect(state.loaded).toBeTrue();
        expect(state.scenariosGroups).toHaveSize(mockScenarios.length);
        expect(state.scenariosGroups).toEqual(mockScenarios);
        expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
        expect(state.categories).toEqual(['default', 'scenario']);
        done();
      });
    });

    it('should not update state when loading scenarios groups fails', (done) => {
      mockedScenarioApiService.getScenariosGroups.and.returnValue(throwError(new Error()));

      expect(service.getState()).toEqual(initialState);

      service.getScenariosGroups().subscribe({
        error: () => {
          const state = service.getState();
          expect(mockedScenarioApiService.getScenariosGroups).toHaveBeenCalled();
          expect(state.loaded).toBeFalse();
          expect(state.scenariosGroups).toHaveSize(0);
          expect(state.tags).toHaveSize(0);
          expect(state.categories).toHaveSize(0);
          done();
        }
      });
    });
  });

  describe('#postScenarioGroup', () => {
    it('should add a new scenario group in the state and update tags and categories cache when published successfully', (done) => {
      mockedScenarioApiService.postScenarioGroup.and.returnValue(of({ ...newScenario, id: '1' }));

      expect(service.getState()).toEqual(initialState);
      service.setScenariosGroupsData(mockScenarios);

      service.postScenarioGroup(newScenario).subscribe(() => {
        const state = service.getState();
        expect(mockedScenarioApiService.postScenarioGroup).toHaveBeenCalledWith(newScenario);
        expect(state.scenariosGroups).toHaveSize(mockScenarios.length + 1);
        expect(state.scenariosGroups).toEqual([...mockScenarios, { ...newScenario, id: '1' }]);
        expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
        expect(state.categories).toEqual(['default', 'scenario']);
        done();
      });
    });

    it('should not update state when post scenario group fails', (done) => {
      mockedScenarioApiService.postScenarioGroup.and.returnValue(throwError(new Error()));

      expect(service.getState()).toEqual(initialState);

      service.postScenarioGroup(newScenario).subscribe({
        error: () => {
          const state = service.getState();
          expect(mockedScenarioApiService.postScenarioGroup).toHaveBeenCalled();
          expect(state.loaded).toBeFalse();
          expect(state.scenariosGroups).toHaveSize(0);
          expect(state.tags).toHaveSize(0);
          expect(state.categories).toHaveSize(0);
          done();
        }
      });
    });
  });

  describe('#updateScenarioGroup', () => {
    it('should update an existing scenario group and update tags and categories cache from the state when the scenario group is successfully updated', (done) => {
      mockedScenarioApiService.updateScenarioGroup.and.returnValue(of(updatedScenario));
      const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));

      service.setScenariosGroupsData(mockScenariosCopy);
      const state = service.getState();

      expect(state.scenariosGroups).toHaveSize(mockScenariosCopy.length);
      expect(state.scenariosGroups.find((s) => s.id === '2')).not.toEqual(updatedScenario);
      expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
      expect(state.categories).toEqual(['default', 'scenario']);

      service.updateScenarioGroup(updatedScenario).subscribe(() => {
        expect(mockedScenarioApiService.updateScenarioGroup).toHaveBeenCalled();
        expect(state.scenariosGroups).toHaveSize(mockScenariosCopy.length);
        expect(state.scenariosGroups.find((s) => s.id === '2')).toEqual(updatedScenario);
        expect(state.tags).toEqual(['JCVD forever', 'new tag', 'tag1', 'tag2', 'test']);
        expect(state.categories).toEqual(['new category', 'scenario']);
        done();
      });
    });

    it('should not update state when put scenario group fails', (done) => {
      mockedScenarioApiService.updateScenarioGroup.and.returnValue(throwError(new Error()));
      const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));

      service.setScenariosGroupsData(mockScenariosCopy);
      const state = service.getState();

      expect(state.scenariosGroups).toHaveSize(mockScenariosCopy.length);
      expect(state.scenariosGroups).toEqual(mockScenarios);
      expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
      expect(state.categories).toEqual(['default', 'scenario']);

      service.updateScenarioGroup(updatedScenario).subscribe({
        error: () => {
          expect(mockedScenarioApiService.updateScenarioGroup).toHaveBeenCalled();
          expect(state.scenariosGroups).toHaveSize(mockScenariosCopy.length);
          expect(state.scenariosGroups.find((s) => s.id === '2')).not.toEqual(updatedScenario);
          expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
          expect(state.categories).toEqual(['default', 'scenario']);
          done();
        }
      });
    });
  });

  describe('#deleteScenarioGroup', () => {
    it('should remove an existing scenario group and update tags and categories cache from the state when the scenario group is successfully deleted', (done) => {
      mockedScenarioApiService.deleteScenarioGroup.and.returnValue(of(false));
      const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));
      service.setScenariosGroupsData(mockScenariosCopy);
      const state = service.getState();

      expect(state.scenariosGroups.length).toBe(mockScenariosCopy.length);
      expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
      expect(state.categories).toEqual(['default', 'scenario']);

      service.deleteScenarioGroup('2').subscribe(() => {
        expect(mockedScenarioApiService.deleteScenarioGroup).toHaveBeenCalled();
        expect(state.scenariosGroups.length).toBe(--mockScenariosCopy.length);
        expect(state.scenariosGroups.find((s) => s.id === '2')).toBe(undefined);
        expect(state.tags).toEqual(['tag1', 'tag2']);
        expect(state.categories).toEqual(['scenario']);
        done();
      });
    });

    it('should not update state when delete scenario group fails', (done) => {
      mockedScenarioApiService.deleteScenarioGroup.and.returnValue(throwError(new Error()));
      const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));
      service.setScenariosGroupsData(mockScenariosCopy);
      const state = service.getState();

      expect(state.scenariosGroups.length).toBe(mockScenariosCopy.length);
      expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
      expect(state.categories).toEqual(['default', 'scenario']);

      service.deleteScenarioGroup('2').subscribe({
        error: () => {
          expect(mockedScenarioApiService.deleteScenarioGroup).toHaveBeenCalled();
          expect(state.scenariosGroups.length).toBe(mockScenariosCopy.length);
          expect(state.tags).toEqual(['tag1', 'tag2', 'test']);
          expect(state.categories).toEqual(['default', 'scenario']);
          done();
        }
      });
    });
  });

  describe('#getScenarioVersion', () => {});

  describe('#importScenarioGroup', () => {});

  describe('#postScenarioVersion', () => {});

  describe('#updateScenarioVersion', () => {});

  describe('#deleteScenarioVersion', () => {});

  it('should build an array of unique categories not falsy sorted alphabetically from scenarios groups', () => {
    const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));

    expect(service.getState().categories).toEqual([]);

    service.setScenariosGroupsData(mockScenariosCopy);

    expect(service.getState().categories).toEqual(['default', 'scenario']);

    mockScenariosCopy.push({ ...newScenario, category: 'new category' });
    service.setScenariosGroupsData(mockScenariosCopy);

    expect(service.getState().categories).toEqual(['default', 'new category', 'scenario']);

    mockScenariosCopy.splice(1, 1);
    service.setScenariosGroupsData(mockScenariosCopy);

    expect(service.getState().categories).toEqual(['new category', 'scenario']);

    mockScenariosCopy[mockScenariosCopy.length - 1].category = 'updated category';
    service.setScenariosGroupsData(mockScenariosCopy);

    expect(service.getState().categories).toEqual(['scenario', 'updated category']);
  });

  it('should build an array of unique tags not falsy sorted alphabetically from scenarios groups', () => {
    const mockScenariosCopy = JSON.parse(JSON.stringify(mockScenarios));

    expect(service.getState().tags).toEqual([]);

    service.setScenariosGroupsData(mockScenariosCopy);

    expect(service.getState().tags).toEqual(['tag1', 'tag2', 'test']);

    mockScenariosCopy.push({ ...newScenario, tags: ['new tag', 'abc'] });
    service.setScenariosGroupsData(mockScenariosCopy);

    expect(service.getState().tags).toEqual(['abc', 'new tag', 'tag1', 'tag2', 'test']);

    mockScenariosCopy.splice(1, 1);
    service.setScenariosGroupsData(mockScenariosCopy);

    expect(service.getState().tags).toEqual(['abc', 'new tag', 'tag1', 'tag2']);

    mockScenariosCopy[mockScenariosCopy.length - 1].tags = ['zyx', 'abc'];
    service.setScenariosGroupsData(mockScenariosCopy);

    expect(service.getState().tags).toEqual(['abc', 'tag1', 'tag2', 'zyx']);
  });
});
