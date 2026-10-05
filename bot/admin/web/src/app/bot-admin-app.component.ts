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
import { NbMenuItem, NbMenuService, NbSidebarService, NbThemeService, NbToastrService } from '@nebular/theme';
import { AuthListener } from './core-nlp/auth/auth.listener';

import { AuthService } from './core-nlp/auth/auth.service';
import { RestService } from './core-nlp/rest/rest.service';
import { StateService } from './core-nlp/state.service';
import { User, UserRole } from './model/auth';
import { NavigationEnd, Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { Subject, filter, take, takeUntil } from 'rxjs';

@Component({
  selector: 'tock-bot-admin-root',
  templateUrl: './bot-admin-app.component.html',
  styleUrls: ['./bot-admin-app.component.css']
})
export class BotAdminAppComponent implements AuthListener, OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private currentUser: User | null = null; // ← stocker l'user pour rebuild

  UserRole = UserRole;

  private errorUnsuscriber: any;
  public menu: NbMenuItem[] = [];
  private currentBreakPointName: string;

  constructor(
    public auth: AuthService,
    public state: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private menuService: NbMenuService,
    private themeService: NbThemeService,
    private sidebarService: NbSidebarService,
    private router: Router,
    public transloco: TranslocoService
  ) {
    this.auth.addListener(this);
  }

  ngOnInit(): void {
    this.errorUnsuscriber = this.rest.errorEmitter.subscribe((e) =>
      this.toastrService.show(e, 'Error', { duration: 5000, status: 'danger' })
    );

    // expand the current route sub-menu on boostrap and when a route change has been initiated other than via the main menu
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        setTimeout(() => {
          this.collapseUnselectedMenuItems();
          this.expandSelectedMenuItem();
        }, 500);
      }
    });

    // automatic reduction of all other submenus when a menu entry is deployed (autoCollapse)
    this.menuService.onSubmenuToggle().subscribe((toggledBag) => {
      this.menu.forEach((entry) => {
        if (entry !== toggledBag.item) entry.expanded = false;
      });
    });

    // watch the current breakPoint
    this.themeService.onMediaQueryChange().subscribe((change) => {
      this.currentBreakPointName = change[1].name;
    });

    // handle menu clicks
    this.menuService.onItemClick().subscribe((menuItem) => {
      // collapse sidebar on link click if current breakpoint is xs or sm
      // Must remain correlated to menu-sidebar compactedBreakpoints
      if (['xs', 'sm'].includes(this.currentBreakPointName)) {
        this.sidebarService.compact('menu-sidebar');
      }

      // collapse all submenus when a first level link is clicked (ie: a top level link, not one inside a submenu)
      if (this.menu.find((item) => item === menuItem.item)) {
        this.menu.forEach((entry) => {
          if (entry !== menuItem.item) {
            entry.expanded = false;
          }
        });
      }
    });

    const savedLang = localStorage.getItem('preferred-lang');
    if (savedLang) {
      this.transloco.setActiveLang(savedLang);
    }
  }

  expandSelectedMenuItem(): void {
    this.menu.forEach((entry) => {
      entry.children?.forEach((child) => {
        if (child.selected) {
          entry.expanded = true;
        }
      });
    });
  }

  collapseUnselectedMenuItems(): void {
    this.menu.forEach((entry) => {
      let hasSelectedItem = false;
      entry.children?.forEach((child) => {
        if (child.selected) {
          hasSelectedItem = true;
        }
      });

      if (!hasSelectedItem && entry.expanded) {
        entry.expanded = false;
      }
    });
  }

  login(user: User): void {
    this.currentUser = user;
    this.transloco
      .selectTranslation()
      .pipe(
        filter((translation) => Object.keys(translation).length > 0),
        take(1) // ← once for init
      )
      .subscribe(() => {
        this.menu = this.createMenu();
        this.buildMenu(); // ← start language change watch after init
      });
  }

  private buildMenu(): void {
    if (!this.currentUser) return;

    this.transloco
      .selectTranslation()
      .pipe(
        filter((translation) => Object.keys(translation).length > 0), // ← wait for a non empty JSON
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.refreshMenuTitles(this.menu, this.createMenu());
      });
  }

  private refreshMenuTitles(current: NbMenuItem[], updated: NbMenuItem[]): void {
    current.forEach((item, index) => {
      if (updated[index]) {
        item.title = updated[index].title;
        if (item.children?.length && updated[index].children?.length) {
          this.refreshMenuTitles(item.children, updated[index].children);
        }
      }
    });
  }

  createMenu(): NbMenuItem[] {
    const t = (key: string) => this.transloco.translate(key);

    return [
      {
        title: t('menu.language-understanding'),
        icon: 'chat-dots',
        hidden: !this.state.hasRole(UserRole.nlpUser),
        children: [
          {
            title: t('menu-items.new-sentence'),
            link: '/language-understanding/try',
            icon: 'plus-circle'
          },
          {
            title: t('menu-items.inbox-sentences'),
            link: '/language-understanding/inbox',
            icon: 'inboxes'
          },
          {
            title: t('menu-items.search-sentences'),
            link: '/language-understanding/search',
            icon: 'search'
          },
          {
            title: t('menu-items.unknown-sentences'),
            link: '/language-understanding/unknown',
            icon: 'question-circle'
          },
          {
            title: t('menu-items.intents'),
            link: '/language-understanding/intents',
            icon: 'compass'
          },
          {
            title: t('menu-items.entities'),
            link: '/language-understanding/entities',
            icon: 'paperclip'
          },
          {
            title: t('menu-items.sentences-logs'),
            link: '/language-understanding/logs',
            icon: 'justify-left'
          }
        ]
      },
      {
        title: t('menu.stories-answers'),
        icon: 'book',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/build/story-create',
            title: t('menu-items.new-story'),
            icon: 'plus-circle'
          },
          {
            link: '/build/story-search',
            title: t('menu-items.all-stories'),
            icon: 'chat-left'
          },
          {
            link: '/faq/management',
            title: t('menu-items.faq-stories'),
            icon: 'chat-left-text'
          },
          {
            link: '/build/i18n',
            title: t('menu-items.answers'),
            icon: 'chat-right-quote'
          },
          {
            link: '/build/story-documents',
            title: t('menu-items.documents'),
            icon: 'images'
          },
          {
            link: '/build/story-rules',
            title: t('menu-items.rules'),
            icon: 'toggle-on'
          }
        ]
      },
      {
        title: t('menu.gen-ai'),
        icon: 'cpu',
        children: [
          {
            link: '/rag/settings',
            title: t('menu-items.rag-settings'),
            icon: 'lightbulb',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            link: '/rag/prompt-context',
            title: 'Rag prompt context',
            icon: 'journal-text'
          },
          {
            link: '/rag/exclusions',
            title: t('menu-items.rag-exclusions'),
            icon: 'lightbulb-off'
          },
          {
            title: t('menu-items.compressor-settings'),
            link: '/configuration/compressor-settings',
            icon: 'trophy',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            title: t('menu-items.sentence-generation-settings'),
            link: '/configuration/sentence-generation-settings',
            icon: 'list-columns',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            title: t('menu-items.observability-settings'),
            link: '/configuration/observability-settings',
            icon: 'display',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            link: '/configuration/vector-db-settings',
            title: t('menu-items.vector-db-settings'),
            icon: 'database',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            link: '/playground',
            title: t('menu-items.playground'),
            icon: 'joystick',
            hidden: !this.state.hasRole(UserRole.admin)
          }
        ]
      },
      {
        title: t('menu.test'),
        icon: 'play-circle',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/test/test',
            title: t('menu-items.test'),
            icon: 'terminal'
          },
          {
            link: '/test/plan',
            title: t('menu-items.test-plans'),
            icon: 'map'
          }
        ]
      },
      {
        title: t('menu.analytics'),
        icon: 'activity',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/analytics/activity',
            title: t('menu-items.activity'),
            icon: 'graph-up-arrow'
          },
          {
            link: '/analytics/behavior',
            title: t('menu-items.behavior'),
            icon: 'pie-chart'
          },
          {
            link: '/analytics/flow',
            title: t('menu-items.flow'),
            icon: 'funnel'
          },
          {
            link: '/analytics/users',
            title: t('menu-items.users'),
            icon: 'people'
          },
          {
            link: '/analytics/dialogs',
            title: t('menu-items.dialogs'),
            icon: 'wechat'
          },
          {
            link: '/analytics/preferences',
            title: t('menu-items.preferences'),
            icon: 'sliders'
          },
          {
            link: '/analytics/satisfaction',
            title: t('menu-items.satisfaction'),
            icon: 'star'
          }
        ]
      },
      {
        title: t('menu.custom-metrics'),
        icon: 'clipboard-data',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/business-metrics/board',
            title: t('menu-items.metrics'),
            icon: 'clipboard2-pulse'
          },
          {
            link: '/business-metrics/indicators',
            title: t('menu-items.indicators'),
            icon: 'sign-merge-left'
          }
        ]
      },
      {
        title: t('menu.answers-quality'),
        icon: 'award',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/quality/samples',
            title: t('menu-items.evaluations'),
            icon: 'eyedropper'
          },
          {
            link: '/quality/datasets',
            title: t('menu-items.datasets'),
            icon: 'palette2'
          }
        ]
      },
      {
        title: t('menu.model-quality'),
        icon: 'box-seam',
        hidden: !this.state.hasRole(UserRole.nlpUser),
        children: [
          {
            link: '/model-quality/log-stats',
            title: t('menu-items.model-stats'),
            icon: 'award'
          },
          {
            link: '/model-quality/intent-quality',
            title: t('menu-items.intent-distance'),
            icon: 'rulers'
          },
          {
            link: '/model-quality/count-stats',
            title: t('menu-items.count-stats'),
            icon: 'hash'
          },
          {
            link: '/model-quality/model-builds',
            title: t('menu-items.model-builds'),
            icon: 'building'
          },
          {
            link: '/model-quality/test-builds',
            title: t('menu-items.test-trends'),
            icon: 'graph-down-arrow'
          },
          {
            link: '/model-quality/test-intent-errors',
            title: t('menu-items.test-intent-errors'),
            icon: 'bug'
          },
          {
            link: '/model-quality/test-entity-errors',
            title: t('menu-items.test-entity-errors'),
            icon: 'bug'
          }
        ]
      },
      {
        title: t('menu.settings'),
        icon: 'gear',
        children: [
          {
            title: t('menu-items.applications'),
            link: '/configuration/nlp',
            icon: 'window-stack'
          },
          {
            title: t('menu-items.configurations'),
            link: '/configuration/bot',
            icon: 'link-45deg',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            title: t('menu-items.namespaces'),
            link: '/configuration/namespaces',
            icon: 'folder'
          },
          {
            title: t('menu-items.log'),
            link: '/configuration/users/logs',
            icon: 'eye',
            hidden: !this.state.hasRole(UserRole.technicalAdmin)
          },
          {
            title: t('menu-items.synchronization'),
            link: '/configuration/synchronization',
            icon: 'arrow-repeat'
          }
        ]
      }
    ];
  }

  logout(): void {}

  ngOnDestroy(): void {
    this.errorUnsuscriber.unsubscribe();
    this.destroy$.next(); // ← cleanup
    this.destroy$.complete();
  }
}
