import {Component, Input, OnInit} from "@angular/core";
import {AnswerConfigurationType, IntentName, SimpleAnswerConfiguration, StoryStep} from "../model/story";
import {StateService} from "../../core-nlp/state.service";
import {FlatTreeControl} from "@angular/cdk/tree";
import {MatTreeFlatDataSource, MatTreeFlattener} from "@angular/material";

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

  @Input()
  readonly: boolean = false;

  treeControl: FlatTreeControl<StoryStep>;
  treeFlattener: MatTreeFlattener<StoryStep, StoryStep>;
  dataSource: MatTreeFlatDataSource<StoryStep, StoryStep>;

  constructor(
    public state: StateService) {
  }

  ngOnInit() {
    this.treeControl = new FlatTreeControl<StoryStep>(s => s.level, s => s.children.length !== 0);
    this.treeFlattener = new MatTreeFlattener(s => s, s => s.level, s => s.children.length !== 0, s => s.children);

    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
    this.dataSource.data = this.steps;
    this.treeControl.expandAll();
  }

  isExpandable(s: StoryStep): boolean {
    return s.children.length !== 0;
  }

  addNewStep(step?: StoryStep) {
    const newStep = new StoryStep(
      "",
      new IntentName(""),
      [new SimpleAnswerConfiguration([])],
      AnswerConfigurationType.simple,
      this.defaultCategory,
      "",
      [],
      step ? step.level + 1 : 0
    );
    newStep.new = true;
    if(step) {
      step.children.push(newStep);
      this.treeControl.expand(step);
    } else {
      this.steps.push(newStep);
    }
    this.validate();
  }

  private findParent(steps:StoryStep[], step:StoryStep) : StoryStep {
    if(step.level === 0 || steps.length === 0) {
      return null;
    }
    const c = steps.find(p => p.children.indexOf(step) !== -1);
    if(c) {
      return c;
    }
    const parents = steps.map(s => this.findParent(s.children, step)).filter(s => s !== null);
    return parents.length === 0 ? null : parents[0];
  }

  deleteStep(step: StoryStep) {
    const parent = this.findParent(this.steps, step);
    if(parent) {
      parent.children.splice(parent.children.indexOf(step), 1);
    } else {
      this.steps.splice(this.steps.indexOf(step), 1);
    }
    this.validate();
  }

  validate() {
    this.dataSource.data = this.steps.slice(0);
  }

}
