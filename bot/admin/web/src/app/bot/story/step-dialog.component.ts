import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {StoryStep} from "../model/story";

@Component({
  selector: 'tock-step-dialog',
  templateUrl: './step-dialog.component.html',
  styleUrls: ['./step-dialog.component.css']
})
export class StepDialogComponent {

  steps: StoryStep[];
  defaultCategory: string;

  constructor(
    public dialogRef: MatDialogRef<StepDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public state: StateService) {

    this.defaultCategory = this.data.category;
    this.steps = this.data.steps ? this.data.steps.slice(0).map(a => {
      let newA = a.clone();
      newA.intentDefinition = this.state.findIntentByName(a.intent.name);
      return newA;
    }) : [];
  }

  cancel() {
    this.dialogRef.close({});
  }

  save() {
    this.dialogRef.close({
      steps: StoryStep.filterNew(this.steps)
    });
  }

}
