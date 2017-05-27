import { WebPage } from './app.po';

describe('web App', () => {
  let page: WebPage;

  beforeEach(() => {
    page = new WebPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
