import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SampleCreateComponent } from './sample-create.component';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { getNbDialogRefMock, getNbTestProviders } from '../../../shared/test-shared/nb-mocks';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from '../../../core-nlp/state.service';
import { StateServiceMock } from '../../../shared/test-shared/state-service.mock';

describe('SampleCreateComponent', () => {
  let component: SampleCreateComponent;
  let fixture: ComponentFixture<SampleCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SampleCreateComponent],
      imports: [TestSharedModule],
      providers: [
        { provide: NbDialogRef, useValue: getNbDialogRefMock() },
        { provide: StateService, useClass: StateServiceMock }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(SampleCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
