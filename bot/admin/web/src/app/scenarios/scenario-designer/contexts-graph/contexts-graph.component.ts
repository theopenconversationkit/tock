import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';
import { Edge, GraphEdge, graphlib, layout, Node } from 'dagre';

import { getScenarioActionDefinitions } from '../../commons/utils';
import { ScenarioActionDefinition, ScenarioVersionExtended } from '../../models';
import { svgPathRoundedCorners } from './utils';
import { CanvaAction, OffsetPosition } from '../../../shared/canvas/models';
import { StateService } from '../../../core-nlp/state.service';

type GraphNode = Node & { name?: string; actionDef?: ScenarioActionDefinition; type?: string };

@Component({
  selector: 'tock-scenario-contexts-graph',
  templateUrl: './contexts-graph.component.html',
  styleUrls: ['./contexts-graph.component.scss']
})
export class ContextsGraphComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  @Input() scenario: ScenarioVersionExtended;
  @ViewChild('canvasWrapperElem', { read: ElementRef }) canvasWrapperElem: ElementRef;
  @ViewChild('dummyContainer') dummyContainer: ElementRef;

  canvasPosition: OffsetPosition = {
    offsetLeft: 0,
    offsetWidth: 0,
    offsetTop: 0,
    offsetHeight: 0
  };

  readonly canvaAction: typeof CanvaAction = CanvaAction;

  constructor(private dialogRef: NbDialogRef<ContextsGraphComponent>, private stateService: StateService) {}

  ngOnInit(): void {
    this.initNodesList();
  }

  get graphAttributes(): {} {
    return this.graphReady ? this.graph.graph() : {};
  }

  get nodes(): GraphNode[] {
    return this.graphReady ? this.graph.nodes().map((v) => this.graph.node(v)) : [];
  }

  get edges(): { id: Edge; infos: GraphEdge }[] {
    return this.graphReady
      ? this.graph.edges().map((v) => {
          return { id: v, infos: this.graph.edge(v) };
        })
      : [];
  }

  noGraphInScenario: boolean = false;

  private initNodesList(): void {
    const nodesLUT: { width?: number } = {};
    const actionDefs = getScenarioActionDefinitions(this.scenario);

    actionDefs.forEach((actionDef) => {
      if (actionDef.outputContextNames.length || actionDef.inputContextNames.length) {
        nodesLUT[actionDef.name] = { label: actionDef.name, type: 'action', actionDef };

        actionDef.outputContextNames.forEach((outCtxName) => {
          if (!nodesLUT[outCtxName]) nodesLUT[outCtxName] = { label: outCtxName, type: 'context' };
        });
        actionDef.inputContextNames.forEach((inCtxName) => {
          if (!nodesLUT[inCtxName]) nodesLUT[inCtxName] = { label: inCtxName, type: 'context' };
        });
      }
    });

    if (Object.keys(nodesLUT).length < 1) {
      this.noGraphInScenario = true;
      return;
    }

    this.measureNodes(nodesLUT);
  }

  dummyNode: { name: string; type: string };

  private measureNodes(nodesLUT: {}): void {
    for (let labName in nodesLUT) {
      const label = nodesLUT[labName];
      if (!label.width) {
        this.dummyNode = { name: label.label, type: label.type };

        // Allows to increase the width of the node according to the number of characters to allow, for example, to make the label bold on hovering without the text overflowing its box
        const widthIncrease = 0; //label.label.length * 0.5;

        setTimeout(() => {
          label.width = this.dummyContainer.nativeElement.offsetWidth + widthIncrease;
          label.height = this.dummyContainer.nativeElement.offsetHeight;
          this.measureNodes(nodesLUT);
        });
        return;
      }
    }
    this.dummyNode = undefined;
    this.computeGraph(nodesLUT);
  }

  graph: graphlib.Graph<{}>;
  graphPadding: number = 20;
  graphReady: boolean = false;

  private computeGraph(nodesLUT: {}): void {
    const actionDefs = getScenarioActionDefinitions(this.scenario);

    this.graph = new graphlib.Graph({ directed: true, compound: false });

    this.graph.setGraph({ ranksep: 48, nodesep: 20, edgesep: 10, rankdir: 'TB' });
    this.graph.setDefaultEdgeLabel(() => ({}));

    actionDefs.forEach((actionDef) => {
      if (actionDef.outputContextNames.length || actionDef.inputContextNames.length) {
        const nodeInfos = nodesLUT[actionDef.name];

        this.graph.setNode(actionDef.name, {
          name: actionDef.name,
          label: actionDef.name,
          type: nodeInfos.type,
          actionDef: nodeInfos.actionDef,
          width: nodeInfos.width,
          height: nodeInfos.height,
          offsetx: nodeInfos.width / 2,
          offsety: nodeInfos.height / 2
        });

        actionDef.outputContextNames.forEach((outCtxName) => {
          if (!this.graph.hasNode[outCtxName]) {
            const nodeInfos = nodesLUT[outCtxName];

            this.graph.setNode(outCtxName, {
              name: outCtxName,
              label: outCtxName,
              type: nodeInfos.type,
              width: nodeInfos.width,
              height: nodeInfos.height,
              offsetx: nodeInfos.width / 2,
              offsety: nodeInfos.height / 2
            });
          }
          this.graph.setEdge(actionDef.name, outCtxName, { minlen: 1 });
        });
        actionDef.inputContextNames.forEach((inCtxName) => {
          if (!this.graph.hasNode[inCtxName]) {
            const nodeInfos = nodesLUT[inCtxName];

            this.graph.setNode(inCtxName, {
              name: inCtxName,
              label: inCtxName,
              type: nodeInfos.type,
              width: nodeInfos.width,
              height: nodeInfos.height,
              offsetx: nodeInfos.width / 2,
              offsety: nodeInfos.height / 2
            });
          }
          this.graph.setEdge(inCtxName, actionDef.name, { minlen: 1 });
        });
      }
    });

    layout(this.graph);

    this.graph.edges().forEach((e) => {
      const edge = this.graph.edge(e);
      const rpath = svgPathRoundedCorners(edge.points, 5, false);
      this.graph.setEdge(e.v, e.w, { points: edge.points, minlen: edge.minlen, rpath: rpath });
    });

    const graphAttributes = this.graph.graph();
    this.canvasPosition = {
      offsetLeft: 0,
      offsetTop: 0,
      offsetWidth: graphAttributes.width,
      offsetHeight: Math.min(graphAttributes.height, this.canvasWrapperElem.nativeElement.offsetHeight - 50)
    };

    this.graphReady = true;
  }

  getNodeTooltip(node: GraphNode): string {
    if (node.type == 'action') {
      if (node.actionDef.description) return node.actionDef.description;
      if (node.actionDef.answers?.length) {
        for (let index = 0; index < node.actionDef.answers.length; index++) {
          if (node.actionDef.answers[index].locale === this.stateService.currentLocale) {
            return node.actionDef.answers[index].answer;
          }
        }
        return node.actionDef.answers[0].answer;
      }
      return node.actionDef.name;
    }
    return node.label;
  }

  nodesHighlightLUT: Map<string, boolean>;
  edgesHighlightLUT: Map<Edge, boolean>;

  highlight(node?: GraphNode): void {
    if (!this.nodesHighlightLUT) {
      this.nodesHighlightLUT = new Map();
      this.graph.nodes().forEach((v) => this.nodesHighlightLUT.set(v, false));
    }
    if (!this.edgesHighlightLUT) {
      this.edgesHighlightLUT = new Map();
      this.graph.edges().forEach((e) => this.edgesHighlightLUT.set(e, false));
    }

    if (!node) {
      this.nodesHighlightLUT.forEach((e, k) => {
        this.nodesHighlightLUT.set(k, false);
      });
      this.edgesHighlightLUT.forEach((e, k) => {
        this.edgesHighlightLUT.set(k, false);
      });
    } else {
      const ancestors = this.getNodeAncestors(node.name);
      ancestors.nodes.forEach((n) => this.nodesHighlightLUT.set(n, true));
      ancestors.edges.forEach((e) => this.edgesHighlightLUT.set(e, true));

      const descendants = this.getNodeDescendants(node.name);
      descendants.nodes.forEach((n) => this.nodesHighlightLUT.set(n, true));
      descendants.edges.forEach((e) => this.edgesHighlightLUT.set(e, true));
    }
  }

  isNodeDimmed(nodeName: string): boolean {
    if (!this.nodesHighlightLUT) return false;

    let atLeastOneHigh: boolean = false;
    for (const v of this.nodesHighlightLUT) {
      if (v[1]) {
        atLeastOneHigh = true;
        break;
      }
    }
    if (!atLeastOneHigh) return false;

    return this.nodesHighlightLUT.get(nodeName) != true;
  }

  isEdgeDimmed(edge: Edge): boolean {
    if (!this.edgesHighlightLUT) return false;

    let atLeastOneHigh: boolean = false;
    for (const e of this.edgesHighlightLUT) {
      if (e[1]) {
        atLeastOneHigh = true;
        break;
      }
    }
    if (!atLeastOneHigh) return false;

    return this.edgesHighlightLUT.get(edge) != true;
  }

  private getNodeAncestors(nodeName: string, stack = { nodes: [], edges: [] }) {
    stack.nodes.push(nodeName);
    const inEdges = this.graph.inEdges(nodeName);
    stack.edges = [...stack.edges, ...inEdges];
    const predecessors = this.graph.predecessors(nodeName) as unknown as string[]; // looks like there is an error in graphLib typing. graph.predecessors actualy returns an array of nodes ids
    predecessors.forEach((p) => {
      this.getNodeAncestors(p, stack);
    });
    return stack;
  }

  private getNodeDescendants(nodeName: string, stack = { nodes: [], edges: [] }) {
    stack.nodes.push(nodeName);
    const outEdges = this.graph.outEdges(nodeName);
    stack.edges = [...stack.edges, ...outEdges];
    const descendants = this.graph.successors(nodeName) as unknown as string[];
    descendants.forEach((p) => {
      this.getNodeDescendants(p, stack);
    });
    return stack;
  }

  cancel(): void {
    this.dialogRef.close();
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
