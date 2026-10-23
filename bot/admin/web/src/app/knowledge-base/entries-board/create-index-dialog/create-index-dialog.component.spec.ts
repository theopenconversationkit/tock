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

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';

import { KnowledgeBaseCreateIndexDialogComponent } from './create-index-dialog.component';
import { KnowledgeBaseIndexState } from '../../models';
import { TestSharedModule } from '../../../shared/test-shared.module';

describe('KnowledgeBaseCreateIndexDialogComponent', () => {
  let component: KnowledgeBaseCreateIndexDialogComponent;
  let fixture: ComponentFixture<KnowledgeBaseCreateIndexDialogComponent>;
  let dialogRef: { close: jasmine.Spy };

  beforeEach(async () => {
    dialogRef = { close: jasmine.createSpy('close') };

    await TestBed.configureTestingModule({
      declarations: [KnowledgeBaseCreateIndexDialogComponent],
      imports: [TestSharedModule],
      providers: [{ provide: NbDialogRef, useValue: dialogRef }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(KnowledgeBaseCreateIndexDialogComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('defaults from the index state', () => {
    it('defaults to switching and offers to create when there is no usable index (NONE)', () => {
      component.indexState = KnowledgeBaseIndexState.NONE;
      component.ngOnInit();

      expect(component.switchIndex).toBeTrue();
      expect(component.recreate).toBeFalse();
      expect(component.titleKey).toBe('knowledge-base.create-index-dialog.create_title');
      expect(component.confirmLabelKey).toBe('knowledge-base.create-index-dialog.create_button');
    });

    it('defaults to switching for a MISSING index', () => {
      component.indexState = KnowledgeBaseIndexState.MISSING;
      component.ngOnInit();

      expect(component.switchIndex).toBeTrue();
      expect(component.recreate).toBeFalse();
    });

    it('defaults to a standalone snapshot and offers to re-create over a READY index', () => {
      component.indexState = KnowledgeBaseIndexState.READY;
      component.ngOnInit();

      expect(component.switchIndex).toBeFalse();
      expect(component.recreate).toBeTrue();
      expect(component.titleKey).toBe('knowledge-base.create-index-dialog.recreate_title');
      expect(component.confirmLabelKey).toBe('knowledge-base.create-index-dialog.recreate_button');
    });
  });

  describe('dropped documentary rows', () => {
    it('counts the other rows only while switching onto the new index', () => {
      component.otherRowCount = 5;
      component.switchIndex = true;

      expect(component.droppedOtherRows).toBe(5);
    });

    it('drops nothing when the new index is kept aside', () => {
      component.otherRowCount = 5;
      component.switchIndex = false;

      expect(component.droppedOtherRows).toBe(0);
    });

    it('treats an unknown other-row count as zero', () => {
      component.otherRowCount = null;
      component.switchIndex = true;

      expect(component.droppedOtherRows).toBe(0);
    });
  });

  describe('closing', () => {
    it('confirms with the chosen switchIndex value', () => {
      component.switchIndex = true;
      component.confirm();

      expect(dialogRef.close).toHaveBeenCalledWith(true);
    });

    it('confirms with false when the index is kept aside', () => {
      component.switchIndex = false;
      component.confirm();

      expect(dialogRef.close).toHaveBeenCalledWith(false);
    });

    it('cancels with no value, so the caller tells it apart from a false confirmation', () => {
      component.cancel();

      expect(dialogRef.close).toHaveBeenCalledWith();
    });
  });
});
