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
import { Component, HostListener, Inject, Input, OnInit } from '@angular/core';

@Component({
  selector: 'tock-scroll-top-button',
  templateUrl: './scroll-top-button.component.html',
  styleUrls: ['./scroll-top-button.component.scss']
})
export class ScrollTopButtonComponent {
  @Input() offset = 100;
  @Input() smoothness = 2;

  showScrollTopButton: boolean = false;

  constructor(@Inject(DOCUMENT) private document: Document) {}

  @HostListener('window:scroll')
  onPageScroll(): void {
    const currentScroll = this.document.documentElement.scrollTop || this.document.body.scrollTop || 0;

    this.showScrollTopButton = currentScroll > this.offset;
  }

  scrollToTop(): void {
    const currentScroll = this.document.documentElement.scrollTop || this.document.body.scrollTop;
    if (currentScroll > 0) {
      window.requestAnimationFrame(this.scrollToTop.bind(this));
      window.scrollTo(0, currentScroll - currentScroll / this.smoothness);
    }
  }
}
