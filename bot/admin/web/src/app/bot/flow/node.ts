/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

import {DialogFlowStateData, DialogFlowStateTransitionData, DialogFlowStateTransitionType} from "../model/flow";

export class StoryNode {

  public dynamic: boolean;
  public count: number;

  constructor(
    public storyDefinitionId: string,
    public states: DialogFlowStateData[],
    public id: number,
    public entities: string[],
    public intent?: string,
    public step?: string
  ) {
    this.dynamic = storyDefinitionId.match(/^[0-9a-fA-F]{24}$/) != null;
    this.count = this.states.reduce((sum, s) => sum + s.count, 0);
  }

  displayCount(): string {
    if (this.count < 1000) {
      return this.count.toString();
    } else if (this.count < 100000) {
      return Math.floor(this.count / 1000) + "k";
    } if (this.count < 1000000) {
      return "."+ Math.floor(this.count / 100000) + "m";
    } else {
      return Math.floor(this.count / 1000000) + "m"
    }
  }

  displayName(): string {
    const sId = this.storyDefinitionId;
    const i = this.states[0].intent ? this.states[0].intent : this.intent;
    return sId === 'tock_unknown_story' ? 'unknown' :
      ((sId && i && sId.match(/^[0-9a-fA-F]{24}$/)) ? i : sId);
  }

  nodeName(): string {
    const nodeName = this.displayName();
    const i = this.intent;
    return nodeName
      + (!i || i === nodeName || this.storyDefinitionId === 'tock_unknown_story' ? "" : "/" + i)
      + (this.step ? "*" + this.step : "")
  }

}

export class NodeTransition {

  public count: number;

  constructor(
    public transitions: DialogFlowStateTransitionData[],
    public previousId: number,
    public nextId: number,
    public type: DialogFlowStateTransitionType
  ) {
    this.count = this.transitions.reduce((sum, s) => sum + s.count, 0);
  }
}
