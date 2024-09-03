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

import { Component, Input, OnInit } from '@angular/core';
import { StateService } from '../../core-nlp/state.service';
import { Application, NlpApplicationConfiguration, NlpModelConfiguration } from '../../model/application';
import { ApplicationService } from '../../core-nlp/applications.service';
import { saveAs } from 'file-saver-es';
import { ApplicationScopedQuery } from '../../model/commons';
import { NlpEngineType } from '../../model/nlp';
import { Subject } from 'rxjs';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { ApplicationUploadComponent } from '../application-upload/application-upload.component';
import { getExportFileName } from '../../shared/utils';

@Component({
  selector: 'tock-application-advanced-options',
  templateUrl: './application-advanced-options.component.html',
  styleUrls: ['./application-advanced-options.component.scss']
})
export class ApplicationAdvancedOptionsComponent implements OnInit {
  @Input()
  application: Application;
  @Input()
  nlpEngineTypeChange: Subject<NlpEngineType>;
  exportAlexa: boolean = false;
  alexaLocale: string;
  tokenizerProperties: string;
  intentClassifierProperties: string;
  entityClassifierProperties: string;

  constructor(
    private toastrService: NbToastrService,
    private nbDialogService: NbDialogService,
    public state: StateService,
    private applicationService: ApplicationService
  ) {}

  ngOnInit(): void {
    if (this.application && this.application.supportedLocales.length > 0) {
      this.alexaLocale = this.application.supportedLocales[0];
    }
    this.nlpEngineTypeChange.subscribe((type) => {
      this.application.nlpEngineType = type;
      if (this.tokenizerProperties) {
        this.displayConfiguration();
      }
    });
  }

  showUploadDumpPanel(): void {
    this.nbDialogService.open(ApplicationUploadComponent, {
      context: {
        applicationName: this.application.name
      }
    });
  }

  triggerBuild(): void {
    this.applicationService
      .triggerBuild(this.application)
      .subscribe((_) => this.toastrService.show(`Application build started`, 'Build', { duration: 2000 }));
  }

  downloadAlexaExport(): void {
    setTimeout((_) => {
      const query = new ApplicationScopedQuery(this.application.namespace, this.application.name, this.alexaLocale);
      this.applicationService.getAlexaExport(query).subscribe((blob) => {
        this.exportAlexa = false;

        const exportFileName = getExportFileName(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          'alexa',
          'json'
        );
        saveAs(blob, exportFileName);

        this.toastrService.show(`Alexa export file provided`, 'Alexa', { duration: 2000 });
      });
    });
  }

  displayConfiguration(): void {
    this.applicationService.getNlpConfiguration(this.application._id, this.application.nlpEngineType).subscribe((m) => {
      this.tokenizerProperties = m.tokenizerConfiguration.toProperties();
      this.intentClassifierProperties = m.intentConfiguration.toProperties();
      this.entityClassifierProperties = m.entityConfiguration.toProperties();
    });
  }

  updateConfiguration(): void {
    const m = new NlpApplicationConfiguration(
      NlpModelConfiguration.parseProperties(this.tokenizerProperties),
      NlpModelConfiguration.parseProperties(this.intentClassifierProperties),
      NlpModelConfiguration.parseProperties(this.entityClassifierProperties)
    );
    this.applicationService.updateModelConfiguration(this.application._id, this.application.nlpEngineType, m).subscribe((_) => {
      this.tokenizerProperties = null;
      this.intentClassifierProperties = null;
      this.entityClassifierProperties = null;
    });
  }
}
