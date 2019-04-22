import {Component, ElementRef, Inject, OnInit, ViewChild} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef, MatSnackBar} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {AnswerConfigurationType, IntentName, MandatoryEntity, SimpleAnswerConfiguration} from "../model/story";

@Component({
  selector: 'tock-mandatory-entities-dialog',
  templateUrl: './mandatory-entities-dialog.component.html',
  styleUrls: ['./mandatory-entities-dialog.component.css']
})
export class MandatoryEntitiesDialogComponent implements OnInit {

  entities: MandatoryEntity[];
  newEntity: MandatoryEntity;
  entityRoles: string[] = [];

  originalEntities: MandatoryEntity[];

  @ViewChild('newRole') newRoleElement: ElementRef;

  constructor(
    public dialogRef: MatDialogRef<MandatoryEntitiesDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public state: StateService,
    private snackBar: MatSnackBar) {
    this.setNewEntity();
    this.entities = this.data.entities ? this.data.entities.slice(0).map(a => a.clone()) : [];
    this.newEntity.category = this.data.category;

    setTimeout(() => this.newRoleElement.nativeElement.focus(), 500);
  }

  ngOnInit() {
    this.setDefaultRoles();
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

  private setDefaultRoles() {
    this.state.entities.subscribe(e => {
      this.entityRoles = e.map(e => e.role).sort((a, b) => a.localeCompare(b));
    });
  }

  onIntentChange(entity: MandatoryEntity, name: string) {
    entity.intent.name = name;
    const i = this.state.findIntentByName(name);
    let roles = [];
    if (i) {
      roles = i.entities.map(e => e.role).sort((a, b) => a.localeCompare(b));
    }
    if (roles.length === 0) {
      this.setDefaultRoles();
    } else {
      this.entityRoles = roles;
    }
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
