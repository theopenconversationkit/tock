import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';
import { ProcessAdvancement, Source, SourceTypes } from '../../models';

import { SourceNormalizationCsvComponent } from './source-normalization-csv.component';

const sourceMock = {
  id: '654',
  enabled: false,
  name: 'Other kind of json source format',
  description: '',
  source_type: SourceTypes.file,
  status: ProcessAdvancement.complete,
  source_parameters: {
    file_format: 'json'
  },
  rawData: []
};

describe('SourceNormalizationCsvComponent', () => {
  let component: SourceNormalizationCsvComponent;
  let fixture: ComponentFixture<SourceNormalizationCsvComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SourceNormalizationCsvComponent],
      providers: [{ provide: NbDialogRef, useValue: { close: () => {} } }]
    }).compileComponents();

    fixture = TestBed.createComponent(SourceNormalizationCsvComponent);
    component = fixture.componentInstance;
    component.source = sourceMock as Source;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
