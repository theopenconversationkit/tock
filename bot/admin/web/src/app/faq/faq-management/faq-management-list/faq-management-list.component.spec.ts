/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbButtonModule, NbCardModule, NbDialogRef, NbIconModule, NbTagModule, NbToggleModule, NbTooltipModule } from '@nebular/theme';
import { of } from 'rxjs';

import { StateService } from '../../../core-nlp/state.service';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { FaqManagementListComponent } from './faq-management-list.component';
import { FaqDefinitionExtended } from '../faq-management.component';
import { DialogService } from '../../../core-nlp/dialog.service';

const mockFaqs: FaqDefinitionExtended[] = [
  {
    id: '1',
    language: 'fr',
    applicationName: '1',
    title: 'faq 1',
    utterances: ['question 1'],
    tags: [],
    answer: 'answer',
    enabled: true
  },
  {
    id: '2',
    language: 'fr',
    applicationName: '1',
    title: 'faq 2',
    utterances: ['question'],
    tags: [],
    answer: 'answer',
    enabled: true
  },
  {
    id: '3',
    language: 'fr',
    applicationName: '1',
    title: 'faq 3',
    description: 'description',
    utterances: ['question 1', 'question 2'],
    tags: ['tag 1', 'tag 2'],
    answer: 'answer',
    enabled: true
  },
  {
    id: '4',
    language: 'fr',
    applicationName: '1',
    title: 'faq 4',
    utterances: ['question 1'],
    tags: ['tag'],
    answer: 'answer',
    enabled: false
  }
];

describe('FaqManagementListComponent', () => {
  let component: FaqManagementListComponent;
  let fixture: ComponentFixture<FaqManagementListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqManagementListComponent],
      imports: [TestSharedModule, NbIconModule, NbCardModule, NbButtonModule, NbTagModule, NbToggleModule, NbTooltipModule],
      providers: [
        {
          provide: StateService,
          useValue: {
            currentApplication: { name: 'app' },
            currentLocale: 'fr',
            intentIdExistsInOtherApplication: () => false
          }
        },
        {
          provide: DialogService,
          useValue: { openDialog: () => ({ onClose: (val: any) => of(val) }) }
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FaqManagementListComponent);
    component = fixture.componentInstance;
    component.faqs = mockFaqs;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create as many entries as the list contains', () => {
    const listElement: HTMLElement = fixture.debugElement.nativeElement;

    expect(listElement.children).toHaveSize(mockFaqs.length);

    Array.from(listElement.children).forEach((child, i) => {
      const titleElement: HTMLElement = child.querySelector('[data-testid="title"]');
      expect(titleElement.textContent.trim()).toBe(mockFaqs[i].title);
    });
  });

  it('should add css indicator when a faq is selected', () => {
    component.selectedFaq = mockFaqs[1];
    fixture.detectChanges();
    const listElement: HTMLElement = fixture.debugElement.nativeElement;

    Array.from(listElement.children).forEach((child, i) => {
      if (i === 1) expect(child).toHaveClass('selected');
      else expect(child).not.toHaveClass('selected');
    });
  });

  it('should call the method when click on download button', () => {
    spyOn(component, 'download');

    const listElement: HTMLElement = fixture.debugElement.nativeElement;
    const buttonElement: HTMLButtonElement = Array.from(listElement.children)[0].querySelector('[data-testid="download"]');

    buttonElement.click();

    expect(component.download).toHaveBeenCalledOnceWith(mockFaqs[0]);
  });

  it('should emit the faq when clicking on the edit button of an item', () => {
    spyOn(component.onEdit, 'emit');
    const listElement: HTMLElement = fixture.debugElement.nativeElement;
    const buttonElement: HTMLButtonElement = Array.from(listElement.children)[0].querySelector('[data-testid="edit"]');

    buttonElement.click();

    expect(component.onEdit.emit).toHaveBeenCalledOnceWith(mockFaqs[0]);
  });

  describe('when click on the button delete faq', () => {
    it('should call the method', () => {
      spyOn(component, 'delete');

      const listElement: HTMLElement = fixture.debugElement.nativeElement;
      const buttonElement: HTMLButtonElement = Array.from(listElement.children)[0].querySelector('[data-testid="delete"]');

      buttonElement.click();

      expect(component.delete).toHaveBeenCalledOnceWith(mockFaqs[0]);
    });

    it('should emit faq when confirmation message is confirmed', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('delete') } as NbDialogRef<any>);
      spyOn(component.onDelete, 'emit');

      component.delete(mockFaqs[0]);

      expect(component.onDelete.emit).toHaveBeenCalledOnceWith(mockFaqs[0]);
    });

    it('should not emit faq when confirmation message is not confirmed', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
      spyOn(component.onDelete, 'emit');

      component.delete(mockFaqs[0]);

      expect(component.onDelete.emit).not.toHaveBeenCalledOnceWith(mockFaqs[0]);
    });
  });

  describe('when click on the toggle button to activate / deactivate faq', () => {
    it('should call the method', () => {
      spyOn(component, 'toggleEnabled');

      const listElement: HTMLElement = fixture.debugElement.nativeElement;
      const toggleElement: HTMLElement = Array.from(listElement.children)[0].querySelector('[data-testid="toggle"]');

      toggleElement.dispatchEvent(new Event('mousedown'));

      expect(component.toggleEnabled).toHaveBeenCalledOnceWith(mockFaqs[0]);
    });

    it('should emit faq to disable when confirmation message is confirmed', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('disable') } as NbDialogRef<any>);
      spyOn(component.onEnable, 'emit');

      component.toggleEnabled(mockFaqs[0]);

      expect(component.onEnable.emit).toHaveBeenCalledOnceWith(mockFaqs[0]);
    });

    it('should emit faq to enable when confirmation message is confirmed', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('enable') } as NbDialogRef<any>);
      spyOn(component.onEnable, 'emit');

      component.toggleEnabled(mockFaqs[3]);

      expect(component.onEnable.emit).toHaveBeenCalledOnceWith(mockFaqs[3]);
    });

    it('should not emit faq when confirmation message is not confirmed', () => {
      spyOn(component['dialogService'], 'openDialog').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
      spyOn(component.onEnable, 'emit');

      component.toggleEnabled(mockFaqs[0]);

      expect(component.onEnable.emit).not.toHaveBeenCalledOnceWith(mockFaqs[0]);
    });
  });
});
