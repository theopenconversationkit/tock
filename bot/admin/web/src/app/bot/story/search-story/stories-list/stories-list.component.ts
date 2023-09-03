import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { StateService } from '../../../../core-nlp/state.service';
import { StoryDefinitionConfigurationSummary } from '../../../model/story';

@Component({
  selector: 'tock-stories-list',
  templateUrl: './stories-list.component.html',
  styleUrls: ['./stories-list.component.scss']
})
export class StoriesListComponent implements OnInit {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() stories: StoryDefinitionConfigurationSummary[];

  @Output() onEditStory = new EventEmitter<StoryDefinitionConfigurationSummary>();
  @Output() onDownloadStory = new EventEmitter<StoryDefinitionConfigurationSummary>();
  @Output() onDeleteStory = new EventEmitter<StoryDefinitionConfigurationSummary>();

  dateFormat = 'dd/MM/yyyy HH:mm';

  constructor(public state: StateService) {}

  ngOnInit(): void {}

  editStory(story) {
    this.onEditStory.emit(story);
  }

  downloadStory(story) {
    this.onDownloadStory.emit(story);
  }

  deleteStory(story) {
    this.onDeleteStory.emit(story);
  }
}
