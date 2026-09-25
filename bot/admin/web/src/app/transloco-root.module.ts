import { NgModule } from '@angular/core';
import { TranslocoHttpLoader } from './transloco-loader';
import { environment } from '../environments/environment';
import { provideTransloco, TranslocoModule } from '@jsverse/transloco';
import { provideTranslocoMessageformat } from '@jsverse/transloco-messageformat';
@NgModule({
  exports: [TranslocoModule],
  providers: [
    provideTransloco({
      config: {
        availableLangs: ['en', 'fr'],
        defaultLang: 'en',
        fallbackLang: 'en',
        reRenderOnLangChange: true,
        prodMode: environment.production
      },
      loader: TranslocoHttpLoader
    }),
    provideTranslocoMessageformat()
  ]
})
export class TranslocoRootModule {}
