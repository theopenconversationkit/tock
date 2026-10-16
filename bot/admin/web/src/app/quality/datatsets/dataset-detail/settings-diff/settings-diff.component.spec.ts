import { CommonModule } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';
import { DatasetRun, DatasetRunSettingsSnapshot } from '../../models';
import { SettingsService } from '../../../../core-nlp/settings.service';
import { getTranslocoTestingModule } from '../../../../shared/test-shared.module';
import { MarkdownDiffService } from '../../services/markdown-diff.service';
import { DatasetDetailSettingsDiffComponent, SettingsDiffCurrentTabs } from './settings-diff.component';
import translations from '../../../../../assets/i18n/quality/en.json';

describe('DatasetDetailSettingsDiffComponent compression settings', () => {
  let fixture: ComponentFixture<DatasetDetailSettingsDiffComponent>;
  let component: DatasetDetailSettingsDiffComponent;

  const vllm = {
    enabled: true,
    setting: { provider: 'VllmRerank', model: 'reranker-v1', minScore: 0.5, maxDocuments: 4, fillToMaxDocuments: true }
  };
  const run = (settingsSnapshot: DatasetRunSettingsSnapshot): DatasetRun => ({ settingsSnapshot } as DatasetRun);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DatasetDetailSettingsDiffComponent],
      imports: [CommonModule, getTranslocoTestingModule({ langs: { en: { quality: translations } } })],
      providers: [
        { provide: NbDialogRef, useValue: {} },
        { provide: SettingsService, useValue: {} },
        { provide: MarkdownDiffService, useValue: jasmine.createSpyObj('MarkdownDiffService', ['diff']) }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(DatasetDetailSettingsDiffComponent);
    component = fixture.componentInstance;
    component.currentTab = SettingsDiffCurrentTabs.CompressionSettings;
    component.currentRun = run({ documentCompressor: vllm });
  });

  it('opens the compression tab and compares provider-specific fields for Bloomz and vLLM', () => {
    component.currentTab = SettingsDiffCurrentTabs.RagSettings;
    component.comparisonRun = run({ documentCompressor: { enabled: true, setting: { provider: 'BloomzRerank', label: 'entailment' } } });
    fixture.detectChanges();
    const button = Array.from<HTMLElement>(fixture.nativeElement.querySelectorAll('button')).find(
      (element) => element.textContent.trim() === 'Compression Settings'
    );
    button.click();
    fixture.detectChanges();

    const panels = fixture.nativeElement.querySelectorAll('code');
    expect(panels[0].textContent).toContain('VllmRerank');
    expect(panels[0].textContent).toContain('reranker-v1');
    expect(panels[0].textContent).toContain('fillToMaxDocuments');
    expect(panels[1].textContent).toContain('BloomzRerank');
    expect(panels[1].textContent).toContain('entailment');
    expect(fixture.nativeElement.querySelector('.diff-changed')).not.toBeNull();
  });

  it('highlights changes to the model, thresholds and enabled state', () => {
    component.comparisonRun = run({
      documentCompressor: { enabled: false, setting: { ...vllm.setting, model: 'reranker-v2', minScore: 0.8 } }
    });
    fixture.detectChanges();
    const panels = fixture.nativeElement.querySelectorAll('code');
    expect(panels[0].textContent).toContain('0.5');
    expect(panels[1].textContent).toContain('0.8');
    expect(panels[1].textContent).toContain('reranker-v2');
    expect(panels[1].textContent).toContain('false');
    expect(panels[0].querySelectorAll('.diff-changed').length).toBeGreaterThanOrEqual(3);
  });

  it('shows disabled compression for a single run without reporting missing history', () => {
    component.currentRun = run({ documentCompressor: { enabled: false } });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('code').length).toBe(1);
    expect(fixture.nativeElement.querySelector('code').textContent).toContain('false');
    expect(fixture.nativeElement.querySelector('p.text-muted')).toBeNull();
  });

  it('shows missing historical settings without inventing differences against a newer run', () => {
    component.comparisonRun = run({});
    component.htmlRunA = 'A previously viewed prompt';
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('p.text-muted').textContent).toContain('not recorded');
    expect(fixture.nativeElement.querySelectorAll('code').length).toBe(1);
    expect(fixture.nativeElement.querySelector('code').textContent).toContain('VllmRerank');
    expect(fixture.nativeElement.querySelector('.diff-added, .diff-removed, .diff-changed')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('A previously viewed prompt');
  });

  it('handles two older runs without settings snapshots', () => {
    component.currentRun = {} as DatasetRun;
    component.comparisonRun = {} as DatasetRun;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('p.text-muted').length).toBe(2);
    expect(fixture.nativeElement.querySelector('code')).toBeNull();
  });

  it('does not highlight identical compressor settings', () => {
    component.comparisonRun = run({ documentCompressor: { ...vllm, setting: { ...vllm.setting } } });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.diff-added, .diff-removed, .diff-changed')).toBeNull();
  });

  it('keeps compressor settings out of the RAG settings tab', () => {
    component.currentRun = run({ maxDocumentsRetrieved: 8, documentCompressor: vllm });
    component.currentTab = SettingsDiffCurrentTabs.RagSettings;
    fixture.detectChanges();
    const settings = fixture.nativeElement.querySelector('code').textContent;
    expect(settings).toContain('maxDocumentsRetrieved');
    expect(settings).not.toContain('documentCompressor');
    expect(settings).not.toContain('VllmRerank');
  });
});
