export interface Question {
  value: string;
  _edit?: boolean;
}
export interface Fragment {
  answer: string;
  use?: boolean;
  questions?: Question[];
  _edit?: boolean;
}
