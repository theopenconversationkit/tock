import {Component, Input, OnInit} from "@angular/core";
import {AnswerContainer, ScriptAnswerConfiguration} from "../model/story";
import {BotService} from "../bot-service";
import {MatDialog, MatSnackBar} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";

@Component({
  selector: 'tock-script-answer',
  templateUrl: './script-answer.component.html',
  styleUrls: ['./script-answer.component.css']
})
export class ScriptAnswerComponent implements OnInit {

  @Input()
  container: AnswerContainer;

  answer: ScriptAnswerConfiguration;

  constructor(private state: StateService,
              private bot: BotService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.answer = this.container.scriptAnswer();
  }

}
