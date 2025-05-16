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
import { By } from '@angular/platform-browser';
import { NbButtonModule, NbIconModule, NbTooltipModule } from '@nebular/theme';

import { FileUploadComponent } from './file-upload.component';
import { TestingModule } from '../../../../testing';

describe('FileUploadComponent', () => {
  let component: FileUploadComponent;
  let fixture: ComponentFixture<FileUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FileUploadComponent],
      imports: [TestingModule, NbButtonModule, NbIconModule, NbTooltipModule]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FileUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not render file type accepted helper when is undefined', () => {
    const helperElement = fixture.debugElement.query(By.css('[data-testid="file-type-accepted"]'));

    expect(helperElement).toBeFalsy();
  });

  it('should render file type accepted helper when is defined', () => {
    component.fileTypeAccepted = ['json', 'xml'];
    component.ngOnInit();
    fixture.detectChanges();
    const helperElement = fixture.debugElement.query(By.css('[data-testid="file-type-accepted"]'));

    expect(helperElement).toBeTruthy();
    expect(helperElement.nativeElement.textContent.trim().toLowerCase()).toBe('file types accepted: json, xml');
  });

  describe('@change', () => {
    it('should call the method when clicking on zone', () => {
      spyOn(component, 'emitFiles');
      const inputElement: HTMLElement = fixture.debugElement.query(By.css('[data-testid="input-file-zone"]')).nativeElement;
      const buttonElement: HTMLElement = fixture.debugElement.query(By.css('[data-testid="browse-for-file"]')).nativeElement;

      inputElement.dispatchEvent(new Event('change', { bubbles: true }));

      expect(component.emitFiles).toHaveBeenCalled();

      buttonElement.dispatchEvent(new Event('change', { bubbles: true }));

      expect(component.emitFiles).toHaveBeenCalled();
    });

    it('should populate the files list when the change event is called', () => {
      component.multiple = true;
      const input: HTMLInputElement = fixture.debugElement.query(By.css('[data-testid="input-file"]')).nativeElement;
      const mockFileList = new DataTransfer();
      const file1 = new File(['content'], 'file1.json');
      const file2 = new File(['content'], 'file2.json');

      mockFileList.items.add(file1);
      mockFileList.items.add(file2);
      input.files = mockFileList.files;
      input.dispatchEvent(new Event('change', { bubbles: true }));

      expect(component.files).toHaveSize(2);
      expect(component.files.includes(file1)).toBeTrue();
      expect(component.files.includes(file2)).toBeTrue();
    });

    it('should populate the files list with first file of selection when the change event is called and the multiple input is false', () => {
      component.multiple = false;
      const input: HTMLInputElement = fixture.debugElement.query(By.css('[data-testid="input-file"]')).nativeElement;
      const mockFileList = new DataTransfer();
      const file1 = new File(['content'], 'file1.json');
      const file2 = new File(['content'], 'file2.json');

      mockFileList.items.add(file1);
      mockFileList.items.add(file2);
      input.files = mockFileList.files;
      input.dispatchEvent(new Event('change', { bubbles: true }));

      expect(component.files).toHaveSize(1);
      expect(component.files.includes(file1)).toBeTrue();
      expect(component.files.includes(file2)).toBeFalse();
    });

    it('should replace file in the files list when the change event is called and the multiple input is false and the files list not empty', () => {
      component.multiple = false;
      const input: HTMLInputElement = fixture.debugElement.query(By.css('[data-testid="input-file"]')).nativeElement;
      const mockFileList = new DataTransfer();
      const file1 = new File(['content'], 'file1.json');
      const file2 = new File(['content'], 'file2.json');

      mockFileList.items.add(file1);
      input.files = mockFileList.files;
      input.dispatchEvent(new Event('change', { bubbles: true }));

      expect(component.files).toHaveSize(1);
      expect(component.files.includes(file1)).toBeTrue();
      expect(component.files.includes(file2)).toBeFalse();

      mockFileList.clearData();
      mockFileList.items.add(file2);
      input.files = mockFileList.files;
      input.dispatchEvent(new Event('change', { bubbles: true }));

      expect(component.files).toHaveSize(1);
      expect(component.files.includes(file1)).toBeFalse();
      expect(component.files.includes(file2)).toBeTrue();
    });

    it('should add new files in the files list without remove the current files', () => {
      component.multiple = true;
      const input: HTMLInputElement = fixture.debugElement.query(By.css('[data-testid="input-file"]')).nativeElement;
      const mockFileList = new DataTransfer();
      const file1 = new File(['content'], 'file1.json');
      const file2 = new File(['content'], 'file2.json');
      const file3 = new File(['content'], 'file3.json');

      mockFileList.items.add(file1);
      mockFileList.items.add(file2);
      input.files = mockFileList.files;
      input.dispatchEvent(new Event('change', { bubbles: true }));

      expect(component.files).toHaveSize(2);
      expect(component.files.includes(file1)).toBeTrue();
      expect(component.files.includes(file2)).toBeTrue();
      expect(component.files.includes(file3)).toBeFalse();

      mockFileList.clearData();
      mockFileList.items.add(file3);
      input.files = mockFileList.files;
      input.dispatchEvent(new Event('change', { bubbles: true }));

      expect(component.files).toHaveSize(3);
      expect(component.files.includes(file1)).toBeTrue();
      expect(component.files.includes(file2)).toBeTrue();
      expect(component.files.includes(file3)).toBeTrue();
    });

    it('should replace files in the files list when files are already present', () => {
      component.multiple = true;
      const input: HTMLInputElement = fixture.debugElement.query(By.css('[data-testid="input-file"]')).nativeElement;
      const mockFileList = new DataTransfer();
      const file1 = new File(['content'], 'file1.json');
      const file2 = new File(['content'], 'file2.json');
      const file3 = new File(['content'], 'file3.json');
      const replaceFile = new File(['content replaced'], 'file2.json');

      mockFileList.items.add(file1);
      mockFileList.items.add(file2);
      mockFileList.items.add(file3);
      input.files = mockFileList.files;
      input.dispatchEvent(new Event('change', { bubbles: true }));

      expect(component.files).toHaveSize(3);
      expect(component.files.includes(file1)).toBeTrue();
      expect(component.files.includes(file2)).toBeTrue();
      expect(component.files.includes(file3)).toBeTrue();

      mockFileList.clearData();
      mockFileList.items.add(replaceFile);
      input.files = mockFileList.files;
      input.dispatchEvent(new Event('change', { bubbles: true }));

      expect(component.files).toHaveSize(3);
      expect(component.files.includes(file1)).toBeTrue();
      expect(component.files.includes(file2)).toBeFalse();
      expect(component.files.includes(file3)).toBeTrue();
      expect(component.files.includes(replaceFile)).toBeTrue();
    });
  });

  it('@drop should call the method when drag on zone', () => {
    const rootElement: HTMLElement = fixture.debugElement.query(By.css('[data-testid="root"]')).nativeElement;
    spyOn(component, 'onDrop');

    rootElement.dispatchEvent(new Event('drop', { bubbles: true }));
    fixture.detectChanges();

    expect(component.onDrop).toHaveBeenCalled();
  });

  it('@dragover should add class when the mouse enters the element', () => {
    const rootElement = fixture.debugElement.query(By.css('[data-testid="root"]')).nativeElement;
    const inputElement = fixture.debugElement.query(By.css('[data-testid="input-file-zone"]')).nativeElement;

    expect(inputElement).not.toHaveClass('dragHover');

    rootElement.dispatchEvent(new Event('dragover', { bubbles: true }));
    fixture.detectChanges();

    expect(inputElement).toHaveClass('dragHover');
  });

  it('@dragleave should remove class when mouse leaves element', () => {
    const rootElement = fixture.debugElement.query(By.css('[data-testid="root"]')).nativeElement;
    const inputElement = fixture.debugElement.query(By.css('[data-testid="input-file-zone"]')).nativeElement;

    expect(inputElement).not.toHaveClass('dragHover');

    rootElement.dispatchEvent(new Event('dragover', { bubbles: true }));
    fixture.detectChanges();

    expect(inputElement).toHaveClass('dragHover');

    rootElement.dispatchEvent(new Event('dragleave', { bubbles: true }));
    fixture.detectChanges();

    expect(inputElement).not.toHaveClass('dragHover');
  });

  it('should create as many entries as the list contains', () => {
    const file1 = new File(['content'], 'file1.json');
    const file2 = new File(['content'], 'file2.json');
    const file3 = new File(['content'], 'file3.json');
    const files = [file1, file2, file3];
    component.files = files;
    fixture.detectChanges();
    const listElement: HTMLElement = fixture.debugElement.query(By.css('[data-testid="files"]')).nativeElement;

    expect(listElement.children).toHaveSize(files.length);

    Array.from(listElement.children).forEach((child, i) => {
      const titleElement: HTMLElement = child.querySelector('[data-testid="filename"]');
      expect(titleElement.textContent.trim()).toBe(files[i].name);
    });
  });

  it('#removeFile should remove element from the list', () => {
    const file1 = new File(['content'], 'file1.json');
    const file2 = new File(['content'], 'file2.json');
    const file3 = new File(['content'], 'file3.json');
    const files = [file1, file2, file3];
    component.files = files;

    expect(component.files).toHaveSize(3);

    component.removeFile('file1.json');

    expect(component.files).toHaveSize(2);
    expect(component.files.includes(file1)).toBeFalse();
  });
});
