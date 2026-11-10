import { APP_BASE_HREF } from '@angular/common';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { RestService } from './rest.service';

describe('RestService silent POST errors', () => {
  let service: RestService;
  let http: HttpTestingController;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    router = jasmine.createSpyObj('router', ['navigateByUrl']);
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RestService, { provide: APP_BASE_HREF, useValue: '/' }, { provide: Router, useValue: router }]
    });
    service = TestBed.inject(RestService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('preserves a conflict response for the caller without a generic global error', () => {
    const globalError = spyOn(service.errorEmitter, 'emit');
    let received: HttpErrorResponse;
    service.postWithOptions('/test', {}, { silent: true }).subscribe({ error: (error) => (received = error) });
    http
      .expectOne(service.url + '/test')
      .flush({ errors: [{ message: 'vsi.management.errors.confirmation_changed' }] }, { status: 409, statusText: 'Conflict' });
    expect(received.status).toBe(409);
    expect(received.error.errors[0].message).toBe('vsi.management.errors.confirmation_changed');
    expect(globalError).not.toHaveBeenCalled();
  });

  it('keeps existing global error handling for ordinary requests', () => {
    const globalError = spyOn(service.errorEmitter, 'emit');
    service.post('/test', {}).subscribe({ error: () => undefined });
    http.expectOne(service.url + '/test').flush({}, { status: 500, statusText: 'Server error' });
    expect(globalError).toHaveBeenCalled();
  });

  it('preserves the URL, parameters and response parser of existing positional calls', () => {
    let result: number;
    service
      .post('/legacy', { value: 'test' }, (response) => response.count, '/custom', true, { page: 2 })
      .subscribe((value) => (result = value));
    const request = http.expectOne('/custom/legacy?page=2');
    expect(JSON.parse(request.request.body)).toEqual({ value: 'test' });
    request.flush({ count: 3 });
    expect(result).toBe(3);
  });

  it('still redirects to login on an authentication error', () => {
    const subscription = service.postWithOptions('/test', {}, { silent: true }).subscribe();
    http.expectOne(service.url + '/test').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
    subscription.unsubscribe();
  });
});
