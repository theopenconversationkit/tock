import { Component, Inject, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { DialogReport } from '../../../shared/model/dialog-data';
import { ConnectorType } from '../../../core/model/configuration';
import { StateService } from '../../../core-nlp/state.service';
import { DialogReportQuery } from '../dialogs';
import { AnalyticsService } from '../../analytics.service';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { ActivatedRoute, UrlSegment } from '@angular/router';
import { BotSharedService } from '../../../shared/bot-shared.service';
import { BehaviorSubject, Subject, takeUntil } from 'rxjs';
import { saveAs } from 'file-saver-es';
import { getExportFileName } from '../../../shared/utils';
import { DOCUMENT, Location } from '@angular/common';
import { Pagination } from '../../../shared/components';
import { DialogListFilters } from './dialogs-list-filters/dialogs-list-filters.component';

export class DialogFilter {
  constructor(
    public exactMatch: boolean,
    public displayTests: boolean,
    public dialogId?: string,
    public text?: string,
    public intentName?: string,
    public connectorType?: ConnectorType,
    public ratings?: number[],
    public configuration?: string,
    public intentsToHide?: string[],
    public isGenAiRagDialog?: boolean
  ) {}
}

@Component({
  selector: 'tock-dialogs-list',
  templateUrl: './dialogs-list.component.html',
  styleUrls: ['./dialogs-list.component.scss']
})
export class DialogsListComponent implements OnInit, OnChanges, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  totalDialogsCount: BehaviorSubject<string> = new BehaviorSubject<string>('');

  loading: boolean = false;

  data: DialogReport[] = [];
  nlpStats: DialogReport[] = [];

  filters: Partial<DialogListFilters> = {
    exactMatch: false
  };

  dialogReportQuery: DialogReportQuery;

  dialogAnchorRef: string;

  pagination: Pagination = {
    start: 0,
    end: undefined,
    size: 10,
    total: undefined
  };

  @Input() ratingFilter: number[];

  constructor(
    public state: StateService,
    private analytics: AnalyticsService,
    private botConfiguration: BotConfigurationService,
    private route: ActivatedRoute,
    public botSharedService: BotSharedService,
    private location: Location,
    @Inject(DOCUMENT) private document: Document
  ) {
    this.dialogAnchorRef = (this.location.getState() as any)?.dialogId;
  }

  ngOnInit() {
    this.state.configurationChange.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.refresh();
    });
    this.refresh();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['ratingFilter'].currentValue != changes['ratingFilter'].previousValue) {
      this.refresh();
    }
  }

  onFilterDialogs(filters) {
    this.filters = {
      ...this.filters,
      ...filters
    };

    this.search();
  }

  paginationChange(pagination: Pagination): void {
    this.search(this.pagination.start, this.pagination.size, false, true, true);
  }

  refresh() {
    this.data = [];
    this.search();
  }

  onScroll() {
    if (this.loading || this.pagination.end >= this.pagination.total) return;
    this.search(this.pagination.end, this.pagination.size, true, false);
  }

  search(
    start: number = 0,
    size: number = this.pagination.size,
    add: boolean = false,
    showLoadingSpinner: boolean = true,
    scrollToTop: Boolean = false
  ) {
    if (this.loading) return;

    if (showLoadingSpinner) this.loading = true;

    const query = this.state.createPaginatedQuery(start, size);

    this.dialogReportQuery = new DialogReportQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      this.filters.exactMatch,
      null,
      this.filters.dialogId,
      this.filters.text,
      this.filters.intentName,
      this.filters.connectorType,
      this.filters.displayTests,
      this.ratingFilter,
      this.filters.configuration,
      this.filters.intentsToHide,
      this.filters.isGenAiRagDialog
    );

    this.analytics.dialogs(this.dialogReportQuery).subscribe((result) => {
      this.pagination.total = result.total;

      if (add) {
        this.data = [...this.data, ...result.rows];
      } else {
        this.data = result.rows;
        this.pagination.start = result.start;
      }

      this.pagination.end = result.end;

      this.totalDialogsCount.next(this.formattedTotal());
      this.loading = false;

      if (scrollToTop) {
        this.scrollToTop();
      }

      if (this.dialogAnchorRef) {
        setTimeout(() => {
          const target = document.querySelector(`#dialog-wrapper-${this.dialogAnchorRef}`);
          this.dialogAnchorRef = undefined;
          if (target) {
            target.scrollIntoView({ behavior: 'smooth', block: 'center' });
          }
        }, 300);
      }
    });
  }

  scrollToTop(): void {
    const currentScroll = this.document.documentElement.scrollTop || this.document.body.scrollTop;
    if (currentScroll > 0) {
      window.requestAnimationFrame(this.scrollToTop.bind(this));
      window.scrollTo(0, currentScroll - currentScroll / 4);
    }
  }

  formattedTotal() {
    return this.pagination.total !== 1000000 ? this.pagination.total.toString() : this.pagination.total + '+';
  }

  waitAndRefresh() {
    setTimeout((_) => this.refresh());
  }

  dataEquals(d1: DialogReport, d2: DialogReport): boolean {
    return d1.id === d2.id;
  }

  exportDialogs() {
    const exportFileName = getExportFileName(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      'dialogs_with_rating',
      'csv'
    );
    this.analytics.downloadDialogsCsv(this.dialogReportQuery).subscribe((blob) => {
      saveAs(blob, exportFileName);
    });

    const exportFileName2 = getExportFileName(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      'dialogs_with_rating_and_intents',
      'csv'
    );
    this.analytics.downloadDialogsWithIntentsCsv(this.dialogReportQuery).subscribe((blob) => {
      saveAs(blob, exportFileName2);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
