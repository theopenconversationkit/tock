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

import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { BotService } from '../../bot/bot-service';
import { AnalyticsService } from '../analytics.service';
import { StateService } from '../../core-nlp/state.service';
import {
  ApplicationDialogFlow,
  DialogFlowRequest,
  DialogFlowStateData,
  DialogFlowStateTransitionData,
  DialogFlowStateTransitionType
} from './flow';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { entityColor } from '../../model/nlp';
import { KeyValue } from '@angular/common';
import { NodeTransition, NodeTypeFilter, NodeTypeFilters, StoryNode } from './node';
import { AnswerConfigurationType, StoryDefinitionConfiguration, StorySearchQuery, StoryStep } from '../../bot/model/story';
import { Subject, takeUntil } from 'rxjs';
import { NbCalendarRange, NbDateService, NbDatepickerDirective, NbToastrService } from '@nebular/theme';
import { darkenIfTooLight, toISOStringWithoutOffset, truncateString } from '../../shared/utils';
import { SelectBotEvent } from '../../shared/components';
import { EChartsOption } from 'echarts';
import { timeRanges } from './time-ranges';
import { layouts } from './layouts';
import { animate, style, transition, trigger } from '@angular/animations';

type Graph = {
  edges: any[];
  nodes: any[];
};

type TransitionsMap = Map<string, NodeTransition>;

