import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';

import { DebugViewerComponent } from './debug-viewer.component';

describe('DebugViewerComponent', () => {
  let component: DebugViewerComponent;
  let fixture: ComponentFixture<DebugViewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DebugViewerComponent],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {
            close: () => {}
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DebugViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
