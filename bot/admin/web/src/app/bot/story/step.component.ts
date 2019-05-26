import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {StoryStep} from "../model/story";
import {MatDialog, MatSnackBar} from "@angular/material";
import {Intent, IntentsCategory} from "../../model/nlp";
import {StateService} from "../../core-nlp/state.service";
import {IntentDialogComponent} from "../../sentence-analysis/intent-dialog/intent-dialog.component";

@Component({
  selector: 'tock-step',
  templateUrl: './step.component.html',
  styleUrls: ['./step.component.css']
})
export class StepComponent implements OnInit {

  @Input()
  step: StoryStep;

  @Input()
  defaultCategory: string = "build";

  @Output()
  delete = new EventEmitter<StoryStep>();

  @Output()
  child = new EventEmitter<StoryStep>();

  @Input()
  readonly: boolean = false;

  intentCategories: IntentsCategory[] = [];
  currentIntentCategories: IntentsCategory[] = [];

  currentEditedIntent: string;

  constructor(
    public state: StateService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog) {
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
    setTimeout(_ => {
      const intentName = step.intent.name.trim();
      if (intentName.length !== 0 && (!step.intentDefinition || step.intentDefinition.name !== intentName)) {
        let intent = this.state.findIntentByName(intentName);
        if (intent) {
          step.intentDefinition = intent;
          if (!step.name || step.name.trim().length === 0) {
            step.name = intentName + "_" + step.level;
          }
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
                );
              step.intent.name = result.name;
              step.name = result.name + "_" + step.level;
            } else {
              step.intent.name = step.intentDefinition ? step.intentDefinition.name : "";
            }
          })
        }
      }
    }, 200);
  }

  removeStep() {
    this.delete.emit(this.step);
  }

  save() {
    let invalidMessage = this.step.currentAnswer().invalidMessage();
    if (this.step.intent.name.trim().length === 0) {
      invalidMessage = "Please choose an intent";
    }
    if (invalidMessage) {
      this.snackBar.open(`Error: ${invalidMessage}`, "ERROR", {duration: 5000});
    } else {
      this.step.new = false;
    }
  }

  addChild() {
    this.child.emit(this.step);
  }


}
