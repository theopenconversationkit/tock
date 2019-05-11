import {Component, Inject, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {AnswerConfigurationType, IntentName, SimpleAnswerConfiguration, StoryStep} from "../model/story";
import {Intent, IntentsCategory} from "../../model/nlp";
import {IntentDialogComponent} from "../../sentence-analysis/intent-dialog/intent-dialog.component";

@Component({
  selector: 'tock-step-dialog',
  templateUrl: './step-dialog.component.html',
  styleUrls: ['./step-dialog.component.css']
})
export class StepDialogComponent implements OnInit {

  newStep: StoryStep;
  steps: StoryStep[];

  intentCategories: IntentsCategory[] = [];
  currentIntentCategories: IntentsCategory[] = [];

  currentEditedIntent: string;
  defaultCategory: string;

  constructor(
    public dialogRef: MatDialogRef<StepDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public state: StateService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog) {

    this.setNewSet();
    this.defaultCategory = this.data.category;
    this.steps = this.data.steps ? this.data.steps.slice(0).map(a => {
      let newA = a.clone();
      newA.intentDefinition = this.state.findIntentByName(a.intent.name);
      return newA;
    }) : [];
  }

  private setNewSet() {
    this.newStep = new StoryStep(
      "",
      new IntentName(""),
      [new SimpleAnswerConfiguration([])],
      AnswerConfigurationType.simple,
      this.data.category
    );
  }

  ngOnInit() {
    this.state.currentIntentsCategories.subscribe(c => {
      this.intentCategories = c;
      this.currentIntentCategories = c;
    });
  }

  onIntentChange(step: StoryStep, name: string) {
    if (this.currentEditedIntent !== name) {
      this.currentEditedIntent = name;
      const intent = name.trim().toLowerCase();
      let target = this.intentCategories.map(
        c => new IntentsCategory(c.category,
          c.intents.filter(i =>
            i.intentLabel().toLowerCase().startsWith(intent)
          ))
      )
        .filter(c => c.intents.length !== 0);

      this.currentIntentCategories = target;
    }
  }

  validateIntent(step: StoryStep) {
    const intentName = step.intent.name.trim();
    if (intentName.length !== 0 && (!step.intentDefinition || step.intentDefinition.name !== intentName)) {
      let intent = this.state.findIntentByName(intentName);
      if (intent) {
        step.intentDefinition = intent;
      } else {
        let dialogRef = this.dialog.open(
          IntentDialogComponent,
          {
            data: {
              create: true,
              category: this.defaultCategory,
              name: intentName,
              label: intentName
            }
          });
        dialogRef.afterClosed().subscribe(result => {
          if (result.name) {
            step.intentDefinition =
              new Intent(
                result.name,
                this.state.currentApplication.namespace,
                [],
                [this.state.currentApplication._id],
                [],
                [],
                result.label,
                result.description,
                result.category
              )
          } else {
            step.intent.name = step.intentDefinition ? step.intentDefinition.name : "";
          }
        })
      }
    }
  }

  removeStep(s: StoryStep) {
    this.steps.splice(this.steps.indexOf(s), 1);
  }

  addStep() {
    let invalidMessage = this.newStep.currentAnswer().invalidMessage();
    if (invalidMessage) {
      this.snackBar.open(`Error: ${invalidMessage}`, "ERROR", {duration: 5000});
    } else {
      this.steps.push(this.newStep);
      this.setNewSet();
    }
  }

  cancel() {
    this.dialogRef.close({});
  }

  save() {
    this.dialogRef.close({
      steps: this.steps
    });
  }

}
