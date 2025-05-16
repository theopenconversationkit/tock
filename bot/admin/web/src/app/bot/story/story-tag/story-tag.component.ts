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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

interface Tag {
  value: string;
  label: string;
}

@Component({
  selector: 'tock-story-tag',
  templateUrl: './story-tag.component.html',
  styleUrls: ['./story-tag.component.scss']
})
export class StoryTagComponent implements OnInit {
  @Input()
  selectedTag: string;

  @Output()
  selectedTagChange: EventEmitter<string> = new EventEmitter<string>();

  tags: Tag[] = [
    { value: 'ENABLE', label: 'Trigger Bot activation' },
    { value: 'DISABLE', label: 'Trigger Bot deactivation' },
    {
      value: 'CHECK_ONLY_SUB_STEPS',
      label: 'Only sub actions entities are checked for action selection'
    },
    {
      value: 'CHECK_ONLY_SUB_STEPS_WITH_STORY_INTENT',
      label: 'Only intents supported by the story are checked for entity action selection'
    },
    {
      value: 'ASK_AGAIN',
      label: 'Ask again the story if the answer is something else than expected'
    }
  ];

  ngOnInit(): void {
    if (!this.selectedTag) {
      this.selectedTag = '';
    }
  }
}
