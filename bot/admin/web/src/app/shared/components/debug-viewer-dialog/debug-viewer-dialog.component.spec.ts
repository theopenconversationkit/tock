import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';

import { DebugViewerDialogComponent } from './debug-viewer-dialog.component';

describe('DebugViewerComponent', () => {
  let component: DebugViewerDialogComponent;
  let fixture: ComponentFixture<DebugViewerDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DebugViewerDialogComponent],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {
            close: () => {}
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DebugViewerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
