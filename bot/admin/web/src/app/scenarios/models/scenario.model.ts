export interface Scenario {
  id: number;
  name: string;
  category: string;
  tags: Array<string>;
  dateCreation: Date;
  dateModification?: Date;
  data?: Array<any>;
}
