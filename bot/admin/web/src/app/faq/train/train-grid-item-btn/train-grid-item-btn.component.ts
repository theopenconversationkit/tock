import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import {StoryStep} from "../../../bot/model/story";

@Component({
  selector: 'tock-train-grid-item-btn',
  templateUrl: './train-grid-item-btn.component.html',
  styleUrls: ['./train-grid-item-btn.component.css']
})
export class TrainGridItemBtnComponent implements OnInit {

  @Output()
  click = new EventEmitter<void>();

  constructor() { }

  ngOnInit(): void {
  }

}
