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
import {FaqQaFilter} from '../qa-grid/qa-grid.component';
import {QaSidepanelEditorService} from "../sidepanels/qa-sidepanel-editor.service";

@Component({
  selector: 'tock-qa-header',
  templateUrl: './qa-header.component.html',
  styleUrls: ['./qa-header.component.scss']
})
export class QaHeaderComponent implements OnInit {

  /* Template-available constants (vertical vs horizontal layout) */

  public readonly CSS_WIDER_CARD_BODY = 'd-flex align-items-center p-2 flex-wrap';
  public readonly CSS_SMALL_CARD_BODY = 'd-flex align-items-stretch p-2 flex-column flex-nowrap';

  public readonly CSS_WIDER_CARD_PART = 'col-4 d-flex justify-content-start';
  public readonly CSS_SMALL_CARD_PART = 'row-4 d-flex justify-content-start';

  /* Input/Output */

  @Input()
  filter: FaqQaFilter;

  @Input()
  viewMode: ViewMode;

  @Output()
  onSearch = new EventEmitter<Partial<FaqQaFilter>>();

  @Output()
  onImport = new EventEmitter<void>();

  @Output()
  onNew = new EventEmitter<void>();

  /* Form-Like state */

  onlyActives = false;
  availableTags: string[] = [];

  /* Component lifecycle */

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService,
    private readonly qaService: FaqDefinitionService,
    private readonly sidepanelEditorService: QaSidepanelEditorService
  ) { }

  ngOnInit(): void {
    this.fetchAvailableTags();

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
    const applicationId = this.state.currentApplication._id;

    this.qaService.getAvailableTags(applicationId)
      .pipe(take(1), takeUntil(this.destroy$))
      .subscribe(tags => {
        this.availableTags = tags;
      });
  }

  selectedTagsChanged(evt): void {
    this.search();
  }

  search(): void {
    this.filter.onlyActives = (true === this.onlyActives);
    this.onSearch.emit(this.filter);
  }

  searchChange(value): void {
    this.search();
  }

  toggleOnlyActives(value: boolean) {
    this.onlyActives = value;
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


}
