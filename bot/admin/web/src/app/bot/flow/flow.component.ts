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
      animate: true
    },
    {
      name: 'circle',
      nodeDimensionsIncludeLabels: true,
      directed: true,
      animate: true
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
      maximal: true
    }
  ];
  layout = this.layouts[0];
  selectedLayout = this.layout.name;
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
    this.flow.states.forEach(s => {
      graph.nodes.push({
        data: {
          id: s._id,
          name: s.storyDefinitionId === 'tock_unknown_story' ? 'unknown' : s.storyDefinitionId,
          weight: s.count,
          colorCode: entityColor(s.storyDefinitionId ? s.storyDefinitionId : "start"),
          shapeType: 'roundrectangle'
        }
      })
    });
    let transitions = [];
    this.flow.transitions.forEach(t => {
      transitions[t.previousStateId] = (transitions[t.previousStateId] ? transitions[t.previousStateId] : 0)
        + t.count;
    });
    this.flow.transitions.forEach(t => {
      let percentage = Math.round((t.count * 10000.0) / transitions[t.previousStateId]) / 100;
      graph.edges.push({
        data: {
          source: t.previousStateId ? t.previousStateId : 'null',
          target: t.nextStateId,
          colorCode: entityColor(DialogFlowStateTransitionType[t.type] ? DialogFlowStateTransitionType[t.type] : DialogFlowStateTransitionType[DialogFlowStateTransitionType.nlp]),
          strength: t.count,
          label: percentage + '%',
          classes: 'autorotate'
        }
      });
    });
    this.graphData = graph;
  }

  nodeChange(id: string) {
    this.selectedNode = this.flow.states.find(s => s._id === id);
  }
}
