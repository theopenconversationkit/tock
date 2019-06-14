import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {IntentName, StoryStep} from "../model/story";
import {MatDialog, MatSnackBar} from "@angular/material";
import {Intent, IntentsCategory, ParseQuery} from "../../model/nlp";
import {StateService} from "../../core-nlp/state.service";
import {IntentDialogComponent} from "../../sentence-analysis/intent-dialog/intent-dialog.component";
import {NlpService} from "../../nlp-tabs/nlp.service";

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

  displayTargetIntent: boolean = false;

  constructor(
    public state: StateService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private nlp: NlpService) {
  }

  ngOnInit() {
    this.state.currentIntentsCategories.subscribe(c => {
      this.intentCategories = c;
      this.currentIntentCategories = c;
    });
    this.displayTargetIntent = this.step.targetIntent.name.trim().length !== 0;
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

  validateIntent(step: StoryStep, targetIntent?: boolean) {
    setTimeout(_ => {
      const intentName = (targetIntent ? step.targetIntent : step.intent).name.trim();
      const intentDef = (targetIntent ? step.targetIntentDefinition : step.intentDefinition);
      if (intentName.length !== 0 && (!intentDef || intentDef.name !== intentName)) {
        let intent = this.state.findIntentByName(intentName);
        if (intent) {
          if (targetIntent) {
            step.targetIntentDefinition = intent;
          } else {
            step.intentDefinition = intent;
          }
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
              const newIntent =
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
              if (targetIntent) {
                step.targetIntentDefinition = newIntent;
                step.targetIntent.name = result.name;
              } else {
                step.intentDefinition = newIntent;
                step.intent.name = result.name;
                step.name = result.name + "_" + step.level;
              }
            } else {
              if (targetIntent) {
                step.targetIntent.name = step.targetIntentDefinition ? step.targetIntentDefinition.name : "";
              } else {
                step.intent.name = step.intentDefinition ? step.intentDefinition.name : "";
              }
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
    if ((!this.step.currentAnswer().isEmpty() || this.step.targetIntent.name.trim().length !== 0)
      && this.step.intent.name.trim().length === 0) {
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

  focusTargetIntent(element) {
    this.displayTargetIntent = true;
    setTimeout(_ => element.focus(), 200);
  }

  userSentenceChange(userSentence: string) {
    if (userSentence.trim().length !== 0 && this.step.intent.name.length === 0) {
      const app = this.state.currentApplication;
      const language = this.state.currentLocale;
      this.nlp.parse(new ParseQuery(
        app.namespace,
        app.name,
        language,
        userSentence,
        true
      )).subscribe(r => {
        if (r.classification.intentId) {
          const intent = this.state.findIntentById(r.classification.intentId);
          if (intent) {
            this.step.intentDefinition = intent;
            this.step.intent = new IntentName(intent.name);
            this.onIntentChange(this.step, intent.name);
          }
        }
      })
    }
  }


}
