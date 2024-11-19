import { Injectable } from '@angular/core';
import { NbWindowRef, NbWindowService, NbWindowState } from '@nebular/theme';
import { TestDialogComponent } from './test-dialog.component';
import { TestMessage } from '../../../test/model/test';
import { Subject } from 'rxjs';
import { ConnectorType } from '../../../core/model/configuration';

export interface openTestDialogOptions {
  sentenceText?: string;
  sentenceLocale?: string;
  connectorType?: ConnectorType;
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
      this.windowRef.close();
    } else {
      this.windowRef?.close();
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

  private defineConnectorTypeSubject = new Subject<ConnectorType>();
  defineConnectorTypeObservable = this.defineConnectorTypeSubject.asObservable();

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

    if (options.connectorType) {
      this.defineConnectorTypeSubject.next(options.connectorType);
    }

    if (options.sentenceText?.trim().length) {
      this.testSentenceSubject.next(options.sentenceText);
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
