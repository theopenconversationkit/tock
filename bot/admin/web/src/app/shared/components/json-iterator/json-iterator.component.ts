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

import { AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, Input, OnDestroy } from '@angular/core';
import { isPrimitive } from '../../utils';
import { JsonIteratorService } from './json-iterator.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'tock-json-iterator',
  templateUrl: './json-iterator.component.html',
  styleUrls: ['./json-iterator.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class JsonIteratorComponent implements AfterViewInit, OnDestroy {
  destroy = new Subject();

  @Input() data: { [key: string]: any };
  @Input() isRoot: boolean = true;
  @Input() parentKey: string;
  @Input() customOrder: string[] = [];

  width = 0;
  private resizeObserver!: ResizeObserver;

  isDeployed: boolean = false;

  isPrimitive = isPrimitive;

  constructor(private jsonIteratorService: JsonIteratorService, private elementRef: ElementRef, private cdr: ChangeDetectorRef) {
    this.jsonIteratorService.communication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type === 'expand' && !this.isDeployed) {
        this.isDeployed = true;
        setTimeout(() => {
          this.expandAll();
        });
      }
    });
  }

  ngAfterViewInit() {
    this.width = this.elementRef.nativeElement.offsetWidth;
    this.resizeObserver = new ResizeObserver((entries) => {
      for (const entry of entries) {
        this.width = Math.floor(entry.contentRect.width);
        this.cdr.detectChanges();
      }
    });
    this.resizeObserver.observe(this.elementRef.nativeElement);
    this.cdr.detectChanges();
  }

  customSort = (a: { key: string }, b: { key: string }) => {
    const order = this.customOrder;
    const aIndex = order.indexOf(a.key);
    const bIndex = order.indexOf(b.key);

    if (aIndex !== -1 && bIndex !== -1) {
      return aIndex - bIndex;
    }

    if (aIndex !== -1) return -1;
    if (bIndex !== -1) return 1;

    return a.key.localeCompare(b.key);
  };

  isExpandable(): boolean {
    if (!this.isPrimitive(this.data)) return true;

    const fontSize = 12;
    const maxChars = Math.floor(this.width / (0.6 * fontSize));
    const fullTextLength = String(this.parentKey).length + String(this.data).length + 6; // +6 for parent name, space, colon, space and quotes around the value

    return fullTextLength >= maxChars;
  }

  switchDeployed() {
    this.isDeployed = !this.isDeployed;
    this.cdr.detectChanges();
  }

  expandAll() {
    this.jsonIteratorService.expandAll();
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.resizeObserver.disconnect();
    this.destroy.next(true);
    this.destroy.complete();
  }
}
