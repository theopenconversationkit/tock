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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, combineLatest, skip, takeUntil } from 'rxjs';

import { BotConfigurationService } from '../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../core/model/configuration';
import { StateService } from '../core-nlp/state.service';
import { DialogService } from '../core-nlp/dialog.service';
import { UserRole } from '../model/auth';
import { EvaluationSampleDefinition, EvaluationSampleStatus } from '../quality/samples/models';
import { BotIdentityEditComponent } from './modals/bot-identity-edit/bot-identity-edit.component';
import { ContactEditComponent } from './modals/contact-edit/contact-edit.component';
import { IngestionNotesComponent } from './modals/ingestion-notes/ingestion-notes.component';
import {
  BotContact,
  BotHistoryEvent,
  BotIdentity,
  DASHBOARD_PERIODS,
  DashboardAnswerOutcome,
  DashboardPeriod,
  DashboardTopic,
  DashboardUsage,
  GenAiConfiguration,
  IngestionNotes,
  KnowledgeIndex,
  WidgetState
} from './models/dashboard.model';
import { DashboardService } from './services/dashboard.service';
import { DashboardStateService } from './services/dashboard-state.service';

/** How many validated evaluations the widget lists, most recent first. */
const EVALUATIONS_DISPLAYED = 3;

@Component({
  selector: 'tock-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  standalone: false
})
export class DashboardComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();

  UserRole = UserRole;
  WidgetState = WidgetState;

  readonly periods = DASHBOARD_PERIODS;
  period: DashboardPeriod = 30;
  displayTests: boolean = false;

  configurations: BotApplicationConfiguration[] = [];

  usage: DashboardUsage;
  usageState: WidgetState = WidgetState.loading;

  outcome: DashboardAnswerOutcome;
  outcomeState: WidgetState = WidgetState.loading;

  topics: DashboardTopic[] = [];
  topicsState: WidgetState = WidgetState.loading;

  knowledgeIndex: KnowledgeIndex;
  knowledgeIndexState: WidgetState = WidgetState.loading;

  ingestionNotes: IngestionNotes;

  contacts: BotContact[] = [];
  contactsState: WidgetState = WidgetState.loading;

  identity: BotIdentity;
  identityState: WidgetState = WidgetState.loading;

  history: BotHistoryEvent[] = [];
  historyState: WidgetState = WidgetState.loading;

  evaluations: EvaluationSampleDefinition[] = [];
  evaluationState: WidgetState = WidgetState.loading;

  genAiConfiguration: GenAiConfiguration;
  genAiConfigurationState: WidgetState = WidgetState.loading;

  constructor(
    public state: StateService,
    private botConfiguration: BotConfigurationService,
    private dashboardService: DashboardService,
    private dashboardState: DashboardStateService,
    private dialog: DialogService
  ) {}

  ngOnInit(): void {
    // The state service is provided by the module, so it outlives this component:
    // read it synchronously first, otherwise a return to the route would query with
    // the field defaults rather than with what the header actually displays.
    this.period = this.dashboardState.period;
    this.displayTests = this.dashboardState.displayTests;

    // configurations is a BehaviorSubject and replays its last value on subscription,
    // which is what reloads the widgets both on a bot change and on re-entering the route.
    //
    // Note that it holds connector configurations, not bots: a perfectly valid bot may
    // have none. The dashboard therefore keys off the current application and uses this
    // stream only as the change signal.
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((configurations) => {
      this.configurations = configurations;

      if (!this.state.currentApplication) {
        return;
      }

      const botKey = `${this.state.currentApplication.namespace}:${this.state.currentApplication.name}`;
      this.dashboardState.applyBotContext(botKey);
      this.loadAll();
    });

    // skip(1) drops the replayed initial values, already covered by the load above.
    combineLatest([this.dashboardState.period$, this.dashboardState.displayTests$])
      .pipe(skip(1), takeUntil(this.destroy$))
      .subscribe(([period, displayTests]) => {
        this.period = period;
        this.displayTests = displayTests;
        this.loadPeriodDependentWidgets();
      });
  }

  /** Connector configurations are optional; only the application is required. */
  get hasApplication(): boolean {
    return !!this.state.currentApplication;
  }

  /** No connector declared: usage figures will stay at zero, the rest still applies. */
  get hasNoConnector(): boolean {
    return this.hasApplication && !this.configurations.length;
  }

  get namespace(): string {
    return this.state.currentApplication.namespace;
  }

  /** On the backend side, the application name is the botId. */
  get applicationName(): string {
    return this.state.currentApplication.name;
  }

  setPeriod(period: DashboardPeriod): void {
    if (period === this.period) return;
    this.dashboardState.setPeriod(period);
  }

  /**
   * Test exchanges are those initiated from the studio rather than from an external
   * connector. On development and staging bots they are often the only traffic there is.
   */
  toggleDisplayTests(): void {
    this.dashboardState.toggleDisplayTests();
  }

  private loadAll(): void {
    this.loadPeriodDependentWidgets();
    this.loadKnowledgeIndex();
    this.loadContacts();
    this.loadIdentity();
    this.loadHistory();
    this.loadEvaluation();
    this.loadGenAiConfiguration();
  }

  private loadPeriodDependentWidgets(): void {
    this.loadUsage();
    this.loadOutcome();
    this.loadTopics();
  }

  private loadUsage(): void {
    this.usageState = WidgetState.loading;
    this.dashboardService
      .getUsage(this.namespace, this.applicationName, this.period, this.displayTests)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (usage) => {
          this.usage = usage;
          this.usageState = usage.total ? WidgetState.ready : WidgetState.empty;
        },
        error: () => (this.usageState = WidgetState.error)
      });
  }

  private loadOutcome(): void {
    this.outcomeState = WidgetState.loading;
    this.dashboardService
      .getAnswerOutcome(this.namespace, this.applicationName, this.period, this.displayTests)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (outcome) => {
          this.outcome = outcome;
          const total = Object.values(outcome.counts ?? {}).reduce((sum, count) => sum + count, 0);
          this.outcomeState = total ? WidgetState.ready : WidgetState.empty;
        },
        error: () => (this.outcomeState = WidgetState.error)
      });
  }

  private loadTopics(): void {
    this.topicsState = WidgetState.loading;
    this.dashboardService
      .getTopics(this.namespace, this.applicationName, this.period, this.displayTests)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (topics) => {
          this.topics = topics;
          this.topicsState = topics.length ? WidgetState.ready : WidgetState.empty;
        },
        error: () => (this.topicsState = WidgetState.error)
      });
  }

  private loadKnowledgeIndex(): void {
    if (!this.state.hasRole(UserRole.admin)) return;

    this.knowledgeIndexState = WidgetState.loading;
    this.dashboardService
      .getKnowledgeIndex(this.namespace, this.applicationName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (index) => {
          this.knowledgeIndex = index;
          this.knowledgeIndexState = index?.indexSessionId ? WidgetState.ready : WidgetState.unavailable;
          if (index?.indexSessionId) {
            this.loadIngestionNotes(index.indexSessionId);
          }
        },
        error: () => (this.knowledgeIndexState = WidgetState.error)
      });
  }

  private loadIngestionNotes(indexSessionId: string): void {
    this.dashboardService
      .getIngestionNotes(this.namespace, this.applicationName, indexSessionId)
      .pipe(takeUntil(this.destroy$))
      .subscribe((notes) => (this.ingestionNotes = notes));
  }

  private loadContacts(): void {
    this.contactsState = WidgetState.loading;
    this.dashboardService
      .getContacts(this.namespace, this.applicationName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (contacts) => {
          this.contacts = contacts;
          this.contactsState = contacts.length ? WidgetState.ready : WidgetState.empty;
        },
        error: () => (this.contactsState = WidgetState.error)
      });
  }

  private loadIdentity(): void {
    this.identityState = WidgetState.loading;
    this.dashboardService
      .getBotIdentity(this.namespace, this.applicationName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (identity) => {
          this.identity = identity;
          this.identityState = WidgetState.ready;
        },
        error: () => (this.identityState = WidgetState.error)
      });
  }

  private loadHistory(): void {
    this.historyState = WidgetState.loading;
    this.dashboardService
      .getBotHistory(this.namespace, this.applicationName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (history) => {
          this.history = history;
          this.historyState = history.length ? WidgetState.ready : WidgetState.empty;
        },
        error: () => (this.historyState = WidgetState.error)
      });
  }

  editIdentity(): void {
    const modal = this.dialog.openDialog(BotIdentityEditComponent, {
      context: { identity: this.identity, technicalName: this.applicationName }
    });

    modal.componentRef.instance.onSave.pipe(takeUntil(this.destroy$)).subscribe((identity: BotIdentity) => {
      this.dashboardService
        .saveBotIdentity(this.namespace, this.applicationName, identity)
        .pipe(takeUntil(this.destroy$))
        .subscribe((saved) => (this.identity = saved));
    });
  }

  private loadEvaluation(): void {
    this.evaluationState = WidgetState.loading;
    this.dashboardService
      .getEvaluationSamples(this.namespace, this.applicationName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (samples) => {
          // Only validated samples are reported. In-progress and abandoned ones are ignored.
          this.evaluations = (samples ?? [])
            .filter((sample) => sample.status === EvaluationSampleStatus.VALIDATED && !!sample.statusChangeDate)
            .sort((a, b) => new Date(b.statusChangeDate).getTime() - new Date(a.statusChangeDate).getTime())
            .slice(0, EVALUATIONS_DISPLAYED);

          this.evaluationState = this.evaluations.length ? WidgetState.ready : WidgetState.empty;
        },
        error: () => (this.evaluationState = WidgetState.error)
      });
  }

  loadGenAiConfiguration(): void {
    if (!this.state.hasRole(UserRole.admin)) return;

    this.genAiConfigurationState = WidgetState.loading;
    this.dashboardService
      .getGenAiConfiguration(this.namespace, this.applicationName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (configuration) => {
          this.genAiConfiguration = configuration;
          this.genAiConfigurationState = configuration?.checks?.length ? WidgetState.ready : WidgetState.unavailable;
        },
        error: () => (this.genAiConfigurationState = WidgetState.error)
      });
  }

  addContact(): void {
    const modal = this.dialog.openDialog(ContactEditComponent, {});
    modal.componentRef.instance.onSave.pipe(takeUntil(this.destroy$)).subscribe((contact: BotContact) => {
      this.persistContacts([...this.contacts, contact]);
    });
  }

  editContact(contact: BotContact): void {
    const modal = this.dialog.openDialog(ContactEditComponent, { context: { contact } });

    modal.componentRef.instance.onSave.pipe(takeUntil(this.destroy$)).subscribe((updated: BotContact) => {
      this.persistContacts(this.contacts.map((c) => (c.id === updated.id ? updated : c)));
    });

    modal.componentRef.instance.onDelete.pipe(takeUntil(this.destroy$)).subscribe((removed: BotContact) => {
      this.persistContacts(this.contacts.filter((c) => c.id !== removed.id));
    });
  }

  private persistContacts(contacts: BotContact[]): void {
    this.dashboardService
      .saveContacts(this.namespace, this.applicationName, contacts)
      .pipe(takeUntil(this.destroy$))
      .subscribe((saved) => {
        this.contacts = saved;
        this.contactsState = saved.length ? WidgetState.ready : WidgetState.empty;
      });
  }

  editIngestionNotes(): void {
    const notes: IngestionNotes = this.ingestionNotes ?? {
      indexSessionId: this.knowledgeIndex.indexSessionId,
      text: '',
      updatedAt: null,
      updatedBy: null
    };

    const modal = this.dialog.openDialog(IngestionNotesComponent, { context: { notes } });

    modal.componentRef.instance.onSave.pipe(takeUntil(this.destroy$)).subscribe((updated: IngestionNotes) => {
      this.dashboardService
        .saveIngestionNotes(this.namespace, this.applicationName, updated)
        .pipe(takeUntil(this.destroy$))
        .subscribe((saved) => (this.ingestionNotes = saved));
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(null);
    this.destroy$.complete();
  }
}
