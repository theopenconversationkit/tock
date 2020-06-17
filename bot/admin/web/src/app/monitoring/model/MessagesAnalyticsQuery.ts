export class MessagesAnalyticsQuery {
  constructor(public namespace: string,
              public applicationName: string,
              public nlpModel: string,
              public from?: Date,
              public to?: Date) {
  }
}
