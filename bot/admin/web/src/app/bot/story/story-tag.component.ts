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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

interface Tag {
  value: string;
  label: string;
}

@Component({
  selector: 'tock-story-tag',
  templateUrl: './story-tag.component.html',
  styleUrls: ['./story-tag.component.css']
})
export class StoryTagComponent implements OnInit {

  @Input()
  selectedTag: string;

  @Output()
  selectedTagChange: EventEmitter<String> = new EventEmitter<String>();

  tags: Tag[] = [
    {value: 'ENABLE', label: 'Bot activation'},
    {value: 'DISABLE', label: 'Bot deactivation'}
  ];

  ngOnInit(): void {
    if (!this.selectedTag) {
      this.selectedTag = '';
    }
  }

}
