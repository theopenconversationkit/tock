// see https://github.com/akveo/nebular/issues/2554
import { Injectable } from '@angular/core';
import { NbFontIconPackParams, NbFontIcon, NbIcon, NbIconLibraries } from '@nebular/theme';

@Injectable()
export class CustomNbIconLibraries extends NbIconLibraries {
  protected createFontIcon(name: string, content: NbIcon | string, params: NbFontIconPackParams): NbFontIcon {
    if (content instanceof NbFontIcon) {
      return content;
    }
    return new NbFontIcon(name, params.ligature ? name : content, params);
  }
}
