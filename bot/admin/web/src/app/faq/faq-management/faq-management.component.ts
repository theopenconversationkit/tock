import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';

import { BotApplicationConfiguration } from '../../core/model/configuration';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { PaginatedQuery } from '../../model/commons';
import { FaqDefinition, FaqFilter, FaqSearchQuery, PaginatedFaqResult } from '../models';
import { FaqManagementEditComponent } from './faq-management-edit/faq-management-edit.component';
import { FaqManagementSettingsComponent } from './faq-management-settings/faq-management-settings.component';
import { Pagination } from '../../shared/components';
import { getExportFileName } from '../../shared/utils';
import { saveAs } from 'file-saver-es';

export type FaqDefinitionExtended = FaqDefinition & { _initQuestion?: string };

@Component({
  selector: 'tock-faq-management',
  templateUrl: './faq-management.component.html',
  styleUrls: ['./faq-management.component.scss']
})
export class FaqManagementComponent implements OnInit, OnDestroy {
  @ViewChild('faqEditComponent') faqEditComponent: FaqManagementEditComponent;
  @ViewChild('faqSettingsComponent') faqSettingsComponent: FaqManagementSettingsComponent;

  configurations: BotApplicationConfiguration[];

  destroy = new Subject();

  faqs: FaqDefinitionExtended[];
  faqEdit: FaqDefinitionExtended;

  isSidePanelOpen = {
    edit: false,
    settings: false
  };

  loading = {
    delete: false,
    edit: false,
    list: false
  };

  initQuestion: string;
  initAnswer: string;

  constructor(
    private botConfiguration: BotConfigurationService,
    private rest: RestService,
    private stateService: StateService,
    private toastrService: NbToastrService,
    private location: Location
  ) {
    this.initQuestion = (this.location.getState() as any)?.question;
    this.initAnswer = (this.location.getState() as any)?.answer;
  }

