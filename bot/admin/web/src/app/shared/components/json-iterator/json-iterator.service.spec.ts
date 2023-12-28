import { TestBed } from '@angular/core/testing';

import { JsonIteratorService } from './json-iterator.service';

describe('JsonIteratorService', () => {
  let service: JsonIteratorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(JsonIteratorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
