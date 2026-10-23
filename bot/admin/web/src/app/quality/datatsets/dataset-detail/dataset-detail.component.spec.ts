import { of } from 'rxjs';
import { Dataset, DatasetRun, DatasetRunState } from '../models';
import { DatasetDetailComponent } from './dataset-detail.component';

describe('DatasetDetailComponent comparison selection', () => {
  let component: DatasetDetailComponent;
  let service: jasmine.SpyObj<any>;
  const latest = { id: 'latest', state: DatasetRunState.COMPLETED, startTime: '2026-10-14T12:00:00Z' } as DatasetRun;
  const older = { id: 'older', state: DatasetRunState.COMPLETED, startTime: '2026-10-14T11:00:00Z' } as DatasetRun;
  const active = { id: 'active', state: DatasetRunState.RUNNING, startTime: '2026-10-14T13:00:00Z' } as DatasetRun;

  function open(runs: DatasetRun[], compareRunId: string): void {
    service = jasmine.createSpyObj('DatasetsService', ['getDataset', 'getRunActions']);
    service.getDataset.and.returnValue(of({ id: 'dataset', questions: [], runs } as Dataset));
    service.getRunActions.and.returnValue(of([]));
    const route = { snapshot: { queryParams: { compareRunId } } };
    component = new DatasetDetailComponent(null, route as any, null, null, null, service, null, null, null);
    component.fetchDataset('dataset');
  }

  afterEach(() => component.ngOnDestroy());

  it('falls back to a different completed run when a direct link selects Run A again', () => {
    open([older, active, latest], latest.id);
    expect(component.currentRun.id).toBe(latest.id);
    expect(component.comparisonRun.id).toBe(older.id);
    expect(service.getRunActions.calls.allArgs()).toEqual([
      ['dataset', latest.id],
      ['dataset', older.id]
    ]);
  });

  it('loads a single run when no other completed run exists', () => {
    open([active, latest], latest.id);
    expect(component.currentRun.id).toBe(latest.id);
    expect(component.comparisonRun).toBeNull();
    expect(service.getRunActions).toHaveBeenCalledOnceWith('dataset', latest.id);
  });

  it('preserves a valid requested comparison', () => {
    const oldest = { ...older, id: 'oldest', startTime: '2026-10-14T10:00:00Z' };
    open([latest, older, oldest, active], oldest.id);
    expect(component.currentRun.id).toBe(latest.id);
    expect(component.comparisonRun.id).toBe(oldest.id);
  });

  it('detects changes to recorded compression settings', () => {
    open(
      [
        { ...latest, settingsSnapshot: { documentCompressor: { enabled: true, setting: { provider: 'VllmRerank', model: 'reranker' } } } },
        { ...older, settingsSnapshot: { documentCompressor: { enabled: false } } }
      ],
      older.id
    );
    expect(component.hasSettingsDiff()).toBeTrue();
  });

  it('does not interpret missing historical compression settings as a change', () => {
    open([{ ...latest, settingsSnapshot: { documentCompressor: { enabled: false } } }, older], older.id);
    expect(component.hasSettingsDiff()).toBeFalse();
  });
});
