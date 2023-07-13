import { of } from 'rxjs';

export class StubNbDialogService {
  open() {
    return {
      onClose: (val: any) => of(val)
    };
  }
}
