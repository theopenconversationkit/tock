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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

export interface Pagination {
  size: number;
  start: number;
  end: number;
  total: number;
}

@Component({
  selector: 'tock-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss']
})
export class PaginationComponent implements OnInit {
  @Input() pagination!: Pagination;
  @Input() sizes: number[] = [10, 25, 50, 100];

  @Output() onPaginationChange = new EventEmitter<Pagination>();

  ngOnInit() {
    if (!this.sizes.includes(this.pagination.size)) {
      this.sizes = [...this.sizes, this.pagination.size].sort(function (a, b) {
        return a - b;
      });
    }
  }

  paginationPrevious(): void {
    let pageStart = this.pagination.start - this.pagination.size;
    if (pageStart < 0) pageStart = 0;
    this.pagination.start = pageStart;
    this.onPaginationChange.emit();
  }

  paginationNext(): void {
    this.pagination.start = this.pagination.start + this.pagination.size;
    this.onPaginationChange.emit();
  }

  paginationSize(): void {
    this.onPaginationChange.emit();
  }

  paginationString(): string {
    return `${this.pagination.start + 1} - ${this.pagination.end} of ${this.pagination.total}`;
  }

  showPrevious(): boolean {
    return this.pagination.start > 0;
  }

  showNext(): boolean {
    return this.pagination.total > this.pagination.end;
  }
}
