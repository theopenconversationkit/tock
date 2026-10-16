import { CommonModule } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { NbToastrService } from '@nebular/theme';
import { NEVER, of, Subject, throwError } from 'rxjs';
import { Dataset, DatasetRun, DatasetRunState } from '../../models';
import { DatasetsService } from '../../services/datasets.service';
import { DialogService } from '../../../../core-nlp/dialog.service';
import { StateService } from '../../../../core-nlp/state.service';
import { TestDialogService } from '../../../../shared/components/test-dialog/test-dialog.service';
import { getTranslocoTestingModule } from '../../../../shared/test-shared.module';
import { DatasetsBoardEntryComponent } from './datasets-board-entry.component';

describe('DatasetsBoardEntryComponent polling', () => {
  const run = { id: 'run', state: DatasetRunState.RUNNING } as DatasetRun;
  let component: DatasetsBoardEntryComponent;
  let service: jasmine.SpyObj<any>;

  beforeEach(() => {
    service = jasmine.createSpyObj('DatasetsService', ['getRun', 'updateRunState', 'getRunDuration']);
    service.getRunDuration.and.returnValue(of('00:00:01'));
    component = new DatasetsBoardEntryComponent(service, null, null, null, null, null);
  });

  afterEach(() => component.ngOnDestroy());

  it('continues polling after a transient error and stops after completion', fakeAsync(() => {
    service.getRun.and.returnValues(
      throwError(() => new Error('503')),
      of({ ...run, state: DatasetRunState.COMPLETED })
    );
    component.dataset = { id: 'dataset', runs: [run] } as Dataset;
    tick(5000);
    expect(service.getRun).toHaveBeenCalledTimes(2);
    expect(service.updateRunState).toHaveBeenCalledWith('dataset', 'run', { state: DatasetRunState.COMPLETED, stats: undefined });
    tick(10000);
    expect(service.getRun).toHaveBeenCalledTimes(2);
  }));

  it('keeps a slow request in flight without cancelling or overlapping it', fakeAsync(() => {
    const response = new Subject<DatasetRun>();
    service.getRun.and.returnValue(response);
    component.dataset = { id: 'dataset', runs: [run] } as Dataset;
    tick(15000);
    expect(service.getRun).toHaveBeenCalledTimes(1);
    response.next({ ...run, state: DatasetRunState.COMPLETED });
    response.complete();
    expect(service.updateRunState).toHaveBeenCalled();
    tick(5000);
    expect(service.getRun).toHaveBeenCalledTimes(1);
  }));
});

