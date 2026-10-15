import { of } from 'rxjs';
import { Dataset, DatasetRunState } from '../models';
import { DatasetsService } from './datasets.service';

describe('DatasetsService', () => {
  let service: DatasetsService;
  let rest: jasmine.SpyObj<any>;
  const snapshot = { indexSessionId: 'index-1' };
  const original = {
    id: 'dataset',
    name: 'Original',
    questions: [],
    runs: [{ id: 'run-1', state: DatasetRunState.COMPLETED, settingsSnapshot: snapshot }]
  } as Dataset;

  beforeEach(() => {
    rest = jasmine.createSpyObj('RestService', ['get', 'getArray', 'put']);
    service = new DatasetsService({ currentApplication: { name: 'bot' } } as any, rest);
    rest.get.and.returnValue(of(original));
    service.getDataset(original.id).subscribe();
  });

  it('merges fresh fields and runs while retaining snapshots by run ID', () => {
    rest.getArray.and.returnValue(
      of([
        {
          ...original,
          name: 'Updated',
          runs: [
            { id: 'run-2', state: DatasetRunState.RUNNING },
            { id: 'run-1', state: DatasetRunState.COMPLETED }
          ]
        }
      ])
    );

    service.getDatasets().subscribe();

    expect(service.datasets[0].name).toBe('Updated');
    expect(service.datasets[0].runs.length).toBe(2);
    expect(service.datasets[0].runs[0].settingsSnapshot).toBeUndefined();
    expect(service.datasets[0].runs[1].settingsSnapshot).toEqual(snapshot as any);
  });

  it('refreshes the detail even when settings were previously loaded', () => {
    rest.get.and.returnValue(of({ ...original, name: 'Changed elsewhere' }));

    service.getDataset(original.id).subscribe((dataset) => expect(dataset.name).toBe('Changed elsewhere'));

    expect(rest.get).toHaveBeenCalledTimes(2);
  });

  it('does not restore a run deleted on the server', () => {
    rest.getArray.and.returnValue(of([{ ...original, runs: [] }]));
    service.getDatasets().subscribe();
    expect(service.datasets[0].runs).toEqual([]);
  });
});
