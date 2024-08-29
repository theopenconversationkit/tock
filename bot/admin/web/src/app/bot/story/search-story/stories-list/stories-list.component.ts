import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { StateService } from '../../../../core-nlp/state.service';
import { IntentName, StoryDefinitionConfigurationSummary } from '../../../model/story';
import { NbDialogService } from '@nebular/theme';
import { Intent } from '../../../../model/nlp';
import { IntentStoryDetailsComponent } from '../../../../shared/components';

@Component({
  selector: 'tock-stories-list',
  templateUrl: './stories-list.component.html',
  styleUrls: ['./stories-list.component.scss']
})
export class StoriesListComponent {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() stories: StoryDefinitionConfigurationSummary[];

  @Output() onEditStory = new EventEmitter<StoryDefinitionConfigurationSummary>();
  @Output() onDownloadStory = new EventEmitter<StoryDefinitionConfigurationSummary>();
  @Output() onDeleteStory = new EventEmitter<StoryDefinitionConfigurationSummary>();

  dateFormat = 'dd/MM/yyyy HH:mm';

  constructor(public state: StateService, private nbDialogService: NbDialogService) {}

  editStory(story) {
    this.onEditStory.emit(story);
  }

  downloadStory(story) {
    this.onDownloadStory.emit(story);
  }

  deleteStory(story) {
    this.onDeleteStory.emit(story);
  }

  displayIntentStoryDetails(intent: IntentName) {
    this.nbDialogService.open(IntentStoryDetailsComponent, {
      context: {
        intentName: intent.name
      }
    });
  }
}
