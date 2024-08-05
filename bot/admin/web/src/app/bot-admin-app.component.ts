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
        icon: 'message-circle-outline',
        hidden: !this.state.hasRole(UserRole.nlpUser),
        children: [
          {
            link: '/language-understanding/try',
            title: 'New Sentence',
            icon: 'plus-circle-outline'
          },
          {
            link: '/language-understanding/inbox',
            title: 'Inbox',
            icon: 'inbox-outline'
          },
          {
            link: '/language-understanding/search',
            title: 'Search',
            icon: 'search-outline'
          },
          {
            link: '/language-understanding/unknown',
            title: 'Unknown',
            icon: 'question-mark-circle-outline'
          },
          {
            link: '/language-understanding/intents',
            title: 'Intents',
            icon: 'compass-outline'
          },
          {
            link: '/language-understanding/entities',
            title: 'Entities',
            icon: 'attach-outline'
          },
          {
            link: '/language-understanding/logs',
            title: 'Logs',
            icon: 'list-outline'
          }
        ]
      },
      {
        title: 'Stories & Answers',
        icon: 'book-open-outline',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/build/story-create',
            title: 'New Story',
            icon: 'plus-circle-outline'
          },
          {
            link: '/build/story-search',
            title: 'All stories',
            icon: 'layers-outline'
          },
          {
            link: '/faq/management',
            title: 'FAQs management',
            icon: 'message-square-outline'
          },
          {
            link: '/build/story-rules',
            title: 'Rules',
            icon: 'toggle-right-outline'
          },
          {
            link: '/build/i18n',
            title: 'Answers',
            icon: 'color-palette-outline'
          },
          {
            link: '/build/story-documents',
            title: 'Documents',
            icon: 'folder-outline'
          }
        ]
      },

      {
        title: 'Gen AI',
        icon: 'bulb-outline',
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
            icon: 'settings-outline',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            link: '/rag/exclusions',
            title: 'Rag exclusions',
            icon: { icon: 'ragexclude', pack: 'tock-custom' }
          },
          {
            title: 'Sentence generation settings',
            link: '/configuration/sentence-generation-settings',
            icon: 'list-outline',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            title: 'Observability settings',
            link: '/configuration/observability-settings',
            icon: 'monitor-outline',
            hidden: !this.state.hasRole(UserRole.admin)
          }
        ]
      },

      {
        title: 'Test',
        icon: 'play-circle-outline',
        hidden: !this.state.hasRole(UserRole.botUser),

        children: [
          {
            link: '/test/test',
            title: 'Test',
            icon: 'smiling-face-outline'
          },
          {
            link: '/test/plan',
            title: 'Test plans',
            icon: 'map-outline'
          }
        ]
      },
      {
        title: 'Analytics',
        icon: 'trending-up-outline',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/analytics/activity',
            title: 'Activity',
            icon: 'activity-outline'
          },
          {
            link: '/analytics/behavior',
            title: 'Behavior',
            icon: 'pie-chart-outline'
          },
          {
            link: '/analytics/flow',
            title: 'Flow',
            icon: 'funnel-outline'
          },
          {
            link: '/analytics/users',
            title: 'Users',
            icon: 'people-outline'
          },
          {
            link: '/analytics/dialogs',
            title: 'Search',
            icon: 'search-outline'
          },
          {
            link: '/analytics/preferences',
            title: 'Preferences',
            icon: 'settings-2-outline'
          },
          {
            link: '/analytics/satisfaction',
            title: 'Satisfaction',
            icon: 'star-outline'
          }
        ]
      },
      {
        title: 'Custom Metrics',
        icon: 'pie-chart-outline',
        hidden: !this.state.hasRole(UserRole.botUser),
        children: [
          {
            link: '/business-metrics/board',
            title: 'Metrics',
            icon: 'pie-chart-outline'
          },
          {
            link: '/business-metrics/indicators',
            title: 'Indicators',
            icon: 'compass-outline'
          }
        ]
      },
      {
        title: 'Model Quality',
        icon: 'clipboard-outline',
        hidden: !this.state.hasRole(UserRole.nlpUser),
        children: [
          {
            link: '/model-quality/log-stats',
            title: 'Model Stats',
            icon: 'activity-outline'
          },
          {
            link: '/model-quality/intent-quality',
            title: 'Intent Distance',
            icon: 'pantone-outline'
          },
          {
            link: '/model-quality/count-stats',
            title: 'Count Stats',
            icon: 'hash-outline'
          },
          {
            link: '/model-quality/model-builds',
            title: 'Model Builds',
            icon: 'save-outline'
          },
          {
            link: '/model-quality/test-builds',
            title: 'Test Trends',
            icon: 'trending-down-outline'
          },
          {
            link: '/model-quality/test-intent-errors',
            title: 'Test Intent Errors',
            icon: 'alert-triangle-outline'
          },
          {
            link: '/model-quality/test-entity-errors',
            title: 'Test Entity Errors',
            icon: 'alert-triangle-outline'
          }
        ]
      },
      {
        title: 'Settings',
        icon: 'settings-outline',
        children: [
          {
            title: 'Applications',
            link: '/configuration/nlp',
            icon: 'browser-outline'
          },
          {
            title: 'Configurations',
            link: '/configuration/bot',
            icon: 'link-outline',
            hidden: !this.state.hasRole(UserRole.admin)
          },

          {
            title: 'Namespaces',
            link: '/configuration/namespaces',
            icon: 'folder-outline'
          },
          {
            title: 'Log',
            link: '/configuration/users/logs',
            icon: 'eye-outline',
            hidden: !this.state.hasRole(UserRole.technicalAdmin)
          },
          {
            title: 'Synchronization',
            link: '/configuration/synchronization',
            icon: 'sync'
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
