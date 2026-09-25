import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnnotationComponent } from './annotation.component';
import { TestSharedModule } from '../../test-shared.module';
import { StateService } from '../../../core-nlp/state.service';
import { StateServiceMock } from '../../test-shared/state-service.mock';
import { getNbDialogRefMock } from '../../test-shared/nb-mocks';
import { NbDialogRef, NbRadioModule } from '@nebular/theme';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('AnnotationComponent', () => {
  let component: AnnotationComponent;
  let fixture: ComponentFixture<AnnotationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AnnotationComponent],
      imports: [TestSharedModule],
      providers: [
        { provide: NbDialogRef, useValue: getNbDialogRefMock() },
        { provide: StateService, useClass: StateServiceMock }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(AnnotationComponent);
    component = fixture.componentInstance;

    component.actionReport = {
      message: { text: 'answer text' },
      ragDebug: undefined,
      annotation: undefined
    } as any;

    component.dialogReport = {
      actions: []
    } as any;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
