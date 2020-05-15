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

import {Component, OnInit} from "@angular/core";
import {BotService} from "../bot-service";
import {StoryDefinitionConfiguration} from "../model/story";


@Component({
  selector: 'tock-story-runtime-settings',
  templateUrl: './story-runtime-settings.component.html',
  styleUrls: ['./story-runtime-settings.component.css']
})
export class StoryRuntimeSettingsComponent implements OnInit {
  displayedColumns: string[] = ['storyTag', 'storyName'];
  stories: StoryDefinitionConfiguration[];

  constructor(private botService: BotService) {
  }

  ngOnInit(): void {
    this.botService.findRuntimeStorySettings().subscribe(
       stories => this.stories = stories
    );
  }
}
