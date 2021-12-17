import {isDocked, undock, ViewMode, dock, toggleSmallScreenMode, toggleWideScreenMode} from '../model/view-mode';
import {fromEvent, Observable} from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { OnInit } from '@angular/core';

const DEFAULT_PANEL_NAME = 'default';

// Typescript Mixin glue code
type Constructor<T = {}> = new(...args: any[]) => T;

/**
 * Enrich component to handle side-panel
 *
 * Currently only the toggle logic is provided
 * @param BaseClass
 * @constructor
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
     */
    isPanelDocked(name = DEFAULT_PANEL_NAME): boolean {
      return isDocked(this.viewMode) && (this.panelName === name);
    }

    isDocked(): boolean {
      return isDocked(this.viewMode);
    }

    protected dock(name = DEFAULT_PANEL_NAME): void {
      this.viewMode = dock(this.viewMode);
      this.panelName = name;
    }

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
      console.log("window width", window.innerWidth);

      if (window.innerWidth < 1620) {
        this.viewMode = toggleSmallScreenMode(this.viewMode);
      } else {
        this.viewMode = toggleWideScreenMode(this.viewMode);
      }
    }

  }
}
