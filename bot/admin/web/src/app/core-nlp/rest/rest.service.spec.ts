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
    service.post('/test', {}, undefined, undefined, true, {}, true).subscribe({ error: (error) => (received = error) });
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

  it('still redirects to login on an authentication error', () => {
    const subscription = service.post('/test', {}, undefined, undefined, true, {}, true).subscribe();
    http.expectOne(service.url + '/test').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
    subscription.unsubscribe();
  });
});
