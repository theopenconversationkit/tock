import {Component, Input, OnInit} from "@angular/core";
import {AnswerConfigurationType, IntentName, SimpleAnswerConfiguration, StoryStep} from "../model/story";
import {StateService} from "../../core-nlp/state.service";

@Component({
  selector: 'tock-steps',
  templateUrl: './steps.component.html',
  styleUrls: ['./steps.component.css']
})
export class StepsComponent implements OnInit {

  @Input()
  steps: StoryStep[] = [];

  @Input()
  defaultCategory: string = "build";

  constructor(
    public state: StateService) {
  }

  ngOnInit() {
  }

  addNewStep() {

    const newStep = new StoryStep(
      "",
      new IntentName(""),
      [new SimpleAnswerConfiguration([])],
      AnswerConfigurationType.simple,
      this.defaultCategory,
      ""
    );
    newStep.new = true;
    this.steps.push(newStep);
  }

  deleteStep(step: StoryStep) {
    this.steps.splice(this.steps.indexOf(step), 1);
  }

}
