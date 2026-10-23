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

import { KnowledgeBaseSyncBannerComponent } from './sync-banner.component';
import { CreateIndexBlocker, KnowledgeBaseSyncStatus } from '../../models';
import { TestSharedModule } from '../../../shared/test-shared.module';

function syncStatus(blocker: CreateIndexBlocker | null): KnowledgeBaseSyncStatus {
  return { createIndexBlocker: blocker } as unknown as KnowledgeBaseSyncStatus;
}

describe('KnowledgeBaseSyncBannerComponent', () => {
  let component: KnowledgeBaseSyncBannerComponent;
  let fixture: ComponentFixture<KnowledgeBaseSyncBannerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [KnowledgeBaseSyncBannerComponent],
      imports: [TestSharedModule],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(KnowledgeBaseSyncBannerComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('canOpenRagSettings', () => {
    it('offers the RAG settings link to an admin when RAG is not configured', () => {
      component.isAdmin = true;
      component.syncStatus = syncStatus(CreateIndexBlocker.RAG_NOT_CONFIGURED);

      expect(component.canOpenRagSettings).toBeTrue();
    });

    it('hides the link from a non-admin even when RAG is not configured', () => {
      component.isAdmin = false;
      component.syncStatus = syncStatus(CreateIndexBlocker.RAG_NOT_CONFIGURED);

      expect(component.canOpenRagSettings).toBeFalse();
    });

    it('does not offer the link for a different blocker', () => {
      component.isAdmin = true;
      component.syncStatus = syncStatus(CreateIndexBlocker.NO_PUBLISHED_ENTRY);

      expect(component.canOpenRagSettings).toBeFalse();
    });

    it('does not offer the link when nothing blocks creation', () => {
      component.isAdmin = true;
      component.syncStatus = syncStatus(null);

      expect(component.canOpenRagSettings).toBeFalse();
    });
  });

  describe('message keys by role', () => {
    it('keeps the actionable no-index message for an admin', () => {
      component.isAdmin = true;

      expect(component.noIndexMessageKey).toBe('knowledge-base.sync-banner.no_index_message');
    });

    it('gives a business user an informative no-index message', () => {
      component.isAdmin = false;

      expect(component.noIndexMessageKey).toBe('knowledge-base.sync-banner.no_index_message_non_admin');
    });

    it('keeps the actionable missing-index message for an admin', () => {
      component.isAdmin = true;

      expect(component.missingIndexMessageKey).toBe('knowledge-base.sync-banner.missing_index_message');
    });

    it('points the missing-index message to an admin for a business user', () => {
      component.isAdmin = false;

      expect(component.missingIndexMessageKey).toBe('knowledge-base.sync-banner.missing_index_message_non_admin');
    });

    it('keeps the undefined-embedding blocker actionable for an admin', () => {
      component.isAdmin = true;
      component.syncStatus = syncStatus(CreateIndexBlocker.EMBEDDING_MODEL_UNDEFINED);

      expect(component.blockerMessageKey).toBe('knowledge-base.job.create_blocked_embedding_model_undefined');
    });

    it('routes the undefined-embedding blocker to an admin for a business user', () => {
      component.isAdmin = false;
      component.syncStatus = syncStatus(CreateIndexBlocker.EMBEDDING_MODEL_UNDEFINED);

      expect(component.blockerMessageKey).toBe('knowledge-base.job.create_blocked_embedding_model_undefined_non_admin');
    });

    it('keeps the no-published-entry blocker identical for a business user (publishing is theirs)', () => {
      component.isAdmin = false;
      component.syncStatus = syncStatus(CreateIndexBlocker.NO_PUBLISHED_ENTRY);

      expect(component.blockerMessageKey).toBe('knowledge-base.job.create_blocked_no_published_entry');
    });
  });
});
