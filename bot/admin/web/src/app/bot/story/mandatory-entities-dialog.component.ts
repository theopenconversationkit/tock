import {Component, ElementRef, Inject, OnInit, ViewChild} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {AnswerConfigurationType, IntentName, MandatoryEntity, SimpleAnswerConfiguration} from "../model/story";
import {EntityDefinition, IntentsCategory} from "../../model/nlp";
import {CreateEntityDialogComponent} from "../../sentence-analysis/create-entity-dialog/create-entity-dialog.component";

@Component({
  selector: 'tock-mandatory-entities-dialog',
  templateUrl: './mandatory-entities-dialog.component.html',
  styleUrls: ['./mandatory-entities-dialog.component.css']
})
export class MandatoryEntitiesDialogComponent implements OnInit {

  entities: MandatoryEntity[];
  newEntity: MandatoryEntity;

  intentCategories: IntentsCategory[] = [];
  currentIntentCategories: IntentsCategory[] = [];

  originalEntities: MandatoryEntity[];

  currentEditedIntent:string;

  constructor(
    public dialogRef: MatDialogRef<MandatoryEntitiesDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public state: StateService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog) {

    this.setNewEntity();
    this.entities = this.data.entities ? this.data.entities.slice(0).map(a => a.clone()) : [];
    this.state.entities.subscribe(allEntities => {
      this.entities.forEach(e => e.entity = allEntities.find(a => a.role === e.role));
    });
    this.newEntity.category = this.data.category;
  }

  ngOnInit() {
    this.state.currentIntentsCategories.subscribe(c => {
      this.intentCategories = c;
      this.currentIntentCategories = c;
    });
  }

  private setNewEntity() {
    const c = this.newEntity ? this.newEntity.category : null;
    this.newEntity = new MandatoryEntity(
      "",
      new IntentName(""),
      [new SimpleAnswerConfiguration([])],
      AnswerConfigurationType.simple,
      c ? c : "");
  }

  onIntentChange(entity: MandatoryEntity, name: string) {
     if(this.currentEditedIntent !== name) {
       this.currentEditedIntent = name;
       const intent = name.trim().toLowerCase();
       let target = this.intentCategories.map(
         c => new IntentsCategory(c.category,
           c.intents.filter(i =>
             i.intentLabel().toLowerCase().startsWith(intent)
             && (!entity.role || i.entities.find(e => e.role === entity.role))
           ))
       )
         .filter(c => c.intents.length !== 0);
       if (target.length === 0) {
         target = this.intentCategories.map(
           c => new IntentsCategory(c.category,
             c.intents.filter(i => i.intentLabel().toLowerCase().startsWith(intent))))
           .filter(c => c.intents.length !== 0);
       }

       this.currentIntentCategories = target;
     }
  }

  selectEntity(e:MandatoryEntity) {
    let dialogRef = this.dialog.open(CreateEntityDialogComponent,
      {
        data: {}
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result && result !== "cancel") {
        let name = result.name;
        let role = result.role;
        e.entity = new EntityDefinition(name, role);
        e.role = role;
      }});
  }

  removeEntity(e: MandatoryEntity) {
    this.entities.splice(this.entities.indexOf(e), 1);
  }

  addEntity() {
    let invalidMessage = this.newEntity.currentAnswer().invalidMessage();
    if (invalidMessage) {
      this.snackBar.open(`Error: ${invalidMessage}`, "ERROR", {duration: 5000});
    } else {
      this.entities.push(this.newEntity);
      this.setNewEntity();
    }
  }

  cancel() {
    this.dialogRef.close({});
  }

  save() {
    this.dialogRef.close({
      entities: this.entities
    });
  }

}
