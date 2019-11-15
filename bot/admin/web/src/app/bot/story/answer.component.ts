import {Component, Input, OnInit} from "@angular/core";
import {
  AnswerConfigurationType,
  AnswerContainer,
  ScriptAnswerConfiguration,
  ScriptAnswerVersionedConfiguration,
  SimpleAnswerConfiguration
} from "../model/story";
import {BotService} from "../bot-service";
import {MatDialog, MatRadioChange} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {AnswerDialogComponent} from "./answer-dialog.component";
import {AnswerController} from "./controller";
import {BotSharedService} from "../../shared/bot-shared.service";
import {DialogService} from "../../core-nlp/dialog.service";

@Component({
  selector: 'tock-answer',
  templateUrl: './answer.component.html',
  styleUrls: ['./answer.component.css']
})
export class AnswerComponent implements OnInit {

  @Input()
  answer: AnswerContainer;

  @Input()
  fullDisplay: boolean = false;

  @Input()
  editable: boolean = true;

  @Input()
  create: boolean = false;

  @Input()
  answerLabel: string = "Answer";

  @Input()
  submit: AnswerController = new AnswerController();

  @Input()
  wide: boolean = false;

  constructor(private state: StateService,
              private bot: BotService,
              private dialog: DialogService,
              private matDialog: MatDialog,
              public shared: BotSharedService) {
  }

  ngOnInit(): void {
    if (!this.answer.currentAnswer()) {
      this.changeAnswerType(this.answer.currentType);
    }
  }

  editAnswer() {
    this.dialog.open(
      this.matDialog,
      AnswerDialogComponent,
      {
        data:
          {
            answer: this.answer,
            create: this.create,
            answerLabel: this.answerLabel
          }
      }
    );
  }

  changeType(event: MatRadioChange) {
    this.changeAnswerType(event.value);
  }

  private changeAnswerType(value: AnswerConfigurationType) {
    if (value === AnswerConfigurationType.simple) {
      if (!this.answer.simpleAnswer()) {
        const newAnswer = new SimpleAnswerConfiguration([]);
        newAnswer.allowNoAnswer = this.answer.allowNoAnwser();
        this.answer.answers.push(newAnswer);
      }
    } else if (value === AnswerConfigurationType.script) {
      if (!this.answer.scriptAnswer()) {
        const s = "import ai.tock.bot.definition.story\n" +
          "\n" +
          "val s = story(\"" + this.answer.containerId() + "\") { \n" +
          "           end(\"Hello World! :)\")\n" +
          "}";
        const script = new ScriptAnswerVersionedConfiguration(s);
        this.answer.answers.push(new ScriptAnswerConfiguration([script], script));
      }
    }
  }

}

