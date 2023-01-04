import { of } from 'rxjs';

export class NbDialogServiceMock {
  open() {
    return {
      onClose: (val: any) => of(val)
    };
  }
}
