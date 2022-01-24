/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import {isDocked, undock, ViewMode, dock, toggleSmallScreenMode, toggleWideScreenMode} from '../model/view-mode';
import {fromEvent, Observable} from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { OnInit } from '@angular/core';

export const DEFAULT_PANEL_NAME = 'default';

// Typescript Mixin glue code
type Constructor<T = {}> = new(...args: any[]) => T;

/**
 * Extends this Mixing to gain Dock/Undock' and 'small screen'/'wide screen' state
 *
 * Note: This will observe Window 'resize' event to toggle on/off ViewMode states
 */
export function WithSidePanel<T extends Constructor>(BaseClass: T= (class {} as any)) {
  return class extends BaseClass {

    viewMode: ViewMode = 'FULL_WIDTH';

    panelName = DEFAULT_PANEL_NAME;

    /**
     * Init function to be called in ngOnInit
     *
     * Observe window size variations to select appropriate view mode
     * @param destroy$
     */
    initSidePanel(destroy$: Observable<any>): void {
      this.adjustViewMode();
      this.observeWindowWidth(destroy$);
    }

    /**
     * Is a side panel opened
     * @param name (Optional) Name of specific Side panel
     */
    isPanelDocked(name = DEFAULT_PANEL_NAME): boolean {
      return isDocked(this.viewMode) && (this.panelName === name);
    }

    /**
     * Is any side panel opened
     */
    isDocked(): boolean {
      return isDocked(this.viewMode);
    }

    /**
     * Toggle On a Side Panel
     * @param name (Optional) Name of specific Side panel
     * @protected
     */
    protected dock(name = DEFAULT_PANEL_NAME): void {
      this.viewMode = dock(this.viewMode);
      this.panelName = name;
    }

    /**
     * Toggle Off active Side Panel if any
     * @protected
     */
    protected undock(): void {
      this.viewMode = undock(this.viewMode);
      this.panelName = DEFAULT_PANEL_NAME;
    }

    private observeWindowWidth(destroy$: Observable<any>): void {
      const screenSizeChanged$ = fromEvent(window, 'resize')
        .pipe(takeUntil(destroy$), debounceTime(1000))
        .subscribe(this.adjustViewMode.bind(this));
    }

    private adjustViewMode(): void {
      if (window.innerWidth < 1620) {
        this.viewMode = toggleSmallScreenMode(this.viewMode);
      } else {
        this.viewMode = toggleWideScreenMode(this.viewMode);
      }
      console.log("adjustViewMode",  this.viewMode);
    }

  }
}
