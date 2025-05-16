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

import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { first } from 'rxjs/operators';

import { RestService } from '../../core-nlp/rest/rest.service';
import { Settings } from '../models';
import { FaqService } from './faq.service';

const mockSettings: Settings = {
  satisfactionEnabled: true,
  satisfactionStoryId: '1'
};

describe('FaqService', () => {
  let service: FaqService;
  const mockedRestApiService: jasmine.SpyObj<RestService> = jasmine.createSpyObj('RestService', ['get', 'post']);

  const initialState = {
    loaded: false,
    settings: {
      satisfactionEnabled: false,
      satisfactionStoryId: null
    }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FaqService,
        {
          provide: RestService,
          useValue: mockedRestApiService
        }
      ]
    });
    service = TestBed.inject(FaqService);
    service.setState(initialState);
  });

  it('should populate the state with the result when loading settings successfully', (done) => {
    mockedRestApiService.get.and.returnValue(of(mockSettings));

    expect(service.getState()).toEqual(initialState);

    service
      .getSettings('1')
      .pipe(first())
      .subscribe(() => {
        const state = service.getState();
        expect(mockedRestApiService.get).toHaveBeenCalled();
        expect(state.loaded).toBeTrue();
        expect(state.settings).toEqual(mockSettings);
        done();
      });
  });

  it('should not update state when loading settings fails', (done) => {
    mockedRestApiService.get.and.returnValue(throwError(new Error()));

    expect(service.getState()).toEqual(initialState);

    service.getSettings('1').subscribe({
      error: () => {
        expect(service.getState()).toEqual(initialState);
        done();
      }
    });
  });

  it('should update settings in the state when published successfully', (done) => {
    mockedRestApiService.post.and.returnValue(of(mockSettings));

    expect(service.getState()).toEqual(initialState);

    service.saveSettings('1', mockSettings).subscribe(() => {
      const state = service.getState();
      expect(state.settings).toEqual(mockSettings);
      done();
    });
  });

  it('should not update state when post settings fails', (done) => {
    mockedRestApiService.post.and.returnValue(throwError(new Error()));

    expect(service.getState()).toEqual(initialState);

    service.saveSettings('1', mockSettings).subscribe({
      error: () => {
        const state = service.getState();
        expect(state.settings).toEqual(initialState.settings);
        done();
      }
    });
  });
});
