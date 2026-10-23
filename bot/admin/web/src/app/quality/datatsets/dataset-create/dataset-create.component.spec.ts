import { of } from 'rxjs';
import { DatasetCreateComponent } from './dataset-create.component';
import { Dataset } from '../models';

describe('DatasetCreateComponent question identity', () => {
  let component: DatasetCreateComponent;
  let service: jasmine.SpyObj<any>;

  beforeEach(() => {
    service = jasmine.createSpyObj('DatasetsService', ['updateDataset']);
    service.updateDataset.and.returnValue(of({}));
    component = new DatasetCreateComponent({ close: () => {} } as any, null, service, null, { translate: (key) => key } as any);
    component.dataset = {
      id: 'dataset',
      name: 'Dataset',
      description: '',
      runs: [],
      questions: [
        { id: 'a', question: 'First question' },
        { id: 'b', question: 'Second question' },
        { id: 'c', question: 'Third question' }
      ]
    } as Dataset;
    component.ngOnInit();
  });

  afterEach(() => component.ngOnDestroy());

  it('preserves IDs after removing an earlier question and adding a new one', () => {
    component.removeQuestion(0);
    component.questions.at(2).controls.question.setValue('New question');
    component.onQuestionInput(2);
    component.submit();

    const questions = service.updateDataset.calls.mostRecent().args[1].questions;
    expect(questions.map((question) => question.id)).toEqual(['b', 'c', undefined]);
    expect(questions.map((question) => question.question)).toEqual(['Second question', 'Third question', 'New question']);
  });

  it('uses stable IDs for the edit warning after a deletion', () => {
    component.removeQuestion(0);
    component.onQuestionInput(0);
    expect(component.showQuestionEditWarning).toBeFalse();
    component.questions.at(0).controls.question.setValue('Changed question');
    component.onQuestionInput(0);
    expect(component.showQuestionEditWarning).toBeTrue();
  });
});
