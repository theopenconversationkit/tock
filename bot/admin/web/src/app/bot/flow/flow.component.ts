/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, OnInit} from "@angular/core";
import {BotService} from "../bot-service";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {StateService} from "../../core-nlp/state.service";
import {
  ApplicationDialogFlow,
  DialogFlowRequest,
  DialogFlowStateData,
  DialogFlowStateTransitionType
} from "../model/flow";
import {BotConfigurationService} from "../../core/bot-configuration.service";
import {entityColor} from "../../model/nlp";

@Component({
  selector: 'tock-flow',
  templateUrl: './flow.component.html',
  styleUrls: ['./flow.component.css']
})
export class FlowComponent implements OnInit {

  layouts = [
    {
      name: 'cola',
      nodeDimensionsIncludeLabels: true,
      animate: true,
      flow: {axis: 'x', minSeparation: 30}
    },
    {
      name: 'dagre',
      rankDir: 'LR',
      directed: true,
      nodeDimensionsIncludeLabels: true,
      animate: true
    },
    {
      name: 'cose',
      rankDir: 'LR',
      nodeDimensionsIncludeLabels: true,
      animate: true
    },
    {
      name: 'cose-bilkent',
      rankDir: 'LR',
      nodeDimensionsIncludeLabels: true,
      animate: true
    },
    {
      name: 'elk',
      nodeDimensionsIncludeLabels: true,
      elk: {
        direction: 'RIGHT',
        edgeRouting: 'SPLINES',
      }
    },
    {
      name: 'grid',
      nodeDimensionsIncludeLabels: true,
      directed: true,
      animate: true,
      spacingFactor: 0.5
    },
    {
      name: 'circle',
      nodeDimensionsIncludeLabels: true,
      directed: true,
      animate: true,
      spacingFactor: 0.5
    },
    {
      name: 'concentric',
      nodeDimensionsIncludeLabels: true,
      directed: true,
      animate: true
    },
    {
      name: 'breadthfirst',
      nodeDimensionsIncludeLabels: true,
      padding: 10,
      directed: true,
      animate: true,
      maximal: false,
      grid: true,
      spacingFactor: 0.5
    }
  ];
  layout = this.layouts[0];
  selectedLayout = this.layout.name;
  recursive: boolean = true;
  entity: boolean = true;
  step: boolean = true;
  intent: boolean = true;
  minimalNodeCount: number = 0;
  maxNodeCount: number = 1;
  minimalTransitionCount: number = 0;

  selectedNode: DialogFlowStateData;

  graphData;

  botConfigurationId: string;
  flow: ApplicationDialogFlow;

  constructor(private nlp: NlpService,
              private state: StateService,
              private bot: BotService,
              private botConfiguration: BotConfigurationService) {
  }

  ngOnInit(): void {
  }

  changeLayout(layout: string) {
    this.layout = this.layouts.find(l => l.name === layout);
  }

  update() {
    setTimeout(_ => this.toGraphData(this.flow));
  }

  displayFlow() {
    this.botConfiguration.configurations.subscribe(c => {
      const conf = c.find(c => c._id === this.botConfigurationId);
      this.bot.getApplicationFlow(
        new DialogFlowRequest(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          conf.botId
        )
      ).subscribe(f => this.toGraphData(f));
    });
  }

  private toGraphData(flow: ApplicationDialogFlow) {
    this.flow = flow;
    const graph = {
      nodes: [],
      edges: []
    };
    graph.nodes.push({data: {id: 'null', name: 'Startup', weight: 1, colorCode: 'blue', shapeType: 'ellipse'}});
    const states = [];
    const statesByKey = [];
    const statesById = [];
    this.flow.states.forEach(s => {
      if (this.entity && this.step && this.intent) {
        statesById[s._id] = s._id;
        states.push(s);
      } else {
        let key = s.storyDefinitionId
          + (this.intent ? "+" + s.intent : "")
          + (this.step ? "+" + s.step : "")
          + (this.entity ? "+" + s.entities.join("%") : "");
        let state = statesByKey[key];
        if (!state) {
          state = Object.assign({}, s);
          if (!this.entity) {
            state.entities = [];
          }
          if (!this.intent) {
            state.intent = null;
          }
          if (!this.step) {
            state.step = null;
          }
          states.push(state);
          statesByKey[key] = state;
          statesById[s._id] = s._id;
        } else {
          state.count += s.count;
          statesById[s._id] = state._id;
        }
      }
    });
    let maxCount = 1;
    const realStates = [];
    states.forEach(s => {
      if (this.minimalNodeCount <= s.count) {
        realStates[s._id] = s._id;
        maxCount = Math.max(maxCount, s.count);
        graph.nodes.push({
          data: {
            id: s._id,
            name: s.storyDefinitionId === 'tock_unknown_story' ? 'unknown' : s.storyDefinitionId
              + (!s.intent || s.intent === s.storyDefinitionId ? "" : "/" + s.intent)
              + (s.step ? "*" + s.intent : ""),
            weight: s.count,
            colorCode: entityColor(s.storyDefinitionId ? s.storyDefinitionId : "start"),
            shapeType: 'roundrectangle'
          }
        })
      }
    });
    this.maxNodeCount = maxCount;

    let transitions = [];
    this.flow.transitions.forEach(t => {
      let prev = statesById[t.previousStateId];
      let next = statesById[t.nextStateId];
      if (this.recursive || prev !== next) {
        transitions[prev] = (transitions[prev] ? transitions[prev] : 0) + t.count;
      }
    });
    this.flow.transitions.forEach(t => {
      let prev = statesById[t.previousStateId];
      let next = statesById[t.nextStateId];
      let percentage = Math.round((t.count * 10000.0) / transitions[prev]) / 100;
      if ((this.recursive || prev !== next) && realStates[next] && (!t.previousStateId || realStates[prev])) {
        if (percentage >= this.minimalTransitionCount) {
          graph.edges.push({
            data: {
              source: prev ? prev : 'null',
              target: next,
              colorCode: entityColor(DialogFlowStateTransitionType[t.type] ? DialogFlowStateTransitionType[t.type] : DialogFlowStateTransitionType[DialogFlowStateTransitionType.nlp]),
              strength: t.count,
              label: percentage + '%',
              classes: 'autorotate'
            }
          });
        }
      }
    });
    this.graphData = graph;
  }

  nodeChange(id: string) {
    this.selectedNode = this.flow.states.find(s => s._id === id);
  }
}
