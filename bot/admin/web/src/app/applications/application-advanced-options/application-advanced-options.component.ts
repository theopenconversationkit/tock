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
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'tock-application-advanced-options',
    templateUrl: './application-advanced-options.component.html',
    styleUrls: ['./application-advanced-options.component.scss'],
    standalone: false
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
    private applicationService: ApplicationService,
    private transloco: TranslocoService
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
      .subscribe((_) =>
        this.toastrService.show(
          this.transloco.translate('applications.application-advanced-options.buildStarted'),
          this.transloco.translate('applications.application-advanced-options.buildTitle'),
          { duration: 2000 }
        )
      );
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

        this.toastrService.show(
          this.transloco.translate('applications.application-advanced-options.alexaExportProvided'),
          this.transloco.translate('applications.application-advanced-options.alexaTitle'),
          { duration: 2000 }
        );
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
