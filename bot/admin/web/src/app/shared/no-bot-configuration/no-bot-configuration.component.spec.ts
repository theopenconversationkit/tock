import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbAlertModule, NbIconModule } from '@nebular/theme';

import { TestSharedModule } from '../test-shared.module';

import { NoBotConfigurationComponent } from './no-bot-configuration.component';

describe('NoBotConfigurationComponent', () => {
  let component: NoBotConfigurationComponent;
  let fixture: ComponentFixture<NoBotConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NoBotConfigurationComponent],
      imports: [TestSharedModule, NbAlertModule, NbIconModule]
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
