/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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
