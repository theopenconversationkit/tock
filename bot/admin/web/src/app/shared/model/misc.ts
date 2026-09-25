export enum SortOrder {
  ASC = 'ASC',
  DESC = 'DESC'
}

export interface SortOption {
  labelKey: string;
  value: SortOrder;
}

export const SortOrders: SortOption[] = [
  { labelKey: 'common.sort.ascending', value: SortOrder.ASC },
  { labelKey: 'common.sort.descending', value: SortOrder.DESC }
];
