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
import { FormControl, FormGroup } from '@angular/forms';
import { ConnectorType } from '../../../../core/model/configuration';
import { Subject, debounceTime, take, takeUntil } from 'rxjs';
import { ExtractFormControlTyping } from '../../../../shared/utils/typescript.utils';
import { BotSharedService } from '../../../../shared/bot-shared.service';
import { StateService } from '../../../../core-nlp/state.service';
import { BotConfigurationService } from '../../../../core/bot-configuration.service';
import { RestService } from '../../../../core-nlp/rest/rest.service';
import {
  AnnotationReason,
  AnnotationReasons,
  AnnotationState,
  AnnotationStates
} from '../../../../shared/components/annotation/annotations';
import { SortOrder, SortOrders } from '../../../../shared/model/misc';

interface DialogListFiltersForm {
  exactMatch: FormControl<boolean>;
  displayTests: FormControl<boolean>;
  dialogId?: FormControl<string>;
  text?: FormControl<string>;
  intentName?: FormControl<string>;
  connectorType?: FormControl<ConnectorType>;
  ratings?: FormControl<number[]>;
  configuration?: FormControl<string>;
  intentsToHide?: FormControl<string[]>;
  isGenAiRagDialog?: FormControl<boolean>;
  dialogSort?: FormControl<SortOrder>;
  dialogCreationDateFrom?: FormControl<Date>;
  dialogCreationDateTo?: FormControl<Date>;
  withAnnotations?: FormControl<boolean>;
  annotationStates?: FormControl<AnnotationState[]>;
  annotationReasons?: FormControl<AnnotationReason[]>;
  annotationSort?: FormControl<SortOrder>;
  annotationCreationDateFrom?: FormControl<Date>;
  annotationCreationDateTo?: FormControl<Date>;
}

export type DialogListFilters = ExtractFormControlTyping<DialogListFiltersForm>;

@Component({
  selector: 'tock-dialogs-list-filters',
  templateUrl: './dialogs-list-filters.component.html',
  styleUrl: './dialogs-list-filters.component.scss'
})
export class DialogsListFiltersComponent implements OnInit {
  private readonly destroy$: Subject<boolean> = new Subject();

  advanced: boolean = false;
  connectorTypes: ConnectorType[] = [];
  configurationNameList: { label: string; applicationId: string }[];

  annotationStates = AnnotationStates;
  annotationReasons = AnnotationReasons;
  sortOrders = SortOrders;

  @Input() initialFilters: Partial<DialogListFilters>;
  @Output() onFilter = new EventEmitter<Partial<DialogListFilters>>();

  constructor(public botSharedService: BotSharedService, public state: StateService, private botConfiguration: BotConfigurationService) {}

  ngOnInit() {
    this.botSharedService
      .getConnectorTypes()
      .pipe(take(1))
      .subscribe((conf) => {
        this.connectorTypes = conf.map((it) => it.connectorType);
      });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((configs) => {
      this.configurationNameList = configs
        .filter((item) => item.targetConfigurationId == null)
        .map((item) => {
          const label = `${item.name} > ${item.connectorType.label()} (${item.applicationId})`;
          return { label: label, applicationId: item.applicationId };
        });
    });

    if (this.initialFilters) {
      this.form.patchValue(this.initialFilters);
    }

    this.form.valueChanges.pipe(debounceTime(500), takeUntil(this.destroy$)).subscribe(() => this.submitFiltersChange());
  }

  form = new FormGroup<DialogListFiltersForm>({
    exactMatch: new FormControl(),
    displayTests: new FormControl(),
    dialogId: new FormControl(),
    text: new FormControl(),
    intentName: new FormControl(),
    connectorType: new FormControl(),
    ratings: new FormControl(),
    configuration: new FormControl(),
    intentsToHide: new FormControl([]),
    isGenAiRagDialog: new FormControl(),
    dialogSort: new FormControl(),
    dialogCreationDateFrom: new FormControl(),
    dialogCreationDateTo: new FormControl(),
    withAnnotations: new FormControl(),
    annotationStates: new FormControl([]),
    annotationReasons: new FormControl([]),
    annotationSort: new FormControl(),
    annotationCreationDateFrom: new FormControl(),
    annotationCreationDateTo: new FormControl()
  });

  getFormControl(formControlName: string): FormControl {
    return this.form.get(formControlName) as FormControl;
  }

  submitFiltersChange(): void {
    const formValue = this.form.value;

    this.onFilter.emit(formValue);
  }

  resetControl(ctrl: FormControl, input?: HTMLInputElement): void {
    ctrl.reset();
    if (input) {
      input.value = '';
    }
  }

  patchControl(ctrl: FormControl, value: any): void {
    ctrl.patchValue(value);
  }

  swapAdvanced(): void {
    this.advanced = !this.advanced;
  }

  getConnectorTypeIconById(connectorId: string): string {
    if (connectorId === null) connectorId = 'web';
    return RestService.connectorIconUrl(connectorId);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
