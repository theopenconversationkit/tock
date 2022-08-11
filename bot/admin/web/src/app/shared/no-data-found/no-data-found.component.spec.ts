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
    const titleElement: HTMLTitleElement = fixture.debugElement.query(By.css('[data-testid="title"]')).nativeElement;

    expect(titleElement.textContent.toLowerCase().trim()).toBe('no data found');
  });

  it('should display custom title if the title input is change', () => {
    const title = 'custom title';
    const titleElement: HTMLTitleElement = fixture.debugElement.query(By.css('[data-testid="title"]')).nativeElement;
    component.title = title;
    fixture.detectChanges();

    expect(titleElement.textContent.toLowerCase().trim()).toBe(title);
  });

  it('should display message if the input is inform', () => {
    const message = 'message to display';
    component.message = undefined;
    fixture.detectChanges();
    let messageElement = fixture.debugElement.query(By.css('[data-testid="message"]'));

    expect(messageElement).toBeFalsy();

    component.message = message;
    fixture.detectChanges();
    messageElement = fixture.debugElement.query(By.css('[data-testid="message"]'));

    expect(messageElement.nativeElement.textContent.toLowerCase().trim()).toBe(message);
  });

  it('should display logo of robot', () => {
    const imageElement: HTMLImageElement = fixture.debugElement.query(By.css('[data-testid="logo"]')).nativeElement;

    expect(imageElement).toBeTruthy();
    expect(imageElement).toHaveClass('tock-robot');
  });
});
