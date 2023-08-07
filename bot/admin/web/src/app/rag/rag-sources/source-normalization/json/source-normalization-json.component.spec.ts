import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';
import { ImportDataTypes, ProcessAdvancement, Source, SourceTypes } from '../../models';

import { SourceNormalizationJsonComponent } from './source-normalization-json.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';

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
  rawData: {
    pages: [
      {
        id: '123456789',
        body: [
          {
            bloc: 'Page 1 body bloc 1'
          },
          {
            bloc: 'Page 1 body bloc 2'
          },
          {
            bloc: 'Page 1 body bloc 3',
            title: 'Page 1 body title 3'
          }
        ]
      },
      {
        id: '987654321',
        body: [
          {
            bloc: 'Page 2 body bloc 1',
            title: 'Page 2 body title 1'
          },
          {
            bloc: 'Page 2 body bloc 2'
          }
        ]
      }
    ]
  }
};
describe('SourceNormalizationJsonComponent', () => {
  let component: SourceNormalizationJsonComponent;
  let fixture: ComponentFixture<SourceNormalizationJsonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SourceNormalizationJsonComponent],
      providers: [{ provide: NbDialogRef, useValue: { close: () => {} } }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(SourceNormalizationJsonComponent);
    component = fixture.componentInstance;
    component.source = sourceMock as Source;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit if content type association was not made', () => {
    spyOn(component.onNormalize, 'emit');
    component.submit();
    expect(component.onNormalize.emit).not.toHaveBeenCalled();
  });

  it('should submit if content type association was made', () => {
    spyOn(component.onNormalize, 'emit');
    component.associations = [
      {
        type: 'content' as ImportDataTypes,
        paths: [['pages', 'body', 'bloc']]
      },
      {
        type: 'source_ref' as ImportDataTypes,
        paths: []
      }
    ];
    component.submit();
    expect(component.onNormalize.emit).toHaveBeenCalled();
  });

  it('should collect data according to associations', () => {
    component.associations = [
      {
        type: 'content' as ImportDataTypes,
        paths: [
          ['pages', 'body', 'bloc'],
          ['pages', 'body', 'title']
        ]
      },
      {
        type: 'source_ref' as ImportDataTypes,
        paths: [['pages', 'id']]
      }
    ];
    fixture.detectChanges();

    const expectedResult = [
      {
        content: 'Page 1 body bloc 1,Page 1 body bloc 2,Page 1 body bloc 3 ,,Page 1 body title 3',
        source_ref: '123456789'
      },
      {
        content: 'Page 2 body bloc 1,Page 2 body bloc 2 Page 2 body title 1,',
        source_ref: '987654321'
      }
    ];

    expect(component.gatherData(component.source.rawData)).toEqual(expectedResult);
  });
});
