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
  selector: 'tock-sticky-menu',
  templateUrl: './sticky-menu.component.html',
  styleUrls: ['./sticky-menu.component.scss']
})
export class StickyMenuComponent implements OnInit {
  @Input() offset: number = 230;

  scrolled: boolean = false;
  prevScrollVal: number;

  constructor(@Inject(DOCUMENT) private document: Document) {}

  ngOnInit() {
    this.onPageScroll();
  }

  @HostListener('window:scroll')
  onPageScroll(): void {
    const verticalOffset = this.document.documentElement.scrollTop || this.document.body.scrollTop || 0;

    if (verticalOffset === 0 && this.prevScrollVal > this.offset) return; // deal with <nb-select> reseting page scroll when opening select

    this.scrolled = verticalOffset > this.offset ? true : false;
    this.prevScrollVal = verticalOffset;
  }
}
