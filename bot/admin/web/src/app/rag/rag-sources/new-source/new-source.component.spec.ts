import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';

import { NewSourceComponent } from './new-source.component';
import { Component, NO_ERRORS_SCHEMA } from '@angular/core';
import { ProcessAdvancement, SourceTypes } from '../models';
import { Validators } from '@angular/forms';

describe('NewSourceComponent', () => {
  let component: NewSourceComponent;
  let fixture: ComponentFixture<NewSourceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NewSourceComponent],
      providers: [{ provide: NbDialogRef, useValue: { close: () => {} } }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(NewSourceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should correctly initialize form on creation', () => {
    const expectedResult = {
      id: null,
      name: null,
      description: null,
      source_type: null,
      source_parameters: {
        source_url: null,
        exclusion_urls: [],
        addExclusionUrlInputControl: null,
        xpaths: [],
        addXPathInputControl: null,
        periodic_update: null,
        periodic_update_frequency: null
      }
    };

    expect(component.form.value).toEqual(expectedResult);

    component.form.patchValue({
      source_type: SourceTypes.file
    });
    expect(component.source_url.hasValidator(Validators.required)).toBeFalse();

    component.form.patchValue({
      source_type: SourceTypes.remote
    });
    expect(component.source_url.hasValidator(Validators.required)).toBeTrue();

    expect(component.periodic_update_frequency.hasValidator(Validators.required)).toBeFalse();
    component.form.get('source_parameters').patchValue({
      periodic_update: true
    });
    expect(component.periodic_update_frequency.hasValidator(Validators.required)).toBeTrue();
  });

  it('should correctly initialize form on update', () => {
    const expectedResult = {
      id: '123456789',
      name: 'Data source one',
      description: 'Data source one description',
      source_parameters: {
        source_url: new URL('https://www.sourceone.test/'),
        exclusion_urls: [new URL('https://www.sourceone.test/home'), new URL('https://www.sourceone.test/cgu')],
        addExclusionUrlInputControl: null,
        xpaths: ['//*[@id="st-faq-root"]/section/div/div[2]'],
        addXPathInputControl: null,
        periodic_update: true,
        periodic_update_frequency: 30
      }
    };

    component.source = {
      id: '123456789',
      name: 'Data source one',
      enabled: true,
      description: 'Data source one description',
      source_type: SourceTypes.remote,
      status: ProcessAdvancement.complete,
      source_parameters: {
        source_url: new URL('https://www.sourceone.test'),
        exclusion_urls: [new URL('https://www.sourceone.test/home'), new URL('https://www.sourceone.test/cgu')],
        xpaths: ['//*[@id="st-faq-root"]/section/div/div[2]'],
        periodic_update: true,
        periodic_update_frequency: 30
      },
      current_indexing_session_id: '111111111',
      indexing_sessions: [
        {
          id: '111111111',
          start_date: new Date('2023-07-24T12:06:11.106Z'),
          end_date: new Date('2023-07-24T14:22:07.106Z'),
          embeding_engine: 'text-embedding-ada-002',
          status: ProcessAdvancement.complete
        },
        {
          id: '222222222',
          start_date: new Date('2023-07-25T12:06:11.106Z'),
          end_date: new Date('2023-07-25T14:22:07.106Z'),
          embeding_engine: 'text-embedding-ada-002',
          status: ProcessAdvancement.running
        }
      ]
    };

    fixture.detectChanges();
    component.ngOnInit();

    expect(component.form.value).toEqual(expectedResult);
  });
});
