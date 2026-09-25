import { BotHistoryComponent } from './bot-history.component';
import { BotHistoryEvent, BotHistoryEventType } from '../../models/dashboard.model';

describe('BotHistoryComponent creation date', () => {
  const component = new BotHistoryComponent();
  const created: BotHistoryEvent = { id: 'bot-id', date: '2026-02-26T08:32:23Z', type: BotHistoryEventType.created };

  it('explains estimated creation dates', () => {
    const event = { ...created, estimated: true };
    expect(component.labelKey(event)).toBe('dashboard.history.event.created.estimatedLabel');
    expect(component.detailKey(event)).toBe('dashboard.history.event.created.estimatedDetail');
    expect(component.hasDetail(event)).toBeTrue();
  });

  it('preserves recorded creation labels', () => {
    expect(component.labelKey(created)).toBe('dashboard.history.event.created.label');
    expect(component.hasDetail(created)).toBeFalse();
  });
});
