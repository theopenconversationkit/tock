import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbAlertModule, NbIconModule } from '@nebular/theme';

import { NoBotConfigurationComponent } from './no-bot-configuration.component';
import { TestingModule } from '../../../../testing';

describe('NoBotConfigurationComponent', () => {
  let component: NoBotConfigurationComponent;
  let fixture: ComponentFixture<NoBotConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NoBotConfigurationComponent],
      imports: [TestingModule, NbAlertModule, NbIconModule]
    }).compileComponents();
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
