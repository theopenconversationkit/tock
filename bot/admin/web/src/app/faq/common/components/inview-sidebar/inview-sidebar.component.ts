import {EventEmitter, Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { ReplaySubject } from 'rxjs';

@Component({
  selector: 'tock-inview-sidebar',
  templateUrl: './inview-sidebar.component.html',
  styleUrls: ['./inview-sidebar.component.scss']
})
export class InviewSidebarComponent implements OnInit {

  @Output()
  onClose = new EventEmitter<void>();

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor() { }

  ngOnInit(): void {
  }

  close() {
    this.onClose.emit(null);
  }

}
