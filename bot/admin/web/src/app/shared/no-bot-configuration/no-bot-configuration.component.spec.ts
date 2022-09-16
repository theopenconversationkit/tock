import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NoBotConfigurationComponent } from './no-bot-configuration.component';

describe('NoBotConfigurationComponent', () => {
  let component: NoBotConfigurationComponent;
  let fixture: ComponentFixture<NoBotConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NoBotConfigurationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NoBotConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
