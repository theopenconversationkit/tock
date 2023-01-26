export interface IndicatorDefinition {
  name: string;
  label: string;
  description: string;
  values: IndicatorValueDefinition[];
  dimensions: string[];
}

export interface IndicatorValueDefinition {
  name: string;
  label: string;
}
