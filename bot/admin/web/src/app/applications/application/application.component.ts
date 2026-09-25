import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StateService } from '../../core-nlp/state.service';
import { Application } from '../../model/application';
import { ApplicationService } from '../../core-nlp/applications.service';
import { Subject } from 'rxjs';
import { NlpEngineType } from '../../model/nlp';
import { NbToastrService } from '@nebular/theme';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'tock-application',
    templateUrl: './application.component.html',
    styleUrls: ['./application.component.scss'],
    standalone: false
})
export class ApplicationComponent implements OnInit {
  applications: Application[];
  application: Application;
  newApplication: boolean;
  newLocale: string;
  nlpEngineType: string;
  nlpEngineTypeChange: Subject<NlpEngineType> = new Subject();

  @ViewChild('appLabel') appLabel: ElementRef;

  constructor(
    private route: ActivatedRoute,
    private toastrService: NbToastrService,
    public state: StateService,
    private applicationService: ApplicationService,
    private router: Router,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    this.applications = this.state.applications;
    this.route.params.subscribe((params) => {
      const id = params['id'];
      if (id && id.length !== 0) {
        this.application = this.applications.find((a) => a._id === id);
        if (this.application) {
          this.application = this.application.clone();
        }
      } else {
        this.newApplication = true;
        this.application = new Application(
          '',
          '',
          this.state.user.organization,
          [],
          [],
          StateService.DEFAULT_ENGINE,
          true,
          true,
          false,
          0.7,
          0.1,
          false,
          []
        );
      }
      this.nlpEngineType = this.application.nlpEngineType.name;
      if (this.application) {
        setTimeout((_) => {
          this.appLabel.nativeElement.focus();
        });
      }
    });
  }

  format(): void {
    this.formatName(this.application.label);
  }

  private formatName(label: string): void {
    if (label && this.newApplication) {
      this.application.name = label
        .replace(/[^A-Za-z0-9_-]*/g, '')
        .toLowerCase()
        .trim();
    }
  }

  saveApplication(): void {
    this.format();
    if (this.application.name.trim().length === 0) {
      this.toastrService.show(
        this.transloco.translate('applications.application.chooseApplicationNameError'),
        this.transloco.translate('applications.application.errorTitle'),
        {
          duration: 5000,
          status: 'warning'
        }
      );
    } else if (this.application.supportedLocales.length === 0) {
      this.toastrService.show(
        this.transloco.translate('applications.application.chooseLocaleError'),
        this.transloco.translate('applications.application.errorTitle'),
        {
          duration: 5000,
          status: 'warning'
        }
      );
    } else {
      this.application.nlpEngineType = this.state.supportedNlpEngines.find((e) => e.name === this.nlpEngineType);
      this.applicationService.saveApplication(this.application).subscribe({
        next: (app) => {
          this.applicationService.refreshCurrentApplication(app);
          this.toastrService.show(
            this.transloco.translate('applications.application.applicationSaved', { name: app.name }),
            this.transloco.translate('applications.application.saveApplicationTitle'),
            {
              duration: 2000,
              status: 'success'
            }
          );
          if (this.newApplication && this.state.applications.length === 1) {
            this.router.navigateByUrl('/nlp/try');
          } else {
            this.redirect();
          }
        },
        error: (error) => {
          this.toastrService.show(error, this.transloco.translate('common.messages.error'), { status: 'danger' });
        }
      });
    }
  }

  private redirect(): void {
    let redirect = '../../';
    if (this.newApplication) {
      redirect = '../';
    }
    this.router.navigate([redirect], { relativeTo: this.route });
  }

  cancel(): void {
    this.redirect();
  }

  removeLocale(locale: string): void {
    this.application.supportedLocales.splice(this.application.supportedLocales.indexOf(locale), 1);
  }

  addLocale(): void {
    this.application.supportedLocales.push(this.newLocale);
    this.toastrService.show(
      this.transloco.translate('applications.application.localeAdded', { locale: this.state.localeName(this.newLocale) }),
      this.transloco.translate('applications.application.localeTitle'),
      {
        duration: 2000,
        status: 'success'
      }
    );
  }

  changeNlpEngine(type: string): void {
    this.nlpEngineTypeChange.next(this.state.supportedNlpEngines.find((e) => e.name === type));
  }
}
