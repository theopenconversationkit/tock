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
import { StateService } from '../../../core-nlp/state.service';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { I18nLabel, I18nLocalizedLabel } from '../../model/i18n';
import { BotService } from '../../bot-service';
import { ConnectorTypeConfiguration } from '../../../core/model/configuration';
import { BotSharedService } from '../../../shared/bot-shared.service';
import { NbToastrService } from '@nebular/theme';
import { take } from 'rxjs';

@Component({
  selector: 'tock-i18n-label',
  templateUrl: './i18n-label.component.html',
  styleUrls: ['./i18n-label.component.scss']
})
export class I18nLabelComponent implements OnInit {
  @Input() i18nLabel: I18nLabel;

  @Input() deleteLabelAllowed: boolean = true;

  @Input() showEmptyLabels: boolean = true;

  connectorTypes: ConnectorTypeConfiguration[] = [];

  @Output() labelDeleted = new EventEmitter<I18nLabel>();

  @Output() fillLabels = new EventEmitter();

  constructor(
    public state: StateService,
    private botService: BotService,
    private toastrService: NbToastrService,
    public botSharedService: BotSharedService
  ) {}

  ngOnInit(): void {
    this.botSharedService
      .getConnectorTypes()
      .pipe(take(1))
      .subscribe((c) => {
        this.connectorTypes = c.filter((conn) => !conn.connectorType.isRest());
      });
  }

  isSupportedLocale(i18n: I18nLocalizedLabel) {
    const supportedLocales = this.state.currentApplication.supportedLocales;
    return supportedLocales == null || supportedLocales.indexOf(i18n.locale) != -1;
  }

  isLocalizedVisible(i18n: I18nLocalizedLabel) {
    const isNew = !i18n.label || i18n.label.length == 0;
    return this.isSupportedLocale(i18n) || !isNew;
  }

  isLocalizedNotHidden(i18n: I18nLocalizedLabel) {
    if (!i18n.label?.length) {
      return this.showEmptyLabels;
    }

    return true;
  }

  deleteLabel() {
    let l = '';
    if (this.i18nLabel.i18n.length !== 0) {
      l = this.i18nLabel.defaultLocalizedLabel().label;
      if (!l || l.trim().length === 0) {
        l = this.i18nLabel._id;
      }
    }

    this.botService.deleteI18nLabel(this.i18nLabel).subscribe((_) => {
      this.toastrService.show(`Label ${l} deleted`, 'Delete', { duration: 3000 });
    });

    this.labelDeleted.emit(this.i18nLabel);
  }

  save() {
    this.botService.saveI18nLabel(this.i18nLabel).subscribe((_) => {
      this.toastrService.show(`Label updated`, 'Update', { duration: 3000 });
    });
  }

  removeLocalizedLabel(label: I18nLocalizedLabel) {
    this.i18nLabel.i18n.splice(this.i18nLabel.i18n.indexOf(label), 1);

    if (this.i18nLabel.i18n.length === 0 || !this.i18nLabel.i18n.some((i) => i.label.length > 0)) {
      this.deleteLabel();
    } else {
      this.save();
    }
  }

  addLocalizedLabelForConnector(label: I18nLocalizedLabel, connectorId: string) {
    this.i18nLabel.i18n.push(new I18nLocalizedLabel(label.locale, label.interfaceType, '', false, connectorId, []));
    this.save();
    this.fillLabels.emit();
  }

  addAlternative(label: I18nLocalizedLabel, index: number, value: string) {
    label.alternatives[index] = value;
    this.save();
  }

  removeAlternative(label: I18nLocalizedLabel, index: number) {
    label.alternatives.splice(index, 1);
    this.save();
  }

  addNewAlternative(label: I18nLocalizedLabel) {
    label.alternatives.push('');
  }
}
