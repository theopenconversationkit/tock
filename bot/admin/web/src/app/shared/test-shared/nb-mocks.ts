// src/app/shared/test-shared/nb-mocks.ts
import { of } from 'rxjs';
import { NbDialogService, NbDialogRef, NbToastrService, NbWindowService } from '@nebular/theme';

export function getNbDialogRefMock() {
  return { onClose: of(null), close: () => {}, componentRef: {} } as unknown as NbDialogRef<any>;
}

export function getNbTestProviders() {
  return [
    { provide: NbDialogService, useValue: { open: () => getNbDialogRefMock() } },
    {
      provide: NbToastrService,
      useValue: {
        show: () => {},
        success: () => {},
        info: () => {},
        warning: () => {},
        primary: () => {},
        danger: () => {},
        default: () => {},
        control: () => {}
      }
    },
    { provide: NbWindowService, useValue: { open: () => ({ close: () => {} }) } }
  ];
}
