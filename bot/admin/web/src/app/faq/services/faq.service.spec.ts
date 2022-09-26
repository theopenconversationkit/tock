import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RestService } from 'src/app/core-nlp/rest/rest.service';
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
        {
          provide: RestService,
          useValue: mockedRestApiService
        },
        { provide: FaqService, useClass: FaqService }
      ]
    });
    service = TestBed.inject(FaqService);

    it('should populate the state with the result when loading settings successfully', (done) => {
      mockedRestApiService.get.and.returnValue(of(mockSettings));

      expect(service.getState()).toEqual(initialState);

      service.getSettings('1').subscribe(() => {
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
});
