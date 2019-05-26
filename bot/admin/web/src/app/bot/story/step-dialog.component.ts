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

  private filterNew(steps: StoryStep[]): StoryStep[] {
    steps.forEach(s => s.children = this.filterNew(s.children));
    return steps.filter(s => !s.new);
  }

  save() {
    this.dialogRef.close({
      steps: this.filterNew(this.steps)
    });
  }

}
