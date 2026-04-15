import { DOCUMENT } from '@angular/common';
import { Component, HostListener, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { NbTooltipDirective } from '@nebular/theme';

@Component({
  selector: 'tock-sticky-menu',
  templateUrl: './sticky-menu.component.html',
  styleUrls: ['./sticky-menu.component.scss']
})
export class StickyMenuComponent implements OnInit {
  @Input() offset: number = 230;
  @Input() hideable: boolean = false;

  @ViewChild(NbTooltipDirective) tooltip: NbTooltipDirective;

  scrolled: boolean = false;
  hidden: boolean = false;
  prevScrollVal: number;

  constructor(@Inject(DOCUMENT) private document: Document) {}

  ngOnInit() {
    this.onPageScroll();
  }

  @HostListener('window:scroll')
  onPageScroll(): void {
    const verticalOffset = this.document.documentElement.scrollTop || this.document.body.scrollTop || 0;

    if (verticalOffset === 0 && this.prevScrollVal > this.offset) return;

    this.scrolled = verticalOffset > this.offset ? true : false;
    this.prevScrollVal = verticalOffset;
  }

  swapDisplay() {
    this.hidden = !this.hidden;
    this.tooltip?.hide();
  }
}
