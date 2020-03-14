import {Component, Inject, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {EntityType, IntentsCategory} from "../../model/nlp";
import {StateService} from "../../core-nlp/state.service";
import {IntentName} from "../model/story";
import {NlpService} from "../../nlp-tabs/nlp.service";

@Component({
  selector: 'select-entity-dialog',
  templateUrl: './select-entity-dialog.component.html',
  styleUrls: ['./select-entity-dialog.component.css']
})
export class SelectEntityDialogComponent implements OnInit {

  generate: boolean;
  entities: EntityType[] = [];
  selectedEntity: EntityType;
  role: string;
  intent: IntentName = new IntentName("");
  intentCategories: IntentsCategory[] = [];
  currentIntentCategories: IntentsCategory[] = [];
  currentEditedIntent: string;
  entityValue: string;
  entityValues: string[] = [];

  alreadySelectedEntity: string;

  constructor(
    public dialogRef: MatDialogRef<SelectEntityDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private state: StateService,
    private nlp: NlpService
  ) {
    this.generate = data.generate;
    this.alreadySelectedEntity = data.selectedEntity;
    this.role = data.role;
    this.entityValue = data.entityValue;
  }

  ngOnInit() {
    this.state.entityTypesSortedByName().subscribe(e => {
      this.entities = this.generate ? e.filter(entity => entity.dictionary) : e;
      if (this.alreadySelectedEntity) {
        this.selectedEntity = e.find(en => en.name == this.alreadySelectedEntity);
      }
      this.calculateEntityValues(this.selectedEntity);
    });
    this.state.currentIntentsCategories.subscribe(c => {
      this.intentCategories = c;
      this.currentIntentCategories = c;
    });

  }

  onIntentChange(name: string) {
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

  selectEntityType(entityType: EntityType) {
    this.selectedEntity = entityType;
    this.entityValue = null;
    this.role = entityType.simpleName();
    this.calculateEntityValues(entityType);
  }

  selectEntityValue(value: string) {
    if (this.entityValue === value) {
      this.entityValue = null
    } else {
      this.entityValue = value;
    }
  }

  private calculateEntityValues(entityType: EntityType) {
    if (entityType && !this.generate) {
      this.nlp.getDictionary(entityType).subscribe(dictionary => {
        this.entityValues = dictionary.values.map(v => v.value);
      });
    }
  }

  cancel() {
    this.dialogRef.close({});
  }

  validateEntity() {
    this.dialogRef.close({
      intent: this.intent,
      entity: this.selectedEntity,
      role: this.role
    });
  }

  validateEntityValue() {
    this.dialogRef.close({
      entity: this.selectedEntity,
      role: this.role,
      value: this.entityValue
    });
  }

  removeEntityValue() {
    this.dialogRef.close({
      entity: this.selectedEntity
    });
  }
}
