import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlaygroundComponent } from './playground.component';

describe('PlaygroundComponent', () => {
  let component: PlaygroundComponent;
  let fixture: ComponentFixture<PlaygroundComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaygroundComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PlaygroundComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
