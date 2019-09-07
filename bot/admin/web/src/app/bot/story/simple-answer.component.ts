import {Component, ElementRef, Input, OnInit, ViewChild} from "@angular/core";
import {AnswerContainer, Media, SimpleAnswer, SimpleAnswerConfiguration} from "../model/story";
import {BotService} from "../bot-service";
import {MatDialog} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {CreateI18nLabelRequest} from "../model/i18n";
import {MediaDialogComponent} from "./media/media-dialog.component";
import {AnswerController} from "./controller";
import {DialogService} from "../../core-nlp/dialog.service";

@Component({
  selector: 'tock-simple-answer',
  templateUrl: './simple-answer.component.html',
  styleUrls: ['./simple-answer.component.css']
})
export class SimpleAnswerComponent implements OnInit {

  @Input()
  container: AnswerContainer;

  @Input()
  answerLabel: string = "Answer";

  @Input()
  submit: AnswerController = new AnswerController();

  answer: SimpleAnswerConfiguration;

  fullDisplay: boolean;
  newAnswer: string;
  newMedia: Media;

  @ViewChild('newAnswerElement', {static: false}) newAnswerElement: ElementRef;

  constructor(private state: StateService,
              private bot: BotService,
              private dialog: DialogService,
              private matDialog: MatDialog) {
  }

  ngOnInit(): void {
    this.answer = this.container.simpleAnswer();
    this.answer.allowNoAnswer = this.container.allowNoAnwser();
    setTimeout(_ => this.newAnswerElement.nativeElement.focus(), 500);
    const _this = this;
    this.submit.answerSubmitListener = callback => _this.addAnswerIfNonEmpty(callback);
  }

  toggleDisplay() {
    this.fullDisplay = !this.fullDisplay;
  }

  updateLabel(answer: SimpleAnswer) {
    this.bot
      .saveI18nLabel(answer.label)
      .subscribe(_ => this.dialog.notify(`Label updated`, "Update"));
  }

  private addAnswerIfNonEmpty(callback) {
    if (this && this.newAnswer && this.newAnswer.trim().length !== 0) {
      this.addAnswer(callback);
    } else {
      callback();
    }
  }

  addAnswer(callback?) {
    if (!this.newAnswer || this.newAnswer.trim().length === 0) {
      this.newAnswer = "";
      if (!this.container.allowNoAnwser() && this.answer.answers.length === 0) {
        this.dialog.notify("Please specify an answer")
      } else {
        this.submit.submitAnswer();
      }
    } else {
      this.bot.createI18nLabel(
        new CreateI18nLabelRequest(
          this.container.category,
          this.newAnswer.trim(),
          this.state.currentLocale,
        )
      ).subscribe(i18n => {
        this.answer.answers.push(
          new SimpleAnswer(
            i18n,
            -1,
            this.newMedia
          ));
        this.newAnswer = "";
        this.newMedia = null;
        if (callback) {
          callback();
        }
      });
    }
  }

  deleteAnswer(answer: SimpleAnswer, notify: boolean = false) {
    this.answer.answers.splice(this.answer.answers.indexOf(answer), 1);
    this.bot.deleteI18nLabel(answer.label).subscribe(c => {
      if (notify) this.dialog.notify(`Label deleted`, "DELETE")
    });
  }

  displayMediaMessage(answer?: SimpleAnswer) {
    const media = answer ? answer.mediaMessage : this.newMedia;
    let dialogRef = this.dialog.open(
      this.matDialog,
      MediaDialogComponent,
      {
        data:
          {
            media: media,
            category: this.container.category
          }
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      const removeMedia = result.removeMedia;
      const media = result.media;
      if (removeMedia || media) {
        if (removeMedia) {
          if (answer) answer.mediaMessage = null; else this.newMedia = null;
        } else {
          if (answer) answer.mediaMessage = media; else this.newMedia = media;
        }
      }
    });
  }

}
