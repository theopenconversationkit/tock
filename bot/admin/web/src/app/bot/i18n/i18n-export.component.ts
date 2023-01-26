/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { NbButtonComponent, NbWindowRef } from '@nebular/theme';

const CSV = { value: 'CSV', label: '.csv' };
const JSON = { value: 'JSON', label: '.json' };

@Component({
  selector: 'tock-i18n-export-action',
  templateUrl: './i18n-export.component.html',
  styleUrls: ['./i18n-export.component.css']
})
export class I18nExportComponent implements AfterViewInit {
  constructor(private windowRef: NbWindowRef) {}

  @ViewChild('focusElement') focusElement: NbButtonComponent;

  exportToOption: { value: string; label: string } = CSV;

  exportToOptions: { value: string; label: string }[] = [CSV, JSON];
  exportAllOption = 'All labels';
  exportAllOptions: string[] = ['All labels', 'Only filtered labels'];
  loading = false;

  ngAfterViewInit(): void {
    const focusElement = this.focusElement['hostElement']?.nativeElement;
    if (focusElement) {
      focusElement.focus();
    }
  }

  export(): void {
    this.loading = true;
    const exportAs = this.windowRef.config.context['exportAs'];
    if (exportAs) {
      exportAs(this.exportToOption.value, this.exportAllOption === 'All labels');
    }
    this.loading = false;
    this.closeWindow();
  }

  closeWindow(): void {
    this.windowRef.close();
  }
}
