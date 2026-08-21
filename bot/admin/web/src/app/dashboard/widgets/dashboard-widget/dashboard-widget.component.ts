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
import { Component, ContentChild, Input, TemplateRef } from '@angular/core';

import { WidgetState } from '../../models/dashboard.model';

/**
 * Shell shared by every dashboard widget. Owns the card chrome and the
 * loading / empty / unavailable / error states so each widget only has to
 * render its own content.
 */
@Component({
  selector: 'tock-dashboard-widget',
  templateUrl: './dashboard-widget.component.html',
  styleUrls: ['./dashboard-widget.component.scss'],
  standalone: false
})
export class DashboardWidgetComponent {
  @Input() title: string;
  @Input() icon: string;
  @Input() state: WidgetState = WidgetState.loading;

  @Input() skeletonLines: number = 4;

  @Input() emptyIcon: string = 'inbox';
  @Input() emptyTitle: string;
  @Input() emptyMessage: string;

  @Input() unavailableIcon: string = 'slash-circle';
  @Input() unavailableTitle: string;
  @Input() unavailableMessage: string;
  @Input() unavailableLinkLabel: string;
  @Input() unavailableLink: string;

  @Input() errorMessage: string;

  /**
   * Content is taken as templates rather than through <ng-content>: projected content
   * is instantiated by the parent whatever the state, so its bindings would evaluate
   * before the data arrives. Templates are only stamped when the widget is ready.
   */
  @ContentChild('widgetBody', { static: true }) bodyTemplate: TemplateRef<unknown>;

  /**
   * Static query: the template must sit directly under the widget tag. Wrapping it in
   * an @if would place it in an embedded view that this query cannot see, and the
   * footer would silently never render. Put the condition inside the template instead.
   */
  @ContentChild('widgetFooter', { static: true }) footerTemplate: TemplateRef<unknown>;

  WidgetState = WidgetState;

  get skeletonRows(): number[] {
    return Array.from({ length: this.skeletonLines }, (_, i) => i);
  }
}
