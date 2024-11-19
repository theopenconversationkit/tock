import { TestBed } from '@angular/core/testing';

import { TestDialogService } from './test-dialog.service';

describe('TestDialogService', () => {
  let service: TestDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TestDialogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
