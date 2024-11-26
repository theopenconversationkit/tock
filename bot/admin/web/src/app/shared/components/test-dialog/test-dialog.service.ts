import { Injectable } from '@angular/core';
import { NbWindowRef, NbWindowService, NbWindowState } from '@nebular/theme';
import { TestDialogComponent } from './test-dialog.component';
import { TestMessage } from '../../../test/model/test';
import { Subject } from 'rxjs';

export interface openTestDialogOptions {
  sentenceText?: string;
  sentenceLocale?: string;
  applicationId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TestDialogService {
  constructor(private nbWindowService: NbWindowService) {}

  windowRef: NbWindowRef<any, any>;

  openTestDialog() {
    if (this.windowRef) {
      if (this.windowRef.state === NbWindowState.MINIMIZED) {
        this.windowRef.maximize();
      } else {
        this.windowRef.close();
      }
    } else {
      this.windowRef = this.nbWindowService.open(TestDialogComponent, {
        title: 'Test dialog',
        windowClass: 'test-dialog-window',
        closeOnBackdropClick: false,
        initialState: NbWindowState.MAXIMIZED
      });

      this.windowRef.onClose.subscribe(() => {
        this.messages = undefined;
        this.windowRef = undefined;
      });
    }
  }

  private defineLocaleSubject = new Subject<string>();
  defineLocaleObservable = this.defineLocaleSubject.asObservable();

  private defineApplicationIdSubject = new Subject<string>();
  defineApplicationIdObservable = this.defineApplicationIdSubject.asObservable();

  private testSentenceSubject = new Subject<string>();
  testSentenceObservable = this.testSentenceSubject.asObservable();

  testSentenceDialog(options: openTestDialogOptions) {
    if (!this.windowRef) this.openTestDialog();

    if (this.windowRef.state === NbWindowState.MINIMIZED) {
      this.windowRef.maximize();
      setTimeout(() => {
        this.testSentenceDialog(options);
      });
      return;
    }

    if (options.sentenceLocale) {
      this.defineLocaleSubject.next(options.sentenceLocale);
    }

    if (options.applicationId) {
      this.defineApplicationIdSubject.next(options.applicationId);
    }

    if (options.sentenceText?.trim().length) {
      // small timeout to avoid reponse of getRecentSentences to hide the load spinner
      setTimeout(() => {
        this.testSentenceSubject.next(options.sentenceText);
      }, 200);
    }

    this.bringToFront();
  }

  bringToFront() {
    const hasManyOverlays = document.getElementsByClassName('cdk-global-overlay-wrapper');
    if (hasManyOverlays?.length > 1) {
      const currentOverlay = this.windowRef.componentRef.location.nativeElement.closest('.cdk-global-overlay-wrapper');
      const otherOverlays = Array.from(hasManyOverlays).filter((ovl) => ovl !== currentOverlay);
      let maxZIndex = 0;
      otherOverlays.forEach((ovl) => {
        const zIndex = window.getComputedStyle(ovl).zIndex;
        if (zIndex && +zIndex > maxZIndex) maxZIndex = +zIndex;
      });
      currentOverlay.style.zIndex = maxZIndex + 1;
    }
  }

  messages: TestMessage[];

  storeMessages(messages) {
    this.messages = messages;
  }

  getStoredMessages() {
    return this.messages;
  }
}
