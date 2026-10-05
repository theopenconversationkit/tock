import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NbDialogRef, NbToggleModule, NbDatepickerModule } from '@nebular/theme';
import { of } from 'rxjs';

import { CreateFeatureComponent } from './create-feature.component';
import { TestSharedModule } from '../../../../shared/test-shared.module';

describe('CreateFeatureComponent', () => {
  let component: CreateFeatureComponent;
  let fixture: ComponentFixture<CreateFeatureComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateFeatureComponent],
      imports: [TestSharedModule, NbToggleModule, NbDatepickerModule.forRoot()],
      providers: [{ provide: NbDialogRef, useValue: { onClose: of(null), close: () => {} } }],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateFeatureComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
