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

export class UserAnalyticsPreferences {
  lineConfig: {
    stacked: boolean;
    curvedLines: boolean;
    focusTarget: boolean;
  };

  graphs: {
    activity: {
      messagesByConnector: boolean;
      messagesByConfiguration: boolean;
      messagesByStory: boolean;
      messagesByIntent: boolean;
      messagesAll: boolean;
      messagesByDays: boolean;
      users: boolean;
    };
    behavior: {
      messagesByStory: boolean;
      messagesByIntent: boolean;
      messagesByDayOfWeek: boolean;
      messagesByHourOfDay: boolean;
      messagesByActionType: boolean;
      messagesByStoryCategory: boolean;
      messagesByStoryType: boolean;
      messagesByLocale: boolean;
    };
  };

  private constructor() {
    this.lineConfig = {
      stacked: false,
      curvedLines: true,
      focusTarget: true
    };

    this.graphs = {
      activity: {
        messagesByConnector: true,
        messagesByConfiguration: true,
        messagesByStory: true,
        messagesByIntent: true,
        messagesAll: true,
        messagesByDays: true,
        users: true
      },
      behavior: {
        messagesByStory: true,
        messagesByIntent: true,
        messagesByDayOfWeek: true,
        messagesByHourOfDay: true,
        messagesByActionType: true,
        messagesByStoryCategory: true,
        messagesByStoryType: true,
        messagesByLocale: true
      }
    };
  }

  static defaultConfiguration(): UserAnalyticsPreferences {
    return new UserAnalyticsPreferences();
  }
}

export enum ChartType {
  line = 'line',
  pie = 'line',
  all = 'all'
}