  ngOnInit(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy)).subscribe((confs) => {
      this.configurations = confs;

      if (confs.length) {
        this.search();
        this.closeSidePanel();

        let initQuestion;
        if (this.initQuestion) {
          initQuestion = this.initQuestion;
          this.initQuestion = undefined;
        }

        let initAnswer;
        if (this.initAnswer) {
          initAnswer = this.initAnswer;
          this.initAnswer = undefined;
        }

        if (initQuestion || initAnswer) {
          this.addFaq(initQuestion, initAnswer);
        }
      }
    });
  }

  pagination: Pagination = {
    start: 0,
    end: undefined,
    size: 10,
    total: undefined
  };

  paginationChange(pagination: Pagination): void {
    this.search(this.pagination.start, this.pagination.size);
  }

  onScroll(): void {
    if (this.loading.list || this.pagination.end >= this.pagination.total) return;
    return this.search(this.pagination.end, this.pagination.size, true, false);
  }

  currentFilters: FaqFilter = {
    search: null,
    tags: [],
    enabled: undefined,
    sort: []
  };

  filterFaqs(filters: FaqFilter): void {
    this.currentFilters = filters;
    this.search();
  }

  toSearchQuery(query: PaginatedQuery): FaqSearchQuery {
    return new FaqSearchQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      this.currentFilters.tags,
      null /* NOTE: There is a weird behavior when set */,
      this.currentFilters.search,
      this.currentFilters.sort,
      this.currentFilters.enabled
    );
  }

  tagsCache: string[] = [];

  updateTagsCache() {
    this.tagsCache = [
      ...new Set(
        <string>[].concat.apply(
          [...this.tagsCache],
          this.faqs.map((v: FaqDefinitionExtended) => v.tags)
        )
      )
    ].sort();
  }

  search(
    start: number = 0,
    size: number = this.pagination.size,
    add: boolean = false,
    showLoadingSpinner: boolean = true,
    partialReload: boolean = false
  ): void {
    if (showLoadingSpinner) this.loading.list = true;

    let query: PaginatedQuery = this.stateService.createPaginatedQuery(start, size);
    const request = this.toSearchQuery(query);

    this.rest
      .post('/faq/search', request)
      .pipe(takeUntil(this.destroy))
      .subscribe((faqs: PaginatedFaqResult) => {
        this.pagination.total = faqs.total;
        if (!partialReload || this.pagination.end > this.pagination.total) {
          this.pagination.end = faqs.end;
        }

        if (add) {
          this.faqs = [...this.faqs, ...faqs.rows];
        } else {
          this.faqs = faqs.rows;
          this.pagination.start = faqs.start;
        }

        this.updateTagsCache();

        this.loading.list = false;
      });
  }

  closeSidePanel(): void {
    this.isSidePanelOpen.settings = false;
    this.isSidePanelOpen.edit = false;
    this.faqEdit = undefined;
  }

  addOrEditFaq(faq?: FaqDefinitionExtended): void {
    if (this.faqSettingsComponent) {
      this.faqSettingsComponent
        .close()
        .pipe(take(1))
        .subscribe((res) => {
          if (res != 'cancel') {
            setTimeout(() => {
              this.addOrEditFaq(faq);
            }, 200);
          }
        });
    } else if (this.faqEditComponent) {
      this.faqEditComponent
        .close()
        .pipe(take(1))
        .subscribe((res) => {
          if (res != 'cancel') {
            if (faq) this.editFaq(faq);
            else this.addFaq();
          }
        });
    } else {
      if (faq) this.editFaq(faq);
      else this.addFaq();
    }
  }

  addFaq(initQuestion?: string, initAnswer?: string) {
    this.faqEdit = {
      id: undefined,
      intentId: undefined,
      title: initQuestion || '',
      description: '',
      utterances: [],
      tags: [],
      answer: initAnswer || '',
      enabled: true,
      applicationName: this.stateService.currentApplication.name,
      language: this.stateService.currentLocale
    };

    if (initQuestion) {
      this.faqEdit._initQuestion = initQuestion;
    }

    this.isSidePanelOpen.edit = true;
  }

  editFaq(faq: FaqDefinitionExtended) {
    this.faqEdit = faq;
    this.isSidePanelOpen.edit = true;
  }

  deleteFaq(faq: FaqDefinitionExtended) {
    this.loading.delete = true;
    const faqId = faq.id;
    this.rest
      .delete(`/faq/${faqId}`)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.faqs = this.faqs.filter((f) => f.id != faqId);
          this.pagination.total--;
          if (this.pagination.end <= this.pagination.total) {
            this.search(this.pagination.end - 1, 1, true, true, true);
          } else {
            this.pagination.end--;
            if (this.pagination.start > 0 && this.pagination.start === this.pagination.total) {
              this.search(this.pagination.end - this.pagination.size);
            }
          }

          this.stateService.resetConfiguration();

          this.toastrService.success(`Faq successfully deleted`, 'Success', {
            duration: 5000,
            status: 'success'
          });

          this.loading.delete = false;
          this.closeSidePanel();
        },
        error: () => {
          this.loading.delete = false;
        }
      });
  }

  enableFaq(faq: FaqDefinitionExtended) {
    faq.enabled = !faq.enabled;
    this.saveFaq(faq);
  }

  saveFaq(faq: FaqDefinitionExtended) {
    let toastLabel = 'created';
    this.loading.edit = true;
    this.rest
      .post('/faq', faq)
      .pipe(take(1))
      .subscribe({
        next: () => {
          if (faq.id) {
            toastLabel = 'updated';
          }

          this.stateService.resetConfiguration();

          this.toastrService.success(`Faq successfully ${toastLabel}`, 'Success', {
            duration: 5000,
            status: 'success'
          });
          this.loading.edit = false;
        },
        error: () => {
          this.loading.edit = false;
        }
      });
  }

  openSettings(): void {
    if (this.faqEditComponent) {
      this.faqEditComponent
        .close()
        .pipe(take(1))
        .subscribe((res) => {
          if (res != 'cancel') {
            this.isSidePanelOpen.settings = true;
          }
        });
    } else {
      this.isSidePanelOpen.settings = true;
    }
  }

  download() {
    let query: PaginatedQuery = this.stateService.createPaginatedQuery(0, 9999);
    const request = this.toSearchQuery(query);

    this.rest
      .post('/faq/search', request)
      .pipe(takeUntil(this.destroy))
      .subscribe((faqsResult: PaginatedFaqResult) => {
        const jsonBlob = new Blob([JSON.stringify(faqsResult.rows)], {
          type: 'application/json'
        });

        const exportFileName = getExportFileName(
          this.stateService.currentApplication.namespace,
          this.stateService.currentApplication.name,
          'Faqs',
          'json'
        );

        saveAs(jsonBlob, exportFileName);

        this.toastrService.show(`Faqs dump provided`, 'Faqs dump', { duration: 3000, status: 'success' });
      });
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
