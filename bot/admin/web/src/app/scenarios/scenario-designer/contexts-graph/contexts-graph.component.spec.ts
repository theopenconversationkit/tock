import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContextsGraphComponent } from './contexts-graph.component';

describe('ContextsGraphComponent', () => {
  let component: ContextsGraphComponent;
  let fixture: ComponentFixture<ContextsGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ContextsGraphComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextsGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
