export class StateServiceMock {
  currentApplication = {
    namespace: 'namespace/test',
    name: 'test',
    _id: '1'
  };

  currentLocal = 'fr';

  intentIdExistsInOtherApplication() {
    return false;
  }
}
