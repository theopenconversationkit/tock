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