@Component({
  selector: 'tock-flow',
  templateUrl: './flow.component.html',
  styleUrls: ['./flow.component.scss'],
  animations: [
    trigger('inOutRightAnimation', [
      transition(':enter', [style({ right: -500, opacity: 0 }), animate('0.3s ease-out', style({ right: 0, opacity: 1 }))]),
      transition(':leave', [style({ right: 0, opacity: 1 }), animate('0.3s ease-in', style({ right: -500, opacity: 0 }))])
    ]),
    trigger('inOutHeightAnimation', [
      transition(':enter', [style({ height: 0 }), animate('0.3s ease-out', style({ height: '*' }))]),
      transition(':leave', [style({ height: '*' }), animate('0.3s ease-in', style({ height: 0 }))])
    ])
  ]
})
export class FlowComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  range: NbCalendarRange<Date>;

  timeRanges = timeRanges;

  layouts = layouts;

  layout = this.layouts[0];

  typeFilters = NodeTypeFilters;
  selectedTypeFilter = NodeTypeFilters[0];
  typeFilterCounters: Map<NodeTypeFilter, number> = new Map();
  recursive: boolean = false;
  entity: boolean = false;
  step: boolean = false;
  intent: boolean = false;
  minimalNodeCount: number = 0;
  maxNodeCount: number = 1;
  minimalTransitionPercentage: number = 10;

  selectedStory: StoryNode;
  direction: number;

  storiesById: Map<string, StoryDefinitionConfiguration> = new Map();
  nodesById: Map<string, StoryNode> = new Map();

  selectedNode: StoryNode;
  allNodes: StoryNode[];
  allTransitionsSize: number;

  selectedConnectorId: string;
  selectedConfigurationName: string;
  lastFlowRequest: DialogFlowRequest;
  userFlow: ApplicationDialogFlow;

  allStories: StoryDefinitionConfiguration[];
  configuredStories: StoryDefinitionConfiguration[];
  staticFlow: ApplicationDialogFlow;
  statsMode: boolean = true;
  displayNodeType: boolean = false;
  displayNodeCount: boolean = false;
  displayTests: boolean = true;
  displayDisabled: boolean = false;
  mergeOldStories: boolean = true;
  displayDebug: boolean = false;
  showAllConfigurations: boolean = false;

  @ViewChild(NbDatepickerDirective) dateRangeInputDirectiveRef;

  advanced: boolean = false;

  sankeyData: EChartsOption;
  forceData: EChartsOption;
  circularData: EChartsOption;

  loading: boolean = false;

  constructor(
    public state: StateService,
    private analytics: AnalyticsService,
    private bot: BotService,
    private botConfiguration: BotConfigurationService,
    private toastrService: NbToastrService,
    private dateService: NbDateService<Date>
  ) {}

  ngOnInit(): void {
    this.initTimeRange();

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((conf) => {
      if (conf?.length) {
        const retainedConfs = conf
          .filter((c) => c.targetConfigurationId == null)
          .sort((c1, c2) => c1.applicationId.localeCompare(c2.applicationId));

        this.selectedConfigurationName = retainedConfs[0].name;
        this.selectedConnectorId = retainedConfs[0]._id;
        this.reload();
      }
    });
  }

  reload(forceReload?: boolean) {
    console.debug('Loading flow...');

    if (this.selectedConfigurationName || this.showAllConfigurations) {
      // Reload user flow
      if (this.statsMode) {
        const request = new DialogFlowRequest(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          this.state.currentApplication.name,
          this.selectedConfigurationName,
          this.selectedConnectorId,
          toISOStringWithoutOffset(this.range.start),
          toISOStringWithoutOffset(this.range.end),
          this.displayTests
        );
        if (forceReload == true || !request.equals(this.lastFlowRequest)) {
          console.debug('Fetching user flow...');
          this.loading = true;

          this.lastFlowRequest = request;
          this.analytics.getApplicationFlow(request).subscribe((f) => {
            this.loading = false;
            this.userFlow = f;
            console.debug('Application flow retrieved, incl. ' + f.states.length + ' states ' + f.transitions.length + ' transitions.');
            this.reset();
          });
        }
      } else {
        // Reload static flow
        console.debug('Fetching stories...');
        this.loading = true;
        this.userFlow = null;
        this.bot
          .getStories(
            new StorySearchQuery(
              this.state.currentApplication.namespace,
              this.state.currentApplication.name,
              this.state.currentLocale,
              0,
              10000
            )
          )
          .subscribe((s) => {
            this.loading = false;
            this.allStories = s;
            this.configuredStories = s.filter((story) => !story.isBuiltIn());
            console.debug(this.allStories.length + ' stories retrieved, incl. ' + this.configuredStories.length + ' configured stories.');
            this.buildStaticFlowFromStories();
            this.reset();
          });
      }
    }
  }

  reset() {
    this.selectedStory = null;
    this.selectedNode = null;
    this.direction = null;
    this.update();
  }

  update() {
    this.minimalNodeCount = 0;
    this.updateCount();
  }

  updateCount() {
    this.forceData = undefined;
    this.circularData = undefined;
    this.sankeyData = undefined;

    setTimeout((_) => {
      this.buildGraph(this.statsMode ? this.userFlow : this.staticFlow);
    });
  }

  private buildStaticFlowFromStories() {
    console.debug('Building static flow from stories...');
    const intentStoryMap = new Map<string, StoryDefinitionConfiguration>();
    const states: DialogFlowStateData[] = [];
    const transitions: DialogFlowStateTransitionData[] = [];
    this.allStories.forEach((s) => {
      intentStoryMap.set(s.intent.name, s);
      states.push(new DialogFlowStateData(s._id, s.intent.name, [], null, s.currentType, s.name, 0, s._id));
      transitions.push(new DialogFlowStateTransitionData(s._id, [], DialogFlowStateTransitionType.nlp, null, null, 0));
    });

    this.allStories.forEach((s) => {
      const intents = new Set<string>();
      StoryStep.findOutcomingIntent(intents, s.steps);
      intents.forEach((i) => {
        const targetStory = intentStoryMap.get(i);
        if (targetStory) {
          transitions.push(
            new DialogFlowStateTransitionData(targetStory.intent.name, [], DialogFlowStateTransitionType.nlp, s.intent.name, null, 0)
          );
        }
      });
    });
    this.staticFlow = new ApplicationDialogFlow(states, transitions);
    this.reset();
  }

  changeMode() {
    this.reload(true);
  }

  changeShowAllConfigurations() {
    if (this.showAllConfigurations) {
      this.selectedConfigurationChanged(null);
    }

    if (!this.showAllConfigurations && !this.selectedConfigurationName) {
      this.state.resetConfiguration();
    }
  }

  selectedConfigurationChanged(event?: SelectBotEvent) {
    this.selectedConfigurationName = !event ? null : event.configurationName;
    this.selectedConnectorId = !event ? null : event.configurationId;
    this.reload();
  }

  buildGraph(theFlow: ApplicationDialogFlow) {
    console.debug('Building graph from flow...');
    let flow = theFlow
      ? this.mergeOldStories
        ? JSON.parse(JSON.stringify(theFlow))
        : theFlow // clone - states might be modified by merge
      : undefined;
    if (flow) {
      const displayOnlyNext: boolean = this.direction === -1;
      const displayOnlyPrev: boolean = this.direction === 1;
      const graph: Graph = {
        nodes: [],
        edges: []
      };

      //1 create nodes
      let nodesNumber = 0;
      const nodesByIndex = new Map<number, StoryNode>();
      const nodesByStateId = new Map<string, StoryNode>();
      const nodesByStoryKey = new Map<string, StoryNode>();

      flow.states.forEach((s) => {
        if (
          this.statsMode ||
          this.displayDisabled ||
          !this.allStories.find((aStory) => aStory._id == s._id).isDisabled(this.selectedConnectorId)
        ) {
          if (this.statsEntity() && this.statsStep() && this.intent) {
            const node = new StoryNode(
              nodesNumber++,
              s._id,
              s.storyDefinitionId,
              [s],
              s.entities,
              s.intent,
              s.step,
              s.storyType,
              s.storyName
            );
            nodesByIndex.set(node.index, node);
            nodesByStateId.set(s._id, node);
          } else {
            let keyWithoutType =
              s.storyDefinitionId +
              (this.statsIntent() ? '+' + s.intent : '') +
              (this.statsStep() ? '+' + s.step : '') +
              (this.statsEntity() ? '+' + s.entities.join('%') : '');
            let key = keyWithoutType + (s.storyType != undefined ? '+' + s.storyType : '');

            let node = nodesByStoryKey.get(key);
            if (node) {
              node.states.push(s);
            } else {
              if (this.mergeOldStories && key != keyWithoutType) {
                const nodeWithoutType = nodesByStoryKey.get(keyWithoutType);
                if (nodeWithoutType) {
                  console.debug('Replacing previous node with typed node...');
                  // Merge previous node to a new typed node
                  node = new StoryNode(
                    nodeWithoutType.index,
                    s.storyDefinitionId,
                    s.storyDefinitionId,
                    [s],
                    this.statsEntity() ? s.entities : [],
                    this.intent ? s.intent : null,
                    this.statsStep() ? s.step : null,
                    s.storyType,
                    s.storyName
                  );
                  node.states.push.apply(node.states, nodeWithoutType.states);

                  // Update maps
                  nodesByStoryKey.delete(keyWithoutType);
                  nodesByStoryKey.set(key, node);
                  nodesByIndex.set(node.index, node);
                  node.states.forEach((state) => {
                    state.storyType = s.storyType;
                    nodesByStateId.set(state._id, node);
                  });
                } else {
                  node = new StoryNode(
                    nodesNumber++,
                    s.storyDefinitionId,
                    s.storyDefinitionId,
                    [s],
                    this.statsEntity() ? s.entities : [],
                    this.intent ? s.intent : null,
                    this.statsStep() ? s.step : null,
                    s.storyType,
                    s.storyName
                  );
                  nodesByStoryKey.set(key, node);
                  nodesByIndex.set(node.index, node);
                }
              } else {
                node = new StoryNode(
                  nodesNumber++,
                  s.storyDefinitionId,
                  s.storyDefinitionId,
                  [s],
                  this.statsEntity() ? s.entities : [],
                  this.intent ? s.intent : null,
                  this.statsStep() ? s.step : null,
                  s.storyType,
                  s.storyName
                );
                nodesByStoryKey.set(key, node);
                nodesByIndex.set(node.index, node);
              }
            }
            nodesByStateId.set(s._id, node);
          }
        }
      });

      //2 set stories map
      const storiesById = new Map<string, StoryDefinitionConfiguration>();
      if (!this.statsMode) {
        nodesByIndex.forEach((s) => {
          const theStoryId = s._id;
          const theStoryName = s.storyName;
          const theStory = this.allStories.find((aStory) => aStory._id == theStoryId);
          if (theStory) {
            storiesById.set(theStoryId, theStory);
          } else {
            console.warn('Cannot find story: ' + theStoryName + ' (ID: ' + theStoryId + ')');
          }
        });
      }

      //3 filter state by count and type
      let finalNodes: StoryNode[] = [];
      this.typeFilterCounters.clear();
      this.typeFilters = new Array<NodeTypeFilter>();
      NodeTypeFilters.forEach((f) => {
        if (f.alwaysDisplay) {
          this.typeFilters.push(f);
          this.typeFilterCounters.set(f, 0);
        }
      });

      nodesByIndex.forEach((s) => {
        NodeTypeFilters.forEach((f) => {
          if (f.filter(s)) {
            if (!f.alwaysDisplay && !this.typeFilters.find((filter) => filter == f)) this.typeFilters.push(f);
            this.typeFilterCounters.set(f, this.typeFilterCounters.has(f) ? this.typeFilterCounters.get(f) + 1 : 1);
          }
        });
        if (this.minimalNodeCount <= s.count && this.selectedTypeFilter.filter(s)) {
          finalNodes[s.index] = s;
        }
      });
      if (finalNodes.length < 1) {
        this.toastrService.show('Please change options to find nodes to render.', 'No node to render', {
          duration: 5000,
          status: 'warning'
        });
      } else {
        //4 create transitions
        const countTransitionByStartId = [];
        const transitionsByKey: TransitionsMap = new Map();

        flow.transitions.forEach((t) => {
          const prev = nodesByStateId.get(t.previousStateId);
          const next = nodesByStateId.get(t.nextStateId);
          const prevId = prev ? prev.index : -1;
          //next should always exist but check anyway...
          if (next) {
            const nextId = next.index;
            if (this.recursive || prev !== next) {
              const tId = prevId + '_' + nextId + '_' + t.type;
              const transition = transitionsByKey.get(tId);
              if (!transition) {
                transitionsByKey.set(tId, new NodeTransition([t], prevId, nextId, t.type));
              } else {
                transition.transitions.push(t);
              }
              const oldCount = countTransitionByStartId[prevId];
              countTransitionByStartId[prevId] = oldCount ? oldCount + t.count : t.count;
            }
          }
        });

        //5 filter transitions per percentage
        const finalTransitions = new Map<string, NodeTransition>();
        transitionsByKey.forEach((t, k) => {
          const prev = nodesByIndex.get(t.previousId);
          const next = nodesByIndex.get(t.nextId);
          const prevId = prev ? prev.index : -1;
          const nextId = next.index;
          const finalPrev = prev ? finalNodes[prev.index] : null;
          const finalNext = finalNodes[nextId];
          const percentage = Math.round((t.count * 10000.0) / countTransitionByStartId[prevId]) / 100;
          if ((this.recursive || prev !== next) && finalNodes[nextId] && (t.previousId === -1 || finalNodes[prevId])) {
            if (
              !this.statsMode ||
              (percentage >= this.minimalTransitionPercentage &&
                (!this.selectedStory ||
                  (!displayOnlyPrev && finalPrev && finalPrev.storyDefinitionId === this.selectedStory._id) ||
                  (!displayOnlyNext && finalNext && finalNext.storyDefinitionId === this.selectedStory._id)))
            ) {
              finalTransitions.set(k, t);
            }
          }
        });

        //6 filter by selected story and create graph nodes
        let addStartup = true;
        const tmpFinalStates = [];
        const theMinCount = finalNodes.reduce((prev, current) => (prev.count < current.count ? prev : current)).count;
        const theMaxCount = finalNodes.reduce((prev, current) => (prev.count > current.count ? prev : current)).count;
        if (this.statsMode) {
          console.debug('Node min visits: ' + theMinCount);
          console.debug('Node max visits: ' + theMaxCount);
        }

        finalNodes.forEach((s) => {
          let include = true;
          if (this.selectedStory) {
            //try to find a transition with this state and selectedStory ID
            include = s.storyDefinitionId === this.selectedStory._id;
            if (!include) {
              finalTransitions.forEach((t) => {
                if (!include) {
                  const prev = finalNodes[t.previousId];
                  const next = finalNodes[t.nextId];
                  const prevId = prev ? prev.index : -1;
                  if (
                    ((!displayOnlyNext && prev && prevId === s.index) || (!displayOnlyPrev && next && next.index === s.index)) &&
                    ((!displayOnlyPrev && prev && prev.storyDefinitionId === this.selectedStory._id) ||
                      (!displayOnlyNext && next && next.storyDefinitionId === this.selectedStory._id))
                  ) {
                    include = true;
                  }
                }
              });
            }
          }
          if (include) {
            addStartup = false;
            tmpFinalStates[s.index] = s;

            const theScore = this.statsMode ? Math.round(((s.count - theMinCount) * 100.0) / (theMaxCount - theMinCount)) : 0;

            const theNodeName =
              (this.displayNodeType
                ? s.storyType == AnswerConfigurationType.builtin
                  ? ' 🔧 '
                  : s.storyType == undefined
                  ? ' ? '
                  : ' 💬 '
                : '') +
              (this.displayNodeCount ? ' (x' + this.displayCount(s.count) + ') ' : '') +
              (!this.statsMode &&
              this.displayDisabled &&
              this.allStories.find((aStory) => aStory._id == s.storyDefinitionId).isDisabled(this.selectedConnectorId)
                ? ' 🚫 '
                : '') +
              s.nodeName();

            graph.nodes.push({
              data: {
                id: s.index,
                name: theNodeName,
                weight: theScore ? theScore : 0,
                colorCode: entityColor(s.storyDefinitionId),
                shapeType: s.dynamic ? 'circle' : 'roundRect'
              }
            });
          }
        });
        finalNodes = tmpFinalStates;

        if (finalTransitions.size > 1000) {
          this.toastrService.show(
            'More than 1000 nodes to render. Please change options to fetch less or filter out more.',
            'Too many nodes to render',
            { duration: 5000, status: 'warning' }
          );
        } else {
          //7 create graph edges
          finalTransitions.forEach((t, k) => {
            const prev = nodesByIndex.get(t.previousId);
            const next = nodesByIndex.get(t.nextId);
            const prevId = prev ? prev.index : -1;
            let percentage = Math.round((t.count * 10000.0) / countTransitionByStartId[prevId]) / 100;
            let fPrev = finalNodes[prevId];
            let fNext = finalNodes[next.index];
            if (
              fNext &&
              (t.previousId === -1 || fPrev) &&
              (!this.selectedStory ||
                (!displayOnlyPrev && fPrev && this.selectedStory._id === fPrev.storyDefinitionId) ||
                (!displayOnlyNext && this.selectedStory._id === fNext.storyDefinitionId))
            ) {
              if (t.previousId === -1) {
                addStartup = true;
              }

              graph.edges.push({
                data: {
                  source: prevId,
                  target: next.index,
                  key: k,
                  colorCode: entityColor(
                    DialogFlowStateTransitionType[t.type]
                      ? DialogFlowStateTransitionType[t.type]
                      : DialogFlowStateTransitionType[DialogFlowStateTransitionType.nlp]
                  ),
                  strength: t.count,
                  label: this.statsMode ? percentage + '%' : '',
                  classes: 'autorotate'
                }
              });
            }
          });

          //8 add startup if useful
          if (addStartup) {
            graph.nodes.push({
              data: { id: -1, name: 'Startup', weight: 40, colorCode: 'cornflowerblue', shapeType: 'triangle' }
            });
          }

          //9 init vars
          this.maxNodeCount = theMaxCount;
          this.storiesById = storiesById;
          this.nodesById = nodesByStoryKey.size > 0 ? nodesByStoryKey : nodesByStateId;
          this.allNodes = finalNodes.filter((n): n is StoryNode => n !== null);
          this.allTransitionsSize = finalTransitions.size;

          // create flow diagram(sankey)
          this.createForceGraph(graph, finalTransitions);
          this.createSankeyDiagram(finalTransitions);
          this.createCircularGraph(graph, finalTransitions);
        }
      }
    }
  }

  getEchartsGraphNodes(graph: Graph) {
    return graph.nodes.map((node) => {
      return {
        id: node.data.id.toString(),
        name: truncateString(node.data.name, 30, true, false),
        fullName: node.data.name,
        symbol: node.data.shapeType,
        symbolSize: Math.max(Math.min(node.data.weight, 50), 10),
        itemStyle: {
          color: node.data.colorCode
        },
        label: {
          color: 'white',
          textBorderColor: darkenIfTooLight(node.data.colorCode),
          textBorderWidth: 2
        }
      };
    });
  }

  getEchartsGraphEdges(graph: Graph) {
    const maxStrength = Math.max(...graph.edges.map((o) => o.data.strength));
    const maxLineWidth = 5;

    return graph.edges.map((edge) => {
      const lineWidth = Math.round(Math.max(Math.min((edge.data.strength / maxStrength) * maxLineWidth, maxLineWidth), 1));

      return {
        source: edge.data.source.toString(),
        target: edge.data.target.toString(),
        symbol: ['circle', 'arrow'],
        symbolSize: [5, 10],
        label: {
          show: true,
          formatter: edge.data.label,
          color: 'grey',
          distance: 2,
          fontSize: 10
        },
        lineStyle: {
          width: lineWidth,
          color: graph.nodes.find((n) => n.data.id.toString() === edge.data.source.toString()).data.colorCode
        }
      };
    });
  }

  echartsTooltipFormatter(params: any, transitions?: TransitionsMap): string {
    if (params.data.fullName) {
      if (params.data.id === '-1') return 'Startup';
      const nodeIndex = params.data.id;
      const node = this.allNodes.find((n) => n.index.toString() === nodeIndex);
      const plural = node.count > 1 ? 's' : '';
      return `Story <strong>${params.data.fullName}</strong> <br />was triggered <strong>${node.count} time${plural}</strong>`;
    }

    if (params.data.source) {
      const key = params.data.source + '_' + params.data.target + '_0';
      const transition = transitions.get(key);

      const sourceNodeIndex = params.data.source;
      let sourceNodeStoryName;
      if (sourceNodeIndex === '-1') {
        sourceNodeStoryName = 'Startup';
      } else {
        const sourceNode = this.allNodes.find((n) => n.index.toString() === sourceNodeIndex);
        sourceNodeStoryName = sourceNode.storyName;
      }

      const targetNodeIndex = params.data.target;
      const targetNode = this.allNodes.find((n) => n.index.toString() === targetNodeIndex);
      const targetNodeStoryName = targetNode.storyName;

      let tip = `From: <strong>${sourceNodeStoryName}</strong><br>To: <strong>${targetNodeStoryName}</strong>`;

      if (this.statsMode) {
        tip = tip + `<br>Count: <strong>${transition.count}</strong>`;
      }

      return tip;
    }
    return undefined;
  }

  createForceGraph(graph: Graph, transitions: TransitionsMap): void {
    const nodes = this.getEchartsGraphNodes(graph);
    const links = this.getEchartsGraphEdges(graph);

    this.forceData = {
      tooltip: {
        formatter: (params) => {
          return this.echartsTooltipFormatter(params, transitions);
        }
      },
      series: {
        type: 'graph',
        layout: 'force',
        force: {
          initLayout: 'circular',
          layoutAnimation: true,
          repulsion: 1000,
          gravity: 0,
          edgeLength: 100,
          friction: 1
        },
        roam: true,
        center: ['50%', '50%'],
        zoom: 1,
        scaleLimit: {
          min: 0.2,
          max: 10
        },
        emphasis: {
          focus: 'adjacency',
          label: {
            show: true
          }
        },
        label: {
          show: true
        },
        lineStyle: {
          opacity: 0.9,
          width: 2,
          curveness: 0.2
        },
        data: nodes,
        links: links
      }
    };
  }

  createCircularGraph(graph: Graph, transitions: TransitionsMap): void {
    const nodes = this.getEchartsGraphNodes(graph);
    const links = this.getEchartsGraphEdges(graph);

    this.circularData = {
      tooltip: {
        formatter: (params) => {
          return this.echartsTooltipFormatter(params, transitions);
        }
      },
      series: {
        type: 'graph',
        layout: 'circular',
        circular: {
          rotateLabel: true
        },
        roam: true,
        zoom: 1,
        scaleLimit: {
          min: 0.2,
          max: 10
        },
        emphasis: {
          focus: 'adjacency',
          label: {
            show: true
          }
        },
        label: {
          show: true
        },
        lineStyle: {
          opacity: 0.9,
          width: 2,
          curveness: 0.2
        },
        data: nodes,
        links: links
      }
    };
  }

  onEchartsGraphClick(params): void {
    params.event.event.stopPropagation();
    if (params.data.name) {
      if (params.data.id !== '-1') {
        const nodeIndex = params.data.id;
        this.selectNode(params.data.id);
        return;
      }
    }
    this.unselect();
  }

  createSankeyDiagram(transitions: TransitionsMap) {
    let allNodes = [];
    let links = [];

    const trByNodeNamesIndex = new Map<string, number>();
    transitions.forEach((transition, key) => {
      const keyValues = key.split('_');
      let count = 0;
      transition.transitions.forEach((tr) => {
        count += tr.count;
      });

      let startNodeName = this.getStoryName(keyValues);
      let destNodeName = this.allNodes[keyValues[1]]?.storyName;

      if (startNodeName !== destNodeName) {
        const mapKey = `${startNodeName}#${destNodeName}`;
        let result = trByNodeNamesIndex.get(mapKey);
        if (result != null) {
          trByNodeNamesIndex.set(mapKey, count + result);
        } else {
          let element = Array.from(trByNodeNamesIndex.keys()).find((key) => key.startsWith(destNodeName));
          if (element == null) {
            trByNodeNamesIndex.set(mapKey, count);
          }
        }
      }
    });

    trByNodeNamesIndex.forEach((value, key) => {
      const nodes = key.split('#');
      if (allNodes.indexOf(nodes[0]) < 0) allNodes.push(nodes[0]);
      if (allNodes.indexOf(nodes[1]) < 0) allNodes.push(nodes[1]);

      links.push({
        source: nodes[0],
        target: nodes[1],
        value: value
      });
    });

    this.sankeyData = {
      tooltip: {
        formatter: (params) => {
          if (params.data.fullName) {
            if (params.data.fullName === 'Startup') return 'Startup';
            return `Story <strong>${params.data.fullName}</strong>`;
          }

          if (params.data.source) {
            return `From: <strong>${params.data.source}</strong><br>To: <strong>${params.data.target}</strong><br>Count: <strong>${params.data.value}</strong>`;
          }
          return undefined;
        }
      },
      series: {
        type: 'sankey',
        animation: false,
        emphasis: {
          focus: 'adjacency'
        },

        data: allNodes.map((node) => {
          return { name: truncateString(node, 30, true, false), fullName: node };
        }),
        links: links
      }
    };
  }

  swapAdvanced(): void {
    this.advanced = !this.advanced;
  }

  statsEntity(): boolean {
    return this.statsMode && this.entity;
  }

  statsStep(): boolean {
    return this.statsMode && this.step;
  }

  statsIntent(): boolean {
    return this.statsMode && this.intent;
  }

  valueAscOrder = (a: KeyValue<string, string>, b: KeyValue<string, string>): number => {
    return a.value.localeCompare(b.value);
  };

  datesChanged(dates: [Date, Date]) {
    this.range.start = dates[0];
    this.range.end = dates[1];
    this.state.dateRange = {
      start: dates[0],
      end: dates[1],
      rangeInDays: this.state.dateRange.rangeInDays
    };
    this.reload();
  }

  normalizeTimeRange() {
    this.range.start = this.dateService.setHours(this.range.start, 0);
    this.range.start = this.dateService.setMinutes(this.range.start, 0);
    this.range.start = this.dateService.setSeconds(this.range.start, 0);

    this.range.end = this.dateService.setHours(this.range.end, 23);
    this.range.end = this.dateService.setMinutes(this.range.end, 59);
    this.range.end = this.dateService.setSeconds(this.range.end, 59);
  }

  setTimeRange(timeSpan): void {
    this.range.start = this.dateService.addDay(this.dateService.today(), -timeSpan.duration);

    this.range.end = this.dateService.addDay(this.dateService.today(), +timeSpan.offset || 0);

    this.normalizeTimeRange();

    this.dateRangeInputDirectiveRef.writeValue(this.range);

    this.state.dateRange = {
      start: this.range.start,
      end: this.range.end,
      rangeInDays: this.state.dateRange.rangeInDays
    };
    this.reload();
  }

  datePickerChange(event: NbCalendarRange<Date>): void {
    if (event.end) {
      this.normalizeTimeRange();
      this.state.dateRange = {
        start: this.range.start,
        end: this.range.end,
        rangeInDays: this.state.dateRange.rangeInDays
      };
      this.reload();
    }
  }

  getTimeRangeBttnStatus(range) {
    if (this.range.end && this.range.start) {
      const spanInDays = (this.range.end.getTime() - this.range.start.getTime()) / 1000 / 60 / 60 / 24;
      const now = new Date();

      if (spanInDays < 1 && range.duration <= 1) {
        if (range.offset) {
          const offsetDate = this.dateService.addDay(now, range.offset);
          if (this.range.start.toDateString() === offsetDate.toDateString()) return 'basic';
        } else {
          if (this.range.start.toDateString() === now.toDateString()) return 'basic';
        }
      } else if (Math.floor(spanInDays) === range.duration) {
        if (this.range.end.toDateString() === now.toDateString()) return 'basic';
      }
    }

    return 'info';
  }

  initTimeRange() {
    this.range = {
      start: this.dateService.addDay(this.dateService.today(), -6),
      end: this.dateService.today()
    };

    if (this.state.dateRange.start) {
      this.range = {
        start: this.state.dateRange.start,
        end: this.state.dateRange.end
      };
    }
  }

  getStoryName(keyValues: string[]): string {
    if (keyValues[0] != '-1') {
      return this.allNodes[keyValues[0]]?.storyName;
    } else {
      return 'Startup';
    }
  }

  unselect(): void {
    this.selectedNode = null;
  }

  selectNode(id: string) {
    this.selectedNode = this.allNodes[id];
  }

  displayCount(count: number): string {
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

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
