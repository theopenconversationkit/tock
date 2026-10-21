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
import { Component, Input, inject } from '@angular/core';
import { Router } from '@angular/router';

import { OpenAnnotations, WidgetState } from '../../models/dashboard.model';
import { Annotation, AnnotationState } from '../../../shared/components/annotation/annotations';
import { ResponseIssueReasons } from '../../../shared/model/response-issue';

@Component({
  selector: 'tock-open-annotations',
  templateUrl: './open-annotations.component.html',
  styleUrls: ['./open-annotations.component.scss'],
  standalone: false
})
export class OpenAnnotationsComponent {
  private readonly router = inject(Router);

  @Input() data: OpenAnnotations = { total: 0, rows: [] };
  @Input() state: WidgetState = WidgetState.loading;
  @Input() namespace: string;
  @Input() applicationId: string;

  WidgetState = WidgetState;
  AnnotationState = AnnotationState;
  ResponseIssueReasons = ResponseIssueReasons;

  navigateToAnnotation(dialogId: string, actionId: string): void {
    this.router.navigate(['/analytics/dialogs', this.namespace, this.applicationId, dialogId], {
      fragment: actionId
    });
  }

  getReasonLabel(reason: string): string {
    return ResponseIssueReasons.find((r) => r.value === reason)?.label || reason;
  }
}
