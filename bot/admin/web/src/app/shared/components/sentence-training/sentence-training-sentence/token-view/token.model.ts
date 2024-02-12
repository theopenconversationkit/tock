import { ClassifiedEntity, EntityContainer } from '../../../../../model/nlp';

export class Token {
  public end: number;

  constructor(
    public start: number,
    public text: string,
    public sentence: EntityContainer,
    public entity?: ClassifiedEntity,
    public subTokens?: Token[]
  ) {
    this.end = this.start + text.length;
  }

  color(): string {
    return this.entity?.entityColor || '';
  }
}
