import { of, Subject, throwError } from 'rxjs';
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

  it('preserves concurrent state updates when a note is saved and preserves the note during polling', () => {
    const response = new Subject<{ note: string | null }>();
    rest.put.and.returnValue(response);
    service.updateRunNote('dataset', 'run-1', 'Compressor OFF').subscribe();
    expect(rest.put).toHaveBeenCalledWith('/bots/bot/datasets/dataset/runs/run-1/note', { note: 'Compressor OFF' }, jasmine.any(Function));
    service.updateRunState('dataset', 'run-1', { state: DatasetRunState.CANCELLED, endTime: '2026-10-16T12:00:00Z' });
    response.next({ note: 'Compressor OFF' });
    response.complete();
    expect(service.datasets[0].runs[0]).toEqual({
      ...original.runs[0],
      state: DatasetRunState.CANCELLED,
      endTime: '2026-10-16T12:00:00Z',
      note: 'Compressor OFF'
    });
    service.updateRunState('dataset', 'run-1', { state: DatasetRunState.CANCELLED });
    expect(service.datasets[0].runs[0].note).toBe('Compressor OFF');

    rest.put.and.returnValue(of({ note: null }));
    service.updateRunNote('dataset', 'run-1', '').subscribe();
    expect(service.datasets[0].runs[0].note).toBeNull();
  });

  it('does not change the cached note after a failed save', () => {
    rest.put.and.returnValue(of({ note: 'Compressor OFF' }));
    service.updateRunNote('dataset', 'run-1', 'Compressor OFF').subscribe();
    rest.put.and.returnValue(throwError(() => new Error('503')));
    service.updateRunNote('dataset', 'run-1', 'Compressor ON').subscribe({ error: () => {} });
    expect(service.datasets[0].runs[0].note).toBe('Compressor OFF');
  });

  it('does not restore a removed run when a delayed note response arrives', () => {
    const response = new Subject<{ note: string | null }>();
    rest.put.and.returnValue(response);
    service.updateRunNote('dataset', 'run-1', 'Compressor OFF').subscribe();
    rest.getArray.and.returnValue(of([{ ...original, runs: [] }]));
    service.getDatasets().subscribe();
    response.next({ note: 'Compressor OFF' });
    response.complete();
    expect(service.datasets[0].runs).toEqual([]);
  });
});
