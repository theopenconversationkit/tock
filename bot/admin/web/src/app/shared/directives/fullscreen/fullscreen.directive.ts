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

import { DOCUMENT } from '@angular/common';
import { Directive, ElementRef, EventEmitter, HostListener, Inject, OnDestroy, OnInit, Output, Renderer2 } from '@angular/core';
import { NbThemeService, NbToastrService } from '@nebular/theme';
import { Subscription } from 'rxjs';

@Directive({
  selector: '[tockFullscreen]',
  exportAs: 'fullscreen'
})
export class FullscreenDirective implements OnInit, OnDestroy {
  @Output() onFullscreenChange = new EventEmitter<boolean>();
  @Output() onFullscreenError = new EventEmitter<string>();

  private subscription = new Subscription();

  constructor(
    @Inject(DOCUMENT) private document: Document,
    private elementRef: ElementRef,
    private themeService: NbThemeService,
    private toastrService: NbToastrService,
    private renderer: Renderer2
  ) {}

  ngOnInit(): void {
    this.subscription = this.themeService.onThemeChange().subscribe((theme: any) => {
      if (theme.name === 'default') {
        this.renderer.setStyle(this.elementRef.nativeElement, 'backgroundColor', '#edf1f7');
      } else if (theme.name === 'dark') {
        this.renderer.setStyle(this.elementRef.nativeElement, 'backgroundColor', '#151a30');
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Returns whether an element is currently in full screen
   * @returns {boolean}
   */
  public get isFullscreen(): boolean {
    return this.document.fullscreenElement != null;
  }

  /**
   * Method to switch from full screen mode to normal mode
   */
  public toggle(): void {
    if (this.isFullscreen) {
      this.close();
    } else {
      this.open();
    }
  }

  /**
   * Method to enter full screen mode.
   * Full screen is added to the body and a css class is added to the target element. This allows to benefit from the normal operation of Nebular
   */
  public async open(): Promise<void> {
    if (!this.isFullscreen) {
      try {
        await this.document.body.requestFullscreen();

        this.renderer.addClass(this.elementRef.nativeElement, 'wrapperFullscreen');
      } catch (e: any) {
        console.error(e);
        this.toastrService.danger('Unable to open full screen mode', 'Error', {
          duration: 5000
        });
        this.onFullscreenError.emit(e);
      }
    } else {
      this.toastrService.info('An element is already in full screen. Please close it before', 'Fullscreen', {
        duration: 5000
      });
    }
  }

  /**
   * Method to exit full screen mode
   */
  public async close(): Promise<void> {
    if (this.isFullscreen) {
      try {
        await this.document.exitFullscreen();

        this.renderer.removeClass(this.elementRef.nativeElement, 'wrapperFullscreen');
      } catch (e: any) {
        console.error(e);
        this.toastrService.danger('Unable to close full screen mode. Try pressing the "Esc" or "f11" key.', 'Error', {
          duration: 5000
        });
        this.onFullscreenError.emit(e);
      }
    }
  }

  @HostListener('document:fullscreenchange', ['$event'])
  onFullscreenChangeEvent(): void {
    this.onFullscreenChange.emit(this.isFullscreen);

    if (!this.isFullscreen && this.elementRef.nativeElement.classList.contains('wrapperFullscreen')) {
      this.renderer.removeClass(this.elementRef.nativeElement, 'wrapperFullscreen');
    }
  }
}
