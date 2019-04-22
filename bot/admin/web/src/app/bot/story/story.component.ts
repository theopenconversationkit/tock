import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from "@angular/core";
import {StoryDefinitionConfiguration} from "../model/story";
import {BotService} from "../bot-service";
import {MatDialog, MatSnackBar} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {ConfirmDialogComponent} from "../../shared-nlp/confirm-dialog/confirm-dialog.component";
import {StoryDialogComponent} from "./story-dialog.component";
import {MandatoryEntitiesDialogComponent} from "./mandatory-entities-dialog.component";
import {StoryNode} from "../flow/node";

@Component({
  selector: 'tock-story',
  templateUrl: './story.component.html',
  styleUrls: ['./story.component.css']
})
export class StoryComponent implements OnInit, OnChanges {

  @Input()
  storyDefinitionId: string = null;

  @Input()
  story: StoryDefinitionConfiguration = null;

  @Input()
  storyNode: StoryNode = null;

  @Input()
  fullDisplay: boolean = false;

  @Input()
  displayCancel: boolean = false;

  @Output()
  delete = new EventEmitter<string>();

  constructor(private state: StateService,
              private bot: BotService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges) {
    if (!this.story) {
      const id = this.storyDefinitionId ? this.storyDefinitionId : this.storyNode.storyDefinitionId;
      this.bot.findStory(id)
        .subscribe(s => {
          this.story = s.storyId ? s : null;
        });
    }
  }

  deleteStory() {
    let dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `Delete the story ${this.story.name}`,
        subtitle: "Are you sure?",
        action: "Remove"
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.bot.deleteStory(this.story._id)
          .subscribe(_ => {
            this.delete.emit(this.story._id);
            this.snackBar.open(`Story deleted`, "Delete", {duration: 2000})
          });
      }
    });
  }

  editStory() {
    let dialogRef = this.dialog.open(
      StoryDialogComponent,
      {
        data:
          {
            create: !this.story._id,
            name: this.story.storyId,
            label: this.story.name,
            intent: this.story.intent.name,
            description: this.story.description,
            category: this.story.category,
            story: true
          }
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result.name) {
        this.story.storyId = result.name;
        this.story.name = result.label;
        this.story.intent.name = result.intent;
        this.story.category = result.category;
        this.story.description = result.description;
        this.saveStory();
      }
    });
  }

  private saveStory() {
    if (this.story._id) {
      this.bot.saveStory(this.story).subscribe(s =>
        this.snackBar.open(`Story ${this.story.name} modified`, "Update", {duration: 3000})
      )
    }
  }

  editEntities() {
    let dialogRef = this.dialog.open(
      MandatoryEntitiesDialogComponent,
      {
        data:
          {
            entities: this.story.mandatoryEntities,
            category: this.story.category
          }
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result.entities) {
        this.story.mandatoryEntities = result.entities;
        this.saveStory();
      }
    });
  }

  createStory() {

  }
}
