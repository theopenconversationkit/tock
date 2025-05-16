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

import { DialogFlowStateData, DialogFlowStateTransitionData, DialogFlowStateTransitionType } from './flow';
import { AnswerConfigurationType, StoryDefinitionConfiguration } from '../../bot/model/story';

export class StoryNode {
  public dynamic: boolean;

  constructor(
    public index: number,
    public _id: string,
    public storyDefinitionId: string,
    public states: DialogFlowStateData[],
    public entities: string[],
    public intent?: string,
    public step?: string,
    public storyType?: AnswerConfigurationType,
    public storyName?: string
  ) {
    this.dynamic = storyDefinitionId.match(/^[0-9a-fA-F]{24}$/) != null;
  }

  get count(): number {
    return this.states ? this.states.reduce((sum, s) => sum + s.count, 0) : 0;
  }

  displayCount(): string {
    let count = this.count;
    if (count < 1000) {
      return count.toString();
    } else if (count < 100000) {
      return Math.floor(count / 1000) + 'k';
    }
    if (count < 1000000) {
      return '.' + Math.floor(count / 100000) + 'm';
    } else {
      return Math.floor(count / 1000000) + 'm';
    }
  }

  displayName(): string {
    const sId = this.storyName;
    const i = this.states[0].intent ? this.states[0].intent : this.intent;
    return sId === 'tock_unknown_story' ? 'unknown' : sId && i && sId;
  }

  nodeName(): string {
    const nodeName = this.displayName();
    const i = this.intent;
    return (
      nodeName +
      (!i || i === nodeName || this.storyDefinitionId === 'tock_unknown_story' ? '' : '/' + i) +
      (this.step ? '*' + this.step : '')
    );
  }

  isConfiguredAnswer(): boolean {
    return this.isSimpleAnswer() || this.isMessageAnswer() || this.isScriptAnswer();
  }

  isSimpleAnswer(): boolean {
    return this.storyType === AnswerConfigurationType.simple;
  }

  isMessageAnswer(): boolean {
    return this.storyType === AnswerConfigurationType.message;
  }

  isScriptAnswer(): boolean {
    return this.storyType === AnswerConfigurationType.script;
  }

  isBuiltIn(): boolean {
    return this.storyType === AnswerConfigurationType.builtin;
  }
}

export class NodeTransition {
  constructor(
    public transitions: DialogFlowStateTransitionData[],
    public previousId: number,
    public nextId: number,
    public type: DialogFlowStateTransitionType
  ) {}

  get count(): number {
    return this.transitions ? this.transitions.reduce((sum, s) => sum + s.count, 0) : 0;
  }
}

export class NodeTypeFilter {
  constructor(
    public name: string,
    public description: string,
    public alwaysDisplay: boolean,
    public filter: (node: StoryNode | StoryDefinitionConfiguration) => boolean
  ) {}
}

export const NodeTypeFilters = [
  new NodeTypeFilter('All', 'All Types', true, (node) => true),
  new NodeTypeFilter('Configured', 'All Configured Types', true, (node) => node.isConfiguredAnswer()),
  new NodeTypeFilter('Simple', 'Only Simple Type', true, (node) => node.isSimpleAnswer()),
  //TODO uncomment this when message type available
  //new NodeTypeFilter('Message', 'Only Message Type', true, node => node.isMessageAnswer()),
  new NodeTypeFilter('Script', 'Only Script Type', true, (node) => node.isScriptAnswer()),
  new NodeTypeFilter('Built-in', 'Only Built-in', true, (node) => node.isBuiltIn()),
  new NodeTypeFilter('Unknown', 'Unknown Type', false, (node) => {
    if (node instanceof StoryNode) {
      return node.storyType == undefined;
    } else if (node instanceof StoryDefinitionConfiguration) {
      return node.currentType == undefined;
    }
  })
];
