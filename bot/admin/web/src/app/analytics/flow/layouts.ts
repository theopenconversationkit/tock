export const layouts = [
  {
    name: 'Force-based',
    type: 'Echarts'
  },
  {
    name: 'Sankey',
    type: 'Echarts'
  },
  {
    name: 'Circular',
    type: 'Echarts'
  },

  {
    name: 'cola',
    nodeDimensionsIncludeLabels: true,
    animate: true,
    flow: { axis: 'x', minSeparation: 30 }
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
  /*
    {
      name: 'elk',
      nodeDimensionsIncludeLabels: true,
      elk: {
        direction: 'RIGHT',
        edgeRouting: 'SPLINES',
      }
    },*/
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
