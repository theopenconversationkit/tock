/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { of } from 'rxjs';

import { RestService } from '../../../core-nlp/rest/rest.service';
import { StateService } from '../../../core-nlp/state.service';
import { FileUploadComponent } from '../../../shared/components/file-upload/file-upload.component';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { FaqManagementImportComponent } from './faq-management-import.component';

describe('FaqManagementImportComponent', () => {
  let component: FaqManagementImportComponent;
  let fixture: ComponentFixture<FaqManagementImportComponent>;
  let closeDialog: jasmine.Spy;
  let post: jasmine.Spy;

  beforeEach(async () => {
    closeDialog = jasmine.createSpy('closeDialog');
    post = jasmine.createSpy('post').and.returnValue(of(1));

    await TestBed.configureTestingModule({
      imports: [TestSharedModule],
      declarations: [FaqManagementImportComponent, FileUploadComponent],
      providers: [
        { provide: NbDialogRef, useValue: { close: closeDialog } },
        { provide: NbToastrService, useValue: { success: () => {} } },
        { provide: RestService, useValue: { post } },
        { provide: StateService, useValue: { currentApplication: { _id: 'application-id' } } }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(FaqManagementImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should import a valid FAQ export into the current application', fakeAsync(() => {
    const faqs = [
      {
        title: 'FAQ title',
        intentName: 'faq-intent',
        utterances: ['Question?'],
        answer: { i18n: [{ label: 'Answer' }] }
      }
    ];
    mockFileContent(JSON.stringify(faqs));
    component.file.setValue([new File([], 'faqs.json', { type: 'application/json' })]);

    component.import();
    tick();

    expect(post).toHaveBeenCalledWith('/faq/import/application-id', faqs);
    expect(closeDialog).toHaveBeenCalledWith(1);
  }));

  it('should reject an invalid JSON export without calling the API', fakeAsync(() => {
    mockFileContent('invalid json');
    component.file.setValue([new File([], 'faqs.json', { type: 'application/json' })]);

    component.import();
    tick();

    expect(component.fileFormatError).toBeTrue();
    expect(post).not.toHaveBeenCalled();
  }));

  function mockFileContent(content: string): void {
    spyOn(FileReader.prototype, 'readAsText').and.callFake(function (this: FileReader) {
      Object.defineProperty(this, 'result', { value: content });
      this.onload?.(new ProgressEvent('load') as ProgressEvent<FileReader>);
    });
  }
});
