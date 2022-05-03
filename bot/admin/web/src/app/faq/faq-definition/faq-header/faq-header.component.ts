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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ReplaySubject} from 'rxjs';
import {take, takeUntil} from 'rxjs/operators';
import {StateService} from 'src/app/core-nlp/state.service';
import {isDockedOrSmall, ViewMode} from '../../common/model/view-mode';
import {FaqDefinitionService} from '../../common/faq-definition.service';
import {FaqDefinitionSidepanelEditorService} from "../sidepanels/faq-definition-sidepanel-editor.service";
import {FaqDefinitionFilter} from "../../common/model/faq-definition-filter";

@Component({
  selector: 'tock-qa-header',
  templateUrl: './faq-header.component.html',
  styleUrls: ['./faq-header.component.scss']
})
export class FaqHeaderComponent implements OnInit {

  /* Template-available constants (vertical vs horizontal layout) */

  public readonly CSS_WIDER_CARD_BODY = 'd-flex align-items-center p-2 flex-wrap';
  public readonly CSS_SMALL_CARD_BODY = 'd-flex align-items-stretch p-2 flex-column flex-nowrap';

  public readonly CSS_WIDER_CARD_PART = 'col-4 d-flex justify-content-start';
  public readonly CSS_SMALL_CARD_PART = 'row-4 d-flex justify-content-start';

  public readonly CSS_WIDER_CARD_BUTTON_PART = 'col-4 d-flex justify-content-end';
  public readonly CSS_SMALL_CARD_BUTTON_PART = 'row-4 d-flex justify-content-end';

  /* Input/Output */

  @Input()
  filter: FaqDefinitionFilter;

  @Input()
  viewMode: ViewMode;

  @Output()
  onSearch = new EventEmitter<Partial<FaqDefinitionFilter>>();

  @Output()
  onImport = new EventEmitter<void>();

  @Output()
  onNew = new EventEmitter<void>();

  /* Form-Like state */
  activationStatus: Boolean = null;
  statusCycleRound = 2
  availableTags: string[] = [];

  /* Component lifecycle */

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService,
    private readonly faqService: FaqDefinitionService,
    private readonly sidepanelEditorService: FaqDefinitionSidepanelEditorService
  ) {
  }

  ngOnInit(): void {
    this.fetchAvailableTags();

    this.state.currentApplicationEmitter // when bot switch
      .pipe(takeUntil(this.destroy$))
      .subscribe(a => {
        this.fetchAvailableTags();
      });

    // when user saved a frequent answer, try also to refresh tags
    // Note: There exist more robust implementation, we use this because it is very simple
    this.sidepanelEditorService.registerOutcomeListener('save-done', 1000, this.destroy$, () => {
      this.fetchAvailableTags();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  fetchAvailableTags(): void {
    this.faqService.getAvailableTags(this.state.currentApplication._id)
      .pipe(take(1), takeUntil(this.destroy$))
      .subscribe(tags => {
        this.availableTags = tags;
      });
  }

  selectedTagsChanged(evt): void {
    this.search();
  }

  search(): void {
    this.filter.enabled = this.activationStatus;
    this.onSearch.emit(this.filter);
  }

  searchChange(value): void {
    this.search();
  }

  toggleFaqActivationSearch(value: Boolean) {
    this.toggleActivationStatus(value)
    this.search();
  }

  importFaq(): void {
    this.onImport.next(null);
  }

  newFaqDefinition(): void {
    this.onNew.next(null);
  }

  isDockedOrSmall(): boolean {
    return isDockedOrSmall(this.viewMode);
  }

  /**
   * Toggle the search activation with cycling states
   * Enabled / Disbaled / Inderterminate
   * @param value
   * @private
   */
  private toggleActivationStatus(value: Boolean) {
    let isToggable = this.statusCycleRound > 0

    if (isToggable && value != null) {
      this.activationStatus = value;
      this.statusCycleRound--
    } else {
      this.activationStatus = null;
      //put back default round available click
      this.statusCycleRound = 2
    }
  }


}
