import { EventEmitter } from '@angular/core';
import { BehaviorSubject, Subject, of } from 'rxjs';
import { Application } from '../../model/application';
import { EntityDefinition, EntityType, Intent, IntentsCategory } from '../../model/nlp';
import { User, UserRole } from '../../model/auth';

/**
 * Reusable test double for StateService.
 *
 * Covers the members most commonly read by components during construction /
 * ngOnInit (streams + currentApplication/currentLocale + a few helper methods),
 * with neutral values so that `should create` passes without extra wiring.
 *
 * Usage:
 *   providers: [{ provide: StateService, useClass: StateServiceMock }]
 *
 * Each spec gets a fresh instance (Angular instantiates useClass per injector),
 * so the Subjects never leak state between tests. Override individual members
 * in a spec when a behavioural test needs a specific value:
 *   const state = TestBed.inject(StateService) as unknown as StateServiceMock;
 *   state.currentApplication = { ... } as Application;
 */
export class StateServiceMock {
  // --- streams (real Subjects so .subscribe / .pipe(...) work) ---
  readonly entityTypes = new BehaviorSubject<EntityType[]>([]);
  readonly entities = new BehaviorSubject<EntityDefinition[]>([]);
  readonly currentIntents = new BehaviorSubject<Intent[]>([]);
  readonly currentIntentsCategories = new BehaviorSubject<IntentsCategory[]>([]);
  readonly currentNamespaceIntentsCategories = new BehaviorSubject<IntentsCategory[]>([]);
  readonly configurationChange = new Subject<boolean>();

  readonly currentApplicationEmitter = new EventEmitter<Application>();
  readonly resetConfigurationEmitter = new EventEmitter<boolean>();

  // --- scalar state ---
  user: User = { roles: [] } as unknown as User;
  namespaces = [];
  applications: Application[] = [];
  locales = [];
  currentApplication: Application = {
    name: 'app',
    namespace: 'namespace',
    supportedLocales: ['fr'],
    intents: [],
    namespaceIntents: []
  } as unknown as Application;
  currentLocale = 'fr';

  // --- helper methods (neutral defaults) ---
  hasRole(_role: UserRole): boolean {
    return true;
  }

  findIntentById(_id: string): Intent {
    return null;
  }

  findIntentByName(_name: string): Intent {
    return null;
  }

  intentLabelByName(name: string): string {
    return name;
  }

  localeName(code: string): string {
    return code;
  }

  otherThanCurrentLocales(): string[] {
    return [];
  }

  createApplicationScopedQuery() {
    return {};
  }

  createPaginatedQuery(_start?: number, _size?: number, _searchMark?: unknown) {
    return {};
  }

  createUpdateEntityDefinitionQuery(_entity?: unknown) {
    return {};
  }

  createPredefinedValueQuery(_entityTypeName?: string, _predefinedValue?: string, _old?: string) {
    return {};
  }

  entityTypesSortedByName = () => of([]);
}
