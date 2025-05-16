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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { StateService } from '../../../../core-nlp/state.service';
import { TestSharedModule } from '../../../../shared/test-shared.module';
import { StoriesUploadComponent } from './stories-upload.component';
import { DialogService } from '../../../../core-nlp/dialog.service';
import { RestService } from '../../../../core-nlp/rest/rest.service';
import { of } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('StoriesUploadComponent', () => {
  let component: StoriesUploadComponent;
  let fixture: ComponentFixture<StoriesUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule],
      providers: [
        {
          provide: NbToastrService,
          useValue: { show: () => {} }
        },
        {
          provide: StateService,
          useValue: { currentApplication: { name: 'TestApp', namespace: 'TestNamespace' }, currentLocale: 'fr' }
        },
        {
          provide: NbDialogRef<StoriesUploadComponent>,
          useValue: {close:()=>{}}
        },
        {
          provide: DialogService,
          useValue: {openDialog:()=>{}}
        },
        {
          provide: RestService,
          useValue: {get:()=>of()}
        }
      ],
      declarations: [StoriesUploadComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(StoriesUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
