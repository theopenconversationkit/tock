import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'tock-train-grid-item',
  templateUrl: './train-grid-item.component.html',
  styleUrls: ['./train-grid-item.component.scss']
})
export class TrainGridItemComponent implements OnInit {

  @Input()
  public sentence: string;

  constructor() { }

  ngOnInit(): void {
  }

}
