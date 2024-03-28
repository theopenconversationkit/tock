import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JsonIteratorComponent } from './json-iterator.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ImportDataTypes } from '../../../models';

const rawDataMock = {
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
};

describe('JsonIteratorComponent', () => {
  let component: JsonIteratorComponent;
  let fixture: ComponentFixture<JsonIteratorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [JsonIteratorComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(JsonIteratorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize its path', () => {
    component.parentKey = 'title';
    component.parentType = 'object';
    component.upstreamPath = ['pages', 'body'];
    component.ngOnInit();
    expect(component.path).toEqual(['pages', 'body', 'title']);
  });

  it('should detect if the key is selected', () => {
    component.associations = [
      {
        type: ImportDataTypes.content,
        paths: [['pages', 'body', 'bloc']]
      },
      {
        type: ImportDataTypes.source_ref,
        paths: []
      }
    ];
    component.parentKey = '2';
    component.parentType = 'array';
    component.upstreamPath = ['pages', 'body'];
    component.recursiveList = {
      bloc: 'Pages body bloc data',
      title: 'Pages body bloc title'
    };

    component.ngOnInit();

    expect(component.path).toEqual(['pages', 'body']);
    expect(component.isSelected('body')).toEqual(null);
    expect(component.isSelected('bloc')).toEqual(ImportDataTypes.content);
  });
});