describe('DatasetsBoardEntryComponent run statistics and comparisons', () => {
  let fixture: ComponentFixture<DatasetsBoardEntryComponent>;

  const completedRun = (id: string, startTime: string): DatasetRun =>
    ({
      id,
      startTime,
      state: DatasetRunState.COMPLETED,
      stats: {
        totalQuestions: 10,
        completedQuestions: 9,
        failedQuestions: 1,
        nonRagAnswers: 0,
        ragAnswerStatusCounts: { not_found_in_context: 2, found_in_context: 6, technical_error: 1 }
      }
    } as DatasetRun);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DatasetsBoardEntryComponent],
      imports: [CommonModule, getTranslocoTestingModule({ translocoConfig: { missingHandler: { logMissingKey: false } } })],
      providers: [
        {
          provide: DatasetsService,
          useValue: {
            getLatestRun: DatasetsService.prototype.getLatestRun,
            getRunDuration: () => of('00:00:01'),
            getAverageDurationPerQuestion: () => '1s',
            getRun: () => NEVER,
            updateRunNote: jasmine.createSpy('updateRunNote').and.returnValue(of({ note: 'Compressor OFF' }))
          }
        },
        { provide: DialogService, useValue: {} },
        { provide: StateService, useValue: {} },
        { provide: NbToastrService, useValue: { danger: jasmine.createSpy('danger') } },
        { provide: TestDialogService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(DatasetsBoardEntryComponent);
    fixture.componentInstance.displayRunsDetail = true;
  });

  function render(runs: DatasetRun[]): void {
    fixture.componentInstance.dataset = { id: 'dataset', name: 'Dataset', questions: [], runs } as Dataset;
    fixture.detectChanges();
  }

  it('displays the percentage of all questions even with failed questions and technical errors', () => {
    render([completedRun('finished', '2026-10-14T12:00:00Z')]);
    expect(fixture.nativeElement.querySelector('.runs-grid small.text-warning').textContent.trim()).toBe('(20%)');
  });

  it('offers comparison with the latest completed run while another run is active', () => {
    render([
      completedRun('latest-completed', '2026-10-14T12:00:00Z'),
      completedRun('older', '2026-10-14T11:00:00Z'),
      { ...completedRun('active', '2026-10-14T13:00:00Z'), state: DatasetRunState.RUNNING }
    ]);
    const rows = fixture.nativeElement.querySelectorAll('.runs-grid:not(.runs-grid-header)');
    expect(rows[0].querySelector('[icon="plus-slash-minus"]')).toBeNull();
    expect(rows[1].querySelector('[icon="plus-slash-minus"]')).not.toBeNull();
    expect(rows[2].querySelector('[icon="plus-slash-minus"]')).toBeNull();
  });

  it('does not offer a comparison with only one completed run', () => {
    render([
      completedRun('only-completed', '2026-10-14T12:00:00Z'),
      { ...completedRun('active', '2026-10-14T13:00:00Z'), state: DatasetRunState.RUNNING }
    ]);
    expect(fixture.nativeElement.querySelector('[icon="plus-slash-minus"]')).toBeNull();
  });

  function editNote(value: string): HTMLInputElement {
    fixture.nativeElement.querySelector('.note-display').click();
    fixture.detectChanges();
    const input: HTMLInputElement = fixture.nativeElement.querySelector('.note-editor input');
    input.value = value;
    input.dispatchEvent(new Event('input'));
    return input;
  }

  it('keeps the focused draft when polling replaces the run object', () => {
    const run = { ...completedRun('active', '2026-10-14T13:00:00Z'), state: DatasetRunState.RUNNING, note: 'Compressor OFF' };
    render([run]);
    const input = editNote('Compressor ON');
    expect(document.activeElement).toBe(input);
    render([{ ...run, stats: { ...run.stats, completedQuestions: 10 } }]);
    expect(fixture.nativeElement.querySelector('.note-editor input')).toBe(input);
    expect(input.value).toBe('Compressor ON');
    expect(document.activeElement).toBe(input);
  });

  it('saves a trimmed note with Enter and prevents duplicate saves while waiting', () => {
    const service = TestBed.inject(DatasetsService);
    const response = new Subject<{ note: string | null }>();
    (service.updateRunNote as jasmine.Spy).and.returnValue(response);
    render([completedRun('finished', '2026-10-14T12:00:00Z')]);
    const input = editNote('  Compressor OFF  ');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    fixture.detectChanges();
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    expect(service.updateRunNote).toHaveBeenCalledOnceWith('dataset', 'finished', 'Compressor OFF');
    expect(input.disabled).toBeTrue();
    response.next({ note: 'Compressor OFF' });
    response.complete();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.note-editor')).toBeNull();
  });

  it('cancels with Escape without changing the saved note', () => {
    render([{ ...completedRun('finished', '2026-10-14T12:00:00Z'), note: 'Compressor OFF' }]);
    editNote('Compressor ON').dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(TestBed.inject(DatasetsService).updateRunNote).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('.note-display').textContent).toContain('Compressor OFF');
  });

  it('keeps the draft and shows an error when saving fails, allowing a retry', () => {
    const service = TestBed.inject(DatasetsService);
    (service.updateRunNote as jasmine.Spy).and.returnValues(
      throwError(() => new Error('503')),
      of({ note: 'Compressor ON' })
    );
    render([completedRun('finished', '2026-10-14T12:00:00Z')]);
    const input = editNote('Compressor ON');
    fixture.nativeElement.querySelector('.save-note').click();
    fixture.detectChanges();
    expect(TestBed.inject(NbToastrService).danger).toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('.note-editor input')).toBe(input);
    expect(input.value).toBe('Compressor ON');
    expect(input.disabled).toBeFalse();
    fixture.nativeElement.querySelector('.save-note').click();
    fixture.detectChanges();
    expect(service.updateRunNote).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.querySelector('.note-editor')).toBeNull();
  });

  it('allows clearing a note and limits input to 200 characters', () => {
    render([{ ...completedRun('finished', '2026-10-14T12:00:00Z'), note: 'Compressor OFF' }]);
    const input = editNote('');
    expect(input.maxLength).toBe(200);
    fixture.nativeElement.querySelector('.save-note').click();
    expect(TestBed.inject(DatasetsService).updateRunNote).toHaveBeenCalledOnceWith('dataset', 'finished', '');
  });
});
