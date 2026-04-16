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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';

import { CreateNamespaceComponent } from './create-namespace.component';
import { NbDialogRef } from '@nebular/theme';

describe('CreateNamespaceComponent', () => {
  let component: CreateNamespaceComponent;
  let fixture: ComponentFixture<CreateNamespaceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateNamespaceComponent],
      imports: [ReactiveFormsModule],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {}
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateNamespaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should generate the technical namespace name from the label', () => {
    component.label.setValue('Mon Namespace RH');

    expect(component.name.value).toBe('mon_namespace_rh');
  });

  it('should keep the manually edited technical namespace name', () => {
    component.label.setValue('Mon Namespace RH');
    component.name.setValue('rh_custom');
    component.label.setValue('Mon Namespace RH Final');

    expect(component.name.value).toBe('rh_custom');
  });
});
