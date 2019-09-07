import {Component, Input, OnInit} from "@angular/core";
import {AnswerContainer, ScriptAnswerConfiguration} from "../model/story";

@Component({
  selector: 'tock-script-answer',
  templateUrl: './script-answer.component.html',
  styleUrls: ['./script-answer.component.css']
})
export class ScriptAnswerComponent implements OnInit {

  @Input()
  container: AnswerContainer;

  answer: ScriptAnswerConfiguration;

  constructor() {
  }

  ngOnInit(): void {
    this.answer = this.container.scriptAnswer();
  }

}
