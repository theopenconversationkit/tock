import { Component, Input, OnInit } from '@angular/core';
import { StateService } from '../../core-nlp/state.service';
import { Application, NlpApplicationConfiguration, NlpModelConfiguration } from '../../model/application';
import { ApplicationService } from '../../core-nlp/applications.service';
import { NlpEngineType } from '../../model/nlp';
import { Subject } from 'rxjs';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { ApplicationUploadComponent } from '../application-upload/application-upload.component';
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
