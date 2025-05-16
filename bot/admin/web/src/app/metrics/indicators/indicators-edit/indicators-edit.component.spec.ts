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

import { SimpleChange } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormControl, FormGroup } from '@angular/forms';
import {
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbDialogRef,
  NbIconModule,
  NbInputModule,
  NbSpinnerModule,
  NbTagModule,
  NbTooltipModule
} from '@nebular/theme';
import { of } from 'rxjs';

import { DialogService } from '../../../core-nlp/dialog.service';
import { FormControlComponent } from '../../../shared/components';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { IndicatorDefinition } from '../../models';
import { IndicatorEdition } from '../indicators.component';
import { IndicatorsEditComponent } from './indicators-edit.component';

const mockIndicator = {
  existing: false,
  indicator: {
    name: '',
    label: '',
    description: '',
    values: [],
    dimensions: []
  }
};

describe('IndicatorsEditComponent', () => {
  let component: IndicatorsEditComponent;
  let fixture: ComponentFixture<IndicatorsEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IndicatorsEditComponent, FormControlComponent],
      imports: [
        TestSharedModule,
        NbCardModule,
        NbTooltipModule,
        NbButtonModule,
        NbInputModule,
        NbAutocompleteModule,
        NbIconModule,
        NbTagModule,
        NbSpinnerModule
      ],
      providers: [{ provide: DialogService, useValue: { openDialog: () => ({ onClose: (val: any) => of(val) }) } }]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IndicatorsEditComponent);
    component = fixture.componentInstance;
    component.indicatorEdition = mockIndicator;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize an empty form', () => {
    const indicatorEdition: IndicatorEdition = {
      existing: false,
      indicator: {
        name: '',
        label: '',
        description: '',
        values: [],
        dimensions: []
      }
    };
    component.ngOnChanges({ indicatorEdition: new SimpleChange(null, indicatorEdition, true) });
    fixture.detectChanges();

    expect(component.form.valid).toBeFalse();

    expect(component.form.value).toEqual({
      label: '',
      description: '',
      values: [
        {
          name: null,
          label: ''
        }
      ],
      dimensions: []
    });
  });

  it('should associate validators to the label', () => {
    expect(component.label.valid).toBeFalse();

    // label field is required
    expect(component.label.errors.required).toBeTrue();
    expect(component.label.errors.maxlength).toBeFalsy();
    expect(component.label.errors.custom).toBeFalsy();
    expect(component.label.valid).toBeFalse();

    // set label to long text (upper than 50 characters)
    component.label.setValue('a'.repeat(51));
    expect(component.label.errors.required).toBeFalsy();
    expect(component.label.errors.maxlength).toBeTruthy();
    expect(component.label.errors.maxlength.requiredLength).toBe(50);
    expect(component.label.errors.custom).toBeFalsy();
    expect(component.label.valid).toBeFalse();

    // set a label without at least one letter
    component.label.setValue('123');
    expect(component.label.errors.required).toBeFalsy();
    expect(component.label.errors.maxlength).toBeFalsy();
    expect(component.label.errors.custom).toEqual('Label must contain at least one letter');
    expect(component.label.valid).toBeFalse();

    // set a label without at least one letter
    component.indicators = [{ label: 'existing indicator label' } as IndicatorDefinition];
    component.label.setValue('existing indicator label');
    expect(component.label.errors.required).toBeFalsy();
    expect(component.label.errors.maxlength).toBeFalsy();
    expect(component.label.errors.custom).toEqual('There is already an indicator with the same label');
    expect(component.label.valid).toBeFalse();

    // set label to something correct
    component.label.setValue('correct value');
    expect(component.label.errors).toBeFalsy();
    expect(component.label.valid).toBeTrue();
  });

  it('should associate validators to the description', () => {
    expect(component.description.valid).toBeTrue();

    // set description to long text (upper than 500 characters)
    component.description.setValue('a'.repeat(501));
    expect(component.description.errors.maxlength).toBeTruthy();
    expect(component.description.valid).toBeFalse();
    expect(component.description.errors.maxlength.requiredLength).toBe(500);

    // set description to something correct
    component.description.setValue('correct value');
    expect(component.description.errors).toBeFalsy();
    expect(component.description.valid).toBeTrue();
  });

  it('should associate validators to the dimensions', () => {
    expect(component.dimensions.valid).toBeFalse();

    // dimensions is required
    expect(component.dimensions.errors.required).toBeTruthy();

    // set dimensions to something correct
    component.dimensions.push(new FormControl('test'));
    expect(component.dimensions.errors).toBeFalsy();
    expect(component.dimensions.valid).toBeTrue();
  });

  it('should associate validators to the values', () => {
    expect(component.values.valid).toBeFalse();

    // values is required
    expect(component.values.errors.required).toBeTruthy();

    // set values to something correct
    component.values.push(new FormGroup({ label: new FormControl('test') }));
    expect(component.values.errors).toBeFalsy();
    expect(component.values.valid).toBeTrue();
  });

  it('should define values name on save', () => {
    component.label.setValue('Test');
    component.dimensions.push(new FormControl('test'));
    component.values.push(new FormGroup({ label: new FormControl("test de label d'indicateur 1") }));
    component.save();
    expect(component.form.value.values).toEqual([
      {
        label: "test de label d'indicateur 1",
        name: 'testDeLabelDIndicateur1'
      }
    ]);
  });

  describe('#close', () => {
    it('should call the onClose method without displaying a confirmation request message when the form is not dirty', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('yes') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      component.close();

      expect(component['dialogService'].openDialog).not.toHaveBeenCalled();
      expect(component.onClose.emit).toHaveBeenCalledOnceWith(true);
    });

    it('should call the onClose method after displaying a confirmation request message and confirm when the form is dirty', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('yes') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      // To display the confirmation message, the form must have been modified
      component.form.markAsDirty();
      component.close();

      expect(component['dialogService'].openDialog).toHaveBeenCalled();
      expect(component.onClose.emit).toHaveBeenCalledOnceWith(true);
    });

    it('should not call the onClose method after displaying a confirmation request message and cancel when the form is dirty', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      // To display the confirmation message, the form must have been modified
      component.form.markAsDirty();
      component.close();

      expect(component['dialogService'].openDialog).toHaveBeenCalled();
      expect(component.onClose.emit).not.toHaveBeenCalled();
    });
  });
});
