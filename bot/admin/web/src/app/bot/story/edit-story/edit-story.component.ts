import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, take } from 'rxjs';
import { BotService } from '../../bot-service';
import { StoryDefinitionConfiguration } from '../../model/story';

@Component({
  selector: 'tock-edit-story',
  templateUrl: './edit-story.component.html',
  styleUrls: ['./edit-story.component.scss']
})
export class EditStoryComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  story: StoryDefinitionConfiguration;

  constructor(private route: ActivatedRoute, private router: Router, private bot: BotService) {}

  ngOnInit(): void {
    this.route.params.pipe(take(1)).subscribe((routeParams) => {
      this.bot
        .findStory(routeParams.storyId)
        .pipe(take(1))
        .subscribe((story) => {
          this.story = story;
          this.story.selected = true;
        });
    });
  }

  closeStory() {
    this.router.navigateByUrl('/build/story-search', { state: { category: this.story.category } });
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
