import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbCardModule } from '@nebular/theme';

import { NoDataFoundComponent } from './no-data-found.component';
import { TestSharedModule } from '../../shared/test-shared.module';

describe('NoDataFoundComponent', () => {
  let component: NoDataFoundComponent;
  let fixture: ComponentFixture<NoDataFoundComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NoDataFoundComponent],
      imports: [TestSharedModule, NbCardModule]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NoDataFoundComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display default title if the title input is not change', () => {
    const titleElement: HTMLElement = fixture.debugElement.query(By.css('h2')).nativeElement;

    expect(titleElement.textContent.toLowerCase()).toBe('no data found');
  });

  it('should display custom title if the title input is change', () => {
    const title = 'custom title';
    const titleElement: HTMLElement = fixture.debugElement.query(By.css('h2')).nativeElement;
    component.title = title;
    fixture.detectChanges();

    expect(titleElement.textContent.toLowerCase()).toBe(title);
  });

  it('should display message if the input is inform', () => {
    const message = 'message to display';
    component.message = undefined;
    fixture.detectChanges();
    let messageElement = fixture.debugElement.query(By.css('p'));

    expect(messageElement).toBeFalsy();

    component.message = message;
    fixture.detectChanges();
    messageElement = fixture.debugElement.query(By.css('p'));

    expect(messageElement.nativeElement.textContent.toLowerCase()).toBe(message);
  });

  it('should display logo of robot', () => {
    const imageElement: HTMLElement = fixture.debugElement.query(By.css('img')).nativeElement;

    expect(imageElement).toBeTruthy();
    expect(imageElement).toHaveClass('tock-robot');
  });
});
