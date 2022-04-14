/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { Router } from '@angular/router';
import {ReplaySubject, Subscription} from 'rxjs';
import {takeUntil} from "rxjs/operators";

import {StateService} from 'src/app/core-nlp/state.service';
import {isDockedOrSmall} from '../common/model/view-mode';
import {DEFAULT_PANEL_NAME, WithSidePanel} from '../common/mixin/with-side-panel';
import {blankFaqDefinition, FaqDefinition} from '../common/model/faq-definition';
import {FaqQaFilter, FaqGridComponent} from './faq-grid/faq-grid.component';
import {truncate} from '../common/util/string-utils';
import {DialogService} from 'src/app/core-nlp/dialog.service';
import {FormProblems, InvalidFormProblems} from '../common/model/form-problems';
import {FaqDefinitionSidepanelEditorService} from "./sidepanels/faq-definition-sidepanel-editor.service";
import { Utterance } from '../common/model/utterance';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';

// Specific action payload
export type EditorTabName = 'Info' | 'Answer' | 'Question';

@Component({
  selector: 'tock-qa',
  templateUrl: './faq-definition.component.html',
  styleUrls: ['./faq-definition.component.scss']
})
export class FaqDefinitionComponent extends WithSidePanel() implements OnInit, OnDestroy {

  activeQaTab: EditorTabName = 'Info';

  configurations: BotApplicationConfiguration[];

  currentItem?: FaqDefinition;

  editorPanelName?: string;

  editorFormValid = false;
  editorFormWarnings: string[] = [];

  initUtterance: Utterance;

  subscriptions = new Subscription();

  public filter: FaqQaFilter;
  @ViewChild(FaqGridComponent) grid;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService,
    private readonly sidepanelEditorService: FaqDefinitionSidepanelEditorService,
    private readonly dialog: DialogService,
    private readonly botConfiguration: BotConfigurationService,
    private readonly router: Router
  ) {
    super();

    this.initUtterance = this.router.getCurrentNavigation().extras?.state?.question;
  }

  ngOnInit(): void {
    this.clearFilter();

    this.initSidePanel(this.destroy$);

    this.state.currentApplicationEmitter // when bot switch
      .pipe(takeUntil(this.destroy$))
      .subscribe(a => {
        this.currentItem = undefined;
        this.clearFilter();
        this.undock();
      });

      if(this.initUtterance){
        this.openNewSidepanel(this.initUtterance);
      }

    this.subscriptions.add(
      this.botConfiguration.configurations.subscribe(confs => {
        this.configurations = confs;
      })
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();

    this.subscriptions.unsubscribe();
  }

  clearFilter(): void {
    this.filter = {
      sort: [],
      search: null,
      tags: [],
      clone: function () {
        return {...this};
      }
    };
  }

  search(filter: Partial<FaqQaFilter>): void {
    this.filter.search = filter.search;
    this.filter.sort = filter.sort;
    this.grid.refresh();
  }

  openImportSidepanel(): void {
    this.dock("import");
  }

  edit(fq: FaqDefinition): void {
    this.editorPanelName = 'Edit FAQ';
    this.currentItem = fq;
    this.dock("edit");
  }

  dock(name = DEFAULT_PANEL_NAME): void {
    if (name !== 'edit') {
      this.sidepanelEditorService.leaveEditMode(); // tell other components we are done with editing now
    }
    super.dock(name); // toggle the docked/undocked state
  }

  undock(): void {
    this.sidepanelEditorService.leaveEditMode(); // tell other components we are done with editing now
    super.undock(); // toggle the docked/undocked state
  }

  onEditorValidityChanged(report: FormProblems): void {
    window.setTimeout(() => { // ExpressionChangedAfterItHasBeenCheckedError workaround
      // enable/disable 'save' button
      this.editorFormValid = report.formValid;

      // extract error labels
      this.editorFormWarnings = report.formValid ? [] : (<InvalidFormProblems>report).items.map(item => {
        return item.errorLabel;
      });
    }, 0);
  }

  openNewSidepanel(initUtterance?:Utterance) {
    const currentLocale = this.state.currentLocale;
    const applicationId = this.state.currentApplication._id;

    this.editorPanelName = 'New FAQ';
    this.currentItem = blankFaqDefinition({applicationId, language: currentLocale});
    this.activeQaTab = 'Info';

    if(initUtterance) {
      this.currentItem.utterances = [initUtterance];
      this.activeQaTab = 'Question';
    }

    this.dock("edit");
  }

  activateEditorTab(tabName: EditorTabName): void {
    this.activeQaTab = tabName;
  }

  async save(): Promise<any> {
    const fq = await this.sidepanelEditorService.save(this.destroy$);
    this.currentItem = fq;

    this.dialog.notify(`Saved`,
      truncate(fq.title || ''), {duration: 2000, status: "basic"});

    this.grid.refresh();
  }

  isDockedOrSmall(): boolean {
    return isDockedOrSmall(this.viewMode);
  }

}
