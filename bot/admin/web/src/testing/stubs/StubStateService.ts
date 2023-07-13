import { Subject } from 'rxjs';

export class StubStateService {
  currentApplication = {
    namespace: 'namespace/test',
    name: 'test',
    _id: '1',
    supportedLocales: ['fr', 'en']
  };

  currentLocal = 'fr';

  configurationChange = new Subject();

  intentIdExistsInOtherApplication() {
    return false;
  }

  localeName(locale: 'fr' | 'en') {
    return locale === 'fr' ? 'french' : locale === 'en' ? 'english' : 'unknown locale';
  }
}
