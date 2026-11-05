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
import { NbDialogRef } from '@nebular/theme';

import { DebugViewerDialogComponent } from './debug-viewer-dialog.component';
import { TranslocoService } from '@jsverse/transloco';
import { TestSharedModule } from '../../test-shared.module';

describe('DebugViewerComponent', () => {
  let component: DebugViewerDialogComponent;
  let fixture: ComponentFixture<DebugViewerDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DebugViewerDialogComponent],
      imports: [TestSharedModule],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {
            close: () => {}
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DebugViewerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show the truncated badge and the row icon for truncated documents', () => {
    const truncatedFixture = TestBed.createComponent(DebugViewerDialogComponent);
    truncatedFixture.componentRef.setInput('debug', {
      documents: [
        { content: 'a', metadata: { id: '1', chunk: '1/1', title: 'A', truncated: true, original_length: 30000, truncated_length: 20050 } },
        { content: 'b', metadata: { id: '2', chunk: '1/1', title: 'B' } },
        { content: 'c', metadata: { id: '3', chunk: '1/1', title: 'C', truncated: true, original_length: 40000, truncated_length: 20050 } }
      ]
    });
    truncatedFixture.detectChanges();

    expect(truncatedFixture.componentInstance.truncatedCount).toBe(2);
    const el: HTMLElement = truncatedFixture.nativeElement;
    expect(el.querySelector('.badge--truncated')?.textContent).toContain('2');
    expect(el.querySelectorAll('.doc-item__truncated').length).toBe(2);
  });
});
