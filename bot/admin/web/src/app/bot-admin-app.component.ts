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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { NbMenuItem, NbMenuService, NbSidebarService, NbThemeService, NbToastrService } from '@nebular/theme';
import { AuthListener } from './core-nlp/auth/auth.listener';

import { AuthService } from './core-nlp/auth/auth.service';
import { RestService } from './core-nlp/rest/rest.service';
import { StateService } from './core-nlp/state.service';
import { User, UserRole } from './model/auth';
import { NavigationEnd, Router } from '@angular/router';

@Component({
  selector: 'tock-bot-admin-root',
  templateUrl: './bot-admin-app.component.html',
  styleUrls: ['./bot-admin-app.component.css']
})
export class BotAdminAppComponent implements AuthListener, OnInit, OnDestroy {
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
    private router: Router
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
    this.menu = [
      {
        title: 'Language Understanding',
        icon: 'chat-dots',
        hidden: !this.state.hasRole(UserRole.nlpUser),
        children: [
          {
            link: '/language-understanding/try',
            title: 'New sentence',
            icon: 'plus-circle'
          },
          {
            link: '/language-understanding/inbox',
            title: 'Inbox sentences',
            icon: 'inboxes'
          },
          {
            link: '/language-understanding/search',
            title: 'Search sentences',
            icon: 'search'
          },
          {
            link: '/language-understanding/unknown',
            title: 'Unknown sentences',
            icon: 'question-circle'
          },
          {
            link: '/language-understanding/intents',
            title: 'Intents',
            icon: 'compass'
          },
          {
            link: '/language-understanding/entities',
            title: 'Entities',
            icon: 'paperclip'
          },
          {
            link: '/language-understanding/logs',
            title: 'Sentences logs',
            icon: 'justify-left'
          }
        ]
      },
      {
        title: 'Stories & Answers',
        icon: 'book',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/build/story-create',
            title: 'New Story',
            icon: 'plus-circle'
          },
          {
            link: '/build/story-search',
            title: 'All stories',
            icon: 'chat-left'
          },
          {
            link: '/faq/management',
            title: 'FAQs stories',
            icon: 'chat-left-text'
          },

          {
            link: '/build/i18n',
            title: 'Answers',
            icon: 'chat-right-quote'
          },
          {
            link: '/build/story-documents',
            title: 'Documents',
            icon: 'images'
          },

          {
            link: '/build/story-rules',
            title: 'Rules',
            icon: 'toggle-on'
          }
        ]
      },

      {
        title: 'Gen AI',
        icon: 'cpu',
        children: [
          // {
          //   link: '/rag/sources',
          //   title: 'Rag sources',
          //   icon: 'cloud-download-outline',
          //   hidden: !this.state.hasRole(UserRole.admin)
          // },

          {
            link: '/rag/settings',
            title: 'Rag settings',
            icon: 'lightbulb',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            link: '/rag/exclusions',
            title: 'Sentences Rag exclusions',
            icon: 'lightbulb-off'
          },
          {
            title: 'Compressor settings',
            link: '/configuration/compressor-settings',
            icon: 'trophy',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            title: 'Sentence generation settings',
            link: '/configuration/sentence-generation-settings',
            icon: 'list-columns',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            title: 'Observability settings',
            link: '/configuration/observability-settings',
            icon: 'display',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            link: '/configuration/vector-db-settings',
            title: 'Vector DB settings',
            icon: 'database',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            link: '/playground',
            title: 'Playground',
            icon: 'joystick',
            hidden: !this.state.hasRole(UserRole.admin)
          }
        ]
      },

      {
        title: 'Test',
        icon: 'play-circle',
        hidden: !this.state.hasRole(UserRole.botUser),

        children: [
          {
            link: '/test/test',
            title: 'Test',
            icon: 'terminal'
          },
          {
            link: '/test/plan',
            title: 'Test plans',
            icon: 'map'
          }
        ]
      },
      {
        title: 'Analytics',
        icon: 'activity',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/analytics/activity',
            title: 'Activity',
            icon: 'graph-up-arrow'
          },
          {
            link: '/analytics/behavior',
            title: 'Behavior',
            icon: 'pie-chart'
          },
          {
            link: '/analytics/flow',
            title: 'Flow',
            icon: 'funnel'
          },
          {
            link: '/analytics/users',
            title: 'Users',
            icon: 'people'
          },
          {
            link: '/analytics/dialogs',
            title: 'Dialogs',
            icon: 'wechat'
          },
          {
            link: '/analytics/preferences',
            title: 'Preferences',
            icon: 'sliders'
          },
          {
            link: '/analytics/satisfaction',
            title: 'Satisfaction',
            icon: 'star'
          }
        ]
      },
      {
        title: 'Custom Metrics',
        icon: 'clipboard-data',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/business-metrics/board',
            title: 'Metrics',
            icon: 'clipboard2-pulse'
          },
          {
            link: '/business-metrics/indicators',
            title: 'Indicators',
            icon: 'sign-merge-left'
          }
        ]
      },
      {
        title: 'Model Quality',
        icon: 'box-seam',
        hidden: !this.state.hasRole(UserRole.nlpUser),
        children: [
          {
            link: '/model-quality/log-stats',
            title: 'Model Stats',
            icon: 'award'
          },
          {
            link: '/model-quality/intent-quality',
            title: 'Intent Distance',
            icon: 'rulers'
          },
          {
            link: '/model-quality/count-stats',
            title: 'Count Stats',
            icon: 'hash'
          },
          {
            link: '/model-quality/model-builds',
            title: 'Model Builds',
            icon: 'building'
          },
          {
            link: '/model-quality/test-builds',
            title: 'Test Trends',
            icon: 'graph-down-arrow'
          },
          {
            link: '/model-quality/test-intent-errors',
            title: 'Test Intent Errors',
            icon: 'bug'
          },
          {
            link: '/model-quality/test-entity-errors',
            title: 'Test Entity Errors',
            icon: 'bug'
          }
        ]
      },
      {
        title: 'Settings',
        icon: 'gear',
        children: [
          {
            title: 'Applications',
            link: '/configuration/nlp',
            icon: 'window-stack'
          },
          {
            title: 'Configurations',
            link: '/configuration/bot',
            icon: 'link-45deg',
            hidden: !this.state.hasRole(UserRole.admin)
          },

          {
            title: 'Namespaces',
            link: '/configuration/namespaces',
            icon: 'folder'
          },
          {
            title: 'Log',
            link: '/configuration/users/logs',
            icon: 'eye',
            hidden: !this.state.hasRole(UserRole.technicalAdmin)
          },
          {
            title: 'Synchronization',
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
  }
}
