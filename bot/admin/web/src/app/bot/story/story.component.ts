/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from "@angular/core";
import {
  AnswerConfigurationType,
  CreateStoryRequest,
  IntentName,
  StoryDefinitionConfiguration,
  StoryStep
} from "../model/story";
import {BotService} from "../bot-service";
import { MatDialog } from "@angular/material/dialog";
import {StateService} from "../../core-nlp/state.service";
import {ConfirmDialogComponent} from "../../shared-nlp/confirm-dialog/confirm-dialog.component";
import {StoryDialogComponent} from "./story-dialog.component";
import {MandatoryEntitiesDialogComponent} from "./mandatory-entities-dialog.component";
import {StoryNode} from "../flow/node";
import {StepDialogComponent} from "./step-dialog.component";
import {AnswerController} from "./controller";
import {DialogService} from "../../core-nlp/dialog.service";

@Component({
  selector: 'tock-story',
  templateUrl: './story.component.html',
  styleUrls: ['./story.component.css']
})
export class StoryComponent implements OnInit, OnChanges {

  @Input()
  story: StoryDefinitionConfiguration = null;

  @Input()
  storyNode: StoryNode = null;

  @Input()
  storyTag: string = "";

  @Input()
  fullDisplay: boolean = false;

  @Input()
  displaySteps: boolean = false;

  @Input()
  botId: string = null;

  @Input()
  displayCancel: boolean = false;

  @Output()
  delete = new EventEmitter<string>();

  @Input()
  submit = new AnswerController();

  @Input()
  displayCount: boolean = true;

  @Output()
  close = new EventEmitter<boolean>();
  isSwitchingToManagedStory = false;

  constructor(private state: StateService,
              private bot: BotService,
              private dialog: DialogService,
              private matDialog: MatDialog) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.storyNode) {
      const c = (changes.storyNode as SimpleChange).currentValue;
      if (!c) {
        this.story = null;
        this.storyTag = "";
      } else if (c.dynamic) {
        this.bot.findStory(this.storyNode.storyDefinitionId)
          .subscribe(s => {
            //explicit null value if no story found
            this.story = s.storyId ? s : null;
            this.storyTag = s.getFirstTag();
          });
      } else {
        this.initStoryByBotIdAndIntent();
      }
    }
  }

  private initStoryByBotIdAndIntent() {
    this.bot.findStoryByBotIdAndIntent(this.botId, this.storyNode.storyDefinitionId)
      .subscribe(s => {
        //explicit null value if no story found
        this.story = s.storyId ? s : null;
        this.storyTag = s.getFirstTag();
      });
  }

  deleteStory() {
    let dialogRef = this.dialog.open(
      this.matDialog,
      ConfirmDialogComponent,
      {
        data: {
          title: `Remove the story ${this.story.name}`,
          subtitle: "Are you sure?",
          action: "Remove"
        }
      });
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.bot.deleteStory(this.story._id)
          .subscribe(_ => {
            this.delete.emit(this.story._id);
            this.story = null;
            this.storyTag = "";
            this.dialog.notify(`Story deleted`, "Delete")
          });
      }
    });
  }

  editStory() {
    let dialogRef = this.dialog.open(
      this.matDialog,
      StoryDialogComponent,
      {
        data:
          {
            create: !this.story._id,
            name: this.story.storyId,
            label: this.story.name,
            tag: this.story.tags && this.story.tags.length > 0 ? this.story.tags[0] : undefined,
            intent: this.story.intent.name,
            description: this.story.description,
            category: this.story.category,
            freezeIntent: this.storyNode,
            userSentence: this.story.userSentence,
            story: this.story
          }
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result && result.name) {
        this.story.storyId = result.name;
        this.story.name = result.label;
        this.story.tags = [result.tag];
        this.story.intent.name = result.intent;
        this.story.category = result.category;
        this.story.description = result.description;
        this.story.userSentence = result.userSentence;
        this.saveStory(this.story.selected);
        this.submitClose();
      }
    });
  }

  private saveStory(selectStoryAfterSave: boolean) {
    this.story.steps = StoryStep.filterNew(this.story.steps);
    this.story.tags = [this.storyTag];
    if (this.story._id) {
      this.bot.saveStory(this.story).subscribe(s => {
        this.story.selected = selectStoryAfterSave;
        this.state.resetConfiguration();
        this.dialog.notify(`Story ${this.story.name} modified`, "Update");
      })
    }
  }

  editEntities() {
    let dialogRef = this.dialog.open(
      this.matDialog,
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
      if (result && result.entities) {
        this.story.mandatoryEntities = result.entities;
        //console.log(this.story);
        this.saveStory(this.story.selected);
      }
    });
  }

  editSteps() {
    let dialogRef = this.dialog.open(
      this.matDialog,
      StepDialogComponent,
      {
        data:
          {
            steps: StoryStep.filterNew(this.story.steps),
            category: this.story.category
          },
        minWidth: 900
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result && result.steps) {
        this.story.steps = result.steps;
        this.saveStory(this.story.selected);
        this.submitClose();
      }
    });
  }

  createStory() {
    this.isSwitchingToManagedStory = false;
    const intent = this.state.findIntentByName(this.storyNode.storyDefinitionId);
    this.story = new StoryDefinitionConfiguration(
      intent.name,
      this.botId,
      new IntentName(intent.name),
      AnswerConfigurationType.simple,
      this.state.user.organization,
      [],
      "build",
      intent.name,
      "",
      this.state.currentLocale,
      []
    );
  }

  saveNewStory() {
    this.isSwitchingToManagedStory = false;
    let invalidMessage = this.story.currentAnswer().invalidMessage();
    if (invalidMessage) {
      this.dialog.notify(`Error: ${invalidMessage}`);
    } else {
      this.bot.newStory(
        new CreateStoryRequest(
          this.story,
          this.state.currentLocale,
          []
        )
      ).subscribe(intent => {
        this.dialog.notify(`New story ${this.story.name} created for language ${this.state.currentLocale}`, "New Story");
        this.initStoryByBotIdAndIntent();
      });
    }
  }

  submitClose() {
    this.close.emit(true);
  }

  manageStory() {
    this.story.currentType = AnswerConfigurationType.simple;
    this.isSwitchingToManagedStory = true;
  }

  cancelManagingStory() {
    if (this.isSwitchingToManagedStory) {
      this.isSwitchingToManagedStory = false;
      this.story.currentType = AnswerConfigurationType.builtin;
    }
  }

}
