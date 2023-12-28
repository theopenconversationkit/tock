import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DebugViewerWindowComponent } from './debug-viewer-window.component';

describe('DebugViewerWindowComponent', () => {
  let component: DebugViewerWindowComponent;
  let fixture: ComponentFixture<DebugViewerWindowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DebugViewerWindowComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DebugViewerWindowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
