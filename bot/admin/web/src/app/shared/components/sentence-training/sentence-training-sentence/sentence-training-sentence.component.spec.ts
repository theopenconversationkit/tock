import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { SentenceTrainingSentenceComponent } from './sentence-training-sentence.component';
import { TestSharedModule } from '../../../test-shared.module';
import { StateService } from '../../../../core-nlp/state.service';
import { StateServiceMock } from '../../../test-shared/state-service.mock';

describe('SentenceTrainingSentenceComponent', () => {
  let component: SentenceTrainingSentenceComponent;
  let fixture: ComponentFixture<SentenceTrainingSentenceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SentenceTrainingSentenceComponent],
      imports: [TestSharedModule],
      providers: [{ provide: StateService, useClass: StateServiceMock }],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(SentenceTrainingSentenceComponent);
    component = fixture.componentInstance;

    component.sentence = {
      getText: () => 'hello world',
      getEntities: () => []
    } as any;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
