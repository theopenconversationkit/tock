import { ChartComponent } from './chart.component';
import { UserAnalyticsQueryResult } from '../users/users';

describe('ChartComponent series selection', () => {
  let component: ChartComponent;

  beforeEach(() => {
    component = new ChartComponent(null);
    component.data = {
      dates: ['2026-01-01', '2026-01-02'],
      connectorsType: ['s0', 's1', 's2', 's3', 's4', 's5', 's6'],
      usersData: [
        [1, 2, 3, 4, 5, 6, 7],
        [1, 2, 3, 4, 5, 6, 7]
      ]
    } as unknown as UserAnalyticsQueryResult;
    // the graph itself is not under test; rebuild() does not call initSelectionList() for every chart type
    spyOn(component, 'rebuild').and.stub();
    component.initSelectionList();
  });

  it('selects the 5 first series by default', () => {
    expect(component.seriesSelectionList).toEqual([0, 1, 2, 3, 4]);
    expect(component.seriesSelectModel).toEqual([0, 1, 2, 3, 4]);
    expect(component.allSeriesSelected()).toBeFalse();
  });

  it('selects every series when "select all" is checked', () => {
    component.onSeriesSelectionChange([0, 1, 2, 3, 4, component.ALL_SERIES]);

    expect(component.seriesSelectionList).toEqual([0, 1, 2, 3, 4, 5, 6]);
    expect(component.allSeriesSelected()).toBeTrue();
    expect(component.seriesSelectModel).toContain(component.ALL_SERIES);
  });

  it('checks "select all" when the last series is selected by hand', () => {
    component.onSeriesSelectionChange([0, 1, 2, 3, 4, 5, 6]);

    expect(component.allSeriesSelected()).toBeTrue();
    expect(component.seriesSelectModel).toContain(component.ALL_SERIES);
  });

  it('goes back to the default selection when "select all" is unchecked', () => {
    component.onSeriesSelectionChange([0, 1, 2, 3, 4, component.ALL_SERIES]);
    component.onSeriesSelectionChange([0, 1, 2, 3, 4, 5, 6]);

    expect(component.seriesSelectionList).toEqual([0, 1, 2, 3, 4]);
    expect(component.seriesSelectModel).not.toContain(component.ALL_SERIES);
  });

  it('unchecks "select all" when one series is unselected', () => {
    component.onSeriesSelectionChange([0, 1, 2, 3, 4, component.ALL_SERIES]);
    component.onSeriesSelectionChange([component.ALL_SERIES, 0, 1, 2, 4, 5, 6]);

    expect(component.seriesSelectionList).toEqual([0, 1, 2, 4, 5, 6]);
    expect(component.allSeriesSelected()).toBeFalse();
    expect(component.seriesSelectModel).not.toContain(component.ALL_SERIES);
  });
});
