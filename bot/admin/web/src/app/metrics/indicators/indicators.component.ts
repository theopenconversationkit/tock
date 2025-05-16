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

import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NbToastrService } from '@nebular/theme';
import { Observable, Subject, take, takeUntil } from 'rxjs';
import { DialogService } from '../../core-nlp/dialog.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { normalizedCamelCase } from '../../shared/utils';
import { IndicatorDefinition } from '../models';
import { IndicatorsEditComponent } from './indicators-edit/indicators-edit.component';
import { IndicatorsFilter } from './indicators-filters/indicators-filters.component';
import { ChoiceDialogComponent } from '../../shared/components';

export interface IndicatorEdition {
  existing: boolean;
  indicator: IndicatorDefinition;
}
@Component({
  selector: 'tock-indicators',
  templateUrl: './indicators.component.html',
  styleUrls: ['./indicators.component.scss']
})
export class IndicatorsComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  configurations: BotApplicationConfiguration[];

  @ViewChild('indicatorsEditComponent') indicatorsEditComponent: IndicatorsEditComponent;

  isSidePanelOpen = {
    edit: false
  };

  loading = {
    list: false,
    edit: false,
    delete: false
  };

  indicators: IndicatorDefinition[];
  filteredIndicators: IndicatorDefinition[];

  currentFilters: IndicatorsFilter = {
    search: null,
    dimensions: []
  };

  indicatorEdition: undefined | IndicatorEdition;

  constructor(
    private botConfiguration: BotConfigurationService,
    private stateService: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private dialogService: DialogService
  ) {}

  ngOnInit() {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy)).subscribe((confs) => {
      this.configurations = confs;
      if (confs.length) {
        this.search();
      } else {
        this.indicators = [];
        this.updateDimensionsCache();
        this.updateIndicatorsList();
        this.closeSidePanel();
      }
    });
  }

  indicatorIsNotPredefined(indicator) {
    return typeof indicator.botId === 'undefined' || indicator.botId.trim().length > 0;
  }

  search(): void {
    this.loading.list = true;
    const url = `/bot/${this.stateService.currentApplication.name}/indicators`;
    this.rest
      .get(url, (indicators) => indicators)
      .pipe(take(1))
      .subscribe((indicators) => {
        this.indicators = indicators;
        this.updateDimensionsCache();
        this.updateIndicatorsList();
        this.loading.list = false;
      });
  }

  updateIndicatorsList(): void {
    this.filteredIndicators = this.indicators.filter((indicator) => {
      if (this.currentFilters.search) {
        if (
          !indicator.label.toLowerCase().includes(this.currentFilters.search.toLowerCase()) &&
          !indicator.description.toLowerCase().includes(this.currentFilters.search.toLowerCase())
        )
          return false;
      }

      if (this.currentFilters.dimensions.length) {
        if (!indicator.dimensions.some((dimension) => this.currentFilters.dimensions.includes(dimension))) return false;
      }

      return true;
    });
    this.filteredIndicators.sort((a, b) => a.name.localeCompare(b.name));
  }

  filterIndicators(filters: IndicatorsFilter): void {
    this.currentFilters = filters;
    this.updateIndicatorsList();
  }

  dimensionsCache: string[] = [];

  updateDimensionsCache(): void {
    this.dimensionsCache = [
      ...new Set(
        <string>[].concat.apply(
          [...this.dimensionsCache],
          this.indicators.map((v: IndicatorDefinition) => v.dimensions)
        )
      )
    ].sort();
  }

  closeSidePanel(): void {
    this.isSidePanelOpen.edit = false;
    this.indicatorEdition = undefined;
  }

  addOrEditIndicator(indicator?: IndicatorDefinition): void {
    if (this.indicatorsEditComponent) {
      this.indicatorsEditComponent
        .close()
        .pipe(take(1))
        .subscribe((res) => {
          if (res != 'cancel') {
            if (indicator) this.editIndicator(indicator);
            else this.addIndicator();
          }
        });
    } else {
      if (indicator) this.editIndicator(indicator);
      else this.addIndicator();
    }
  }

  addIndicator(): void {
    this.indicatorEdition = {
      existing: false,
      indicator: {
        name: '',
        label: '',
        description: '',
        values: [],
        dimensions: []
      }
    };
    this.isSidePanelOpen.edit = true;
  }

  editIndicator(indicator: IndicatorDefinition): void {
    this.indicatorEdition = {
      existing: true,
      indicator: indicator
    };
    this.isSidePanelOpen.edit = true;
  }

  getUnicIndicatorName(indicator: IndicatorDefinition): string {
    let candidate = normalizedCamelCase(indicator.label);
    let count = 1;
    const candidateBase = candidate;
    while (this.indicators.find((indctr) => indctr.name === candidate)) {
      candidate = candidateBase + count++;
    }
    return candidate;
  }

  saveOrCreateIndicator(indicatorEdition: IndicatorEdition): void {
    this.loading.edit = true;

    let url: string;
    let type: 'post' | 'put';
    let toastLabel: string;
    let method: Observable<IndicatorDefinition>;

    if (!indicatorEdition.existing) {
      type = 'post';
      url = `/bot/${this.stateService.currentApplication.name}/indicators`;
      toastLabel = 'created';
      indicatorEdition.indicator.name = this.getUnicIndicatorName(indicatorEdition.indicator);
      method = this.rest.post(url, indicatorEdition.indicator);
    } else {
      type = 'put';
      url = `/bot/${this.stateService.currentApplication.name}/indicators/${indicatorEdition.indicator.name}`;
      toastLabel = 'updated';
      method = this.rest.put(url, indicatorEdition.indicator);
    }

    method.pipe(take(1)).subscribe({
      next: (newIndicator: IndicatorDefinition) => {
        if (type === 'post') this.indicators.push(newIndicator);
        else {
          const updatedIndicatorIndex = this.indicators.findIndex((indctr) => indctr.name === indicatorEdition.indicator.name);
          this.indicators[updatedIndicatorIndex] = { ...this.indicators[updatedIndicatorIndex], ...newIndicator };
        }

        this.updateIndicatorsList();
        this.updateDimensionsCache();

        this.toastrService.success(`Indicator successfully ${toastLabel}`, 'Success', {
          duration: 5000,
          status: 'success'
        });
        this.isSidePanelOpen.edit = false;
        this.loading.edit = false;
      },
      error: () => {
        this.loading.edit = false;
      }
    });
  }

  confirmDeleteIndicator(indicator: IndicatorDefinition): void {
    const action = 'delete';
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: 'Delete an indicator',
        subtitle: `Are you sure you want to delete the indicator "${indicator.label}" ?`,
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) {
        this.deleteIndicator(indicator);
      }
    });
  }

  private deleteIndicator(indicator: IndicatorDefinition): void {
    this.loading.delete = true;
    const indicatorName = indicator.name;
    const url = `/bot/${this.stateService.currentApplication.name}/indicators/${indicator.name}`;
    this.rest
      .delete(url)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.indicators = this.indicators.filter((f) => f.name != indicatorName);
          this.updateIndicatorsList();

          this.toastrService.success(`Faq successfully deleted`, 'Success', {
            duration: 5000,
            status: 'success'
          });

          this.closeSidePanel();
          this.loading.delete = false;
        },
        error: () => {
          this.loading.delete = false;
        }
      });
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
