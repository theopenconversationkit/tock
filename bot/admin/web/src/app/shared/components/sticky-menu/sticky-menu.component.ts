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
