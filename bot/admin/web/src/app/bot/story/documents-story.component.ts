import {Component, OnDestroy, OnInit} from '@angular/core';
import {BotService} from '../bot-service';
import {NlpService} from '../../nlp-tabs/nlp.service';
import {StateService} from '../../core-nlp/state.service';
import {
  MediaFile, SimpleAnswer,
  StoryDefinitionConfiguration,
} from '../model/story';
import {Subscription} from 'rxjs';
import {RestService} from 'src/app/core-nlp/rest/rest.service';
import {MediaDialogComponent} from "./media/media-dialog.component";
import {DialogService} from 'src/app/core-nlp/dialog.service';

@Component({
  selector: 'tock-documents-story',
  templateUrl: './documents-story.component.html',
  styleUrls: ['./documents-story.component.css']
})
export class DocumentsStoryComponent implements OnInit, OnDestroy {
  private stories: StoryDefinitionConfiguration [];
  fileList: MediaFile [];
  selectedStory: StoryDefinitionConfiguration;
  storyNameColumn = 'Story';
  categoryColumn = 'Category';
  fileNameColumn = 'File Name';

  fileTitleColumn = 'File Title'
  actionsColumn = 'Actions';
  allColumns = [this.storyNameColumn, this.categoryColumn, this.fileNameColumn, this.fileTitleColumn, this.actionsColumn];
  category: string = '';
  loading: boolean = false;
  private subscription: Subscription;
  constructor(
    private nlp: NlpService,
    public state: StateService,
    private bot: BotService,
    public rest: RestService,
    private dialog: DialogService
  ) {
  }

  ngOnInit(): void {
    this.getStoriesWithFileFromServer();
    this.subscription = this.state.configurationChange.subscribe(_ => this.getStoriesWithFileFromServer());
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  saveStory() {
    this.bot.saveStory(this.selectedStory).subscribe(res => {
      this.getStoriesWithFileFromServer();
    })
  }

  getStoriesWithFileFromServer() {
    this.bot.findStoryDefinitionsByNamespaceAndBotIdWithFileAttached(this.state.currentApplication.name).subscribe((res: any []) => {
      this.stories = res;
      this.fileList = res.flatMap(obj => obj.answers)
        .flatMap(answer => answer.answers)
        .filter(answer => answer?.mediaMessage?.file)
        .map(answer => answer.mediaMessage.file);
    });
  }

  getStoryWithThisMediaFile(file: MediaFile): StoryDefinitionConfiguration {
    return this.stories.find(story =>
      story.answers?.some(answer =>
        //@ts-ignore
        answer.answers?.some(innerAnswer =>
          innerAnswer.mediaMessage?.file?.id === file.id)));
  }

  getAnswerWithThisMediaFile(file: MediaFile): SimpleAnswer {
    const matchingStory = this.getStoryWithThisMediaFile(file);
    if (matchingStory) {
      return matchingStory.answers
        //@ts-ignore
        .flatMap(answer => answer.answers)
        .find(innerAnswer => innerAnswer.mediaMessage?.file?.id === file.id);
    }
    return undefined;
  }

  displayMediaMessage(file: MediaFile) {
    this.selectedStory = this.getStoryWithThisMediaFile(file);
    let answer = this.getAnswerWithThisMediaFile(file);
    let dialogRef = this.dialog.openDialog(MediaDialogComponent, {
      context: {
        // @ts-ignore
        media: answer.mediaMessage,
        category: this.selectedStory.category
      }
    });

    dialogRef.onClose.subscribe(result => {
      if (result && (result.removeMedia || result.media)) {
        answer.mediaMessage = result.removeMedia ? null : result.media
        this.saveStory();
      }
    });
  }

}
