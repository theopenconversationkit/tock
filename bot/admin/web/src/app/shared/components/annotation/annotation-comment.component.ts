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

import { Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { AnnotationEvent } from './annotations';

@Component({
  selector: 'tock-annotation-comment',
  templateUrl: './annotation-comment.component.html',
  styleUrl: './annotation-comment.component.scss'
})
export class AnnotationCommentComponent {
  @Input() event: AnnotationEvent;

  @Output() commentChange = new EventEmitter<{ event: AnnotationEvent; value: string }>();
  @Output() commentDelete = new EventEmitter<AnnotationEvent>();

  @ViewChild('commentEditInput') commentEditInput: ElementRef;

  edit(): void {
    this.event._edited = true;
  }

  commentSubmittable(): boolean {
    const currVal = this.commentEditInput?.nativeElement?.value;
    return currVal && currVal.trim().length && currVal !== this.event.comment;
  }

  cancel(): void {
    this.event._edited = false;
  }

  submit(): void {
    this.commentChange.emit({ event: this.event, value: this.commentEditInput.nativeElement.value });
    this.cancel();
  }

  delete(): void {
    this.commentDelete.emit(this.event);
  }
}
