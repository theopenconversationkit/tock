import {Component, ElementRef, Input, OnInit, ViewChild} from "@angular/core";
import {AnswerContainer, SimpleAnswer, SimpleAnswerConfiguration} from "../model/story";
import {BotService} from "../bot-service";
import {MatDialog, MatSnackBar} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {CreateI18nLabelRequest} from "../model/i18n";

@Component({
  selector: 'tock-simple-answer',
  templateUrl: './simple-answer.component.html',
  styleUrls: ['./simple-answer.component.css']
})
export class SimpleAnswerComponent implements OnInit {

  @Input()
  container: AnswerContainer;

  answer: SimpleAnswerConfiguration;

  fullDisplay: boolean;
  newAnswer: string;

  @ViewChild('newAnswerElement') newAnswerElement: ElementRef;

  constructor(private state: StateService,
              private bot: BotService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.answer = this.container.simpleAnswer();
    setTimeout(_ => this.newAnswerElement.nativeElement.focus(), 500);
  }

  toggleDisplay() {
    this.fullDisplay = !this.fullDisplay;
  }

  updateLabel(answer: SimpleAnswer) {
    this.bot
      .saveI18nLabel(answer.label)
      .subscribe(_ => this.snackBar.open(`Label updated`, "Update", {duration: 3000}));
  }

  addAnswerIfNonEmpty() {
    if (this.newAnswer && this.newAnswer.trim().length !== 0 && this.answer.answers.length === 0) {
      this.addAnswer();
    }
  }

  addAnswer() {
    if (!this.newAnswer || this.newAnswer.trim().length === 0) {
      this.snackBar.open("Please specify an answer", "Error", {duration: 5000})
    } else {
      this.bot.createI18nLabel(
        new CreateI18nLabelRequest(
          this.container.category,
          this.newAnswer,
          this.state.currentLocale,
        )
      ).subscribe(i18n => {
        this.answer.answers.push(
          new SimpleAnswer(
            i18n,
            -1
          ));
        this.newAnswer = "";
      });
    }
  }

  deleteAnswer(answer: SimpleAnswer, notify: boolean = false) {
    this.answer.answers.splice(this.answer.answers.indexOf(answer), 1);
    this.bot.deleteI18nLabel(answer.label).subscribe(c => {
      if (notify) this.snackBar.open(`Label deleted`, "DELETE", {duration: 1000})
    });
  }

}
