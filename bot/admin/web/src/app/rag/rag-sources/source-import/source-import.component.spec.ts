import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';
import { ProcessAdvancement, Source, SourceTypes } from '../models';

import { SourceImportComponent } from './source-import.component';

const sourceMock = {
  id: '654',
  enabled: false,
  name: 'Other kind of json source format',
  description: '',
  source_type: SourceTypes.file,
  status: ProcessAdvancement.complete,
  source_parameters: {
    file_format: 'json'
  }
};

describe('SourceImportComponent', () => {
  let component: SourceImportComponent;
  let fixture: ComponentFixture<SourceImportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SourceImportComponent],
      providers: [{ provide: NbDialogRef, useValue: { close: () => {} } }]
    }).compileComponents();

    fixture = TestBed.createComponent(SourceImportComponent);
    component = fixture.componentInstance;
    component.source = sourceMock as Source;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
