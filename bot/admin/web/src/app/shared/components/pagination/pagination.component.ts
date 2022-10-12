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
