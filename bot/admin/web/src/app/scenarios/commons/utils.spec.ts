import { Scenario, ScenarioData, ScenarioItem, SCENARIO_STATE } from '../models';
import {
  getAllSmNonGroupStatesNames,
  getAllSmStatesNames,
  getAllSmTransitionNames,
  getAllSmTransitions,
  getContrastYIQ,
  getScenarioActionDefinitions,
  getScenarioActions,
  getScenarioIntentDefinitions,
  getScenarioIntents,
  getSmStateById,
  getSmStateParentById,
  getSmTransitionByName,
  getSmTransitionParentByTarget,
  getSmTransitionParentsByname,
  normalize,
  normalizedCamelCase,
  normalizedSnakeCase,
  removeSmStateById,
  renameSmStateById,
  revertTransformMatrix,
  stringifiedCleanScenario
} from './utils';

describe('Scenarios Utils', () => {
  it('normalize', () => {
    const before = 'é&"(-è_çà)=æɛß,@ـسحـ \u0300 \u0301 \u0310 êôïö°';
    const after = 'e&"(-e_ca)=æɛß,@ـسحـ    eoio°';
    expect(normalize(before)).toEqual(after);
  });

  it('normalizedSnakeCase', () => {
    const before = ' test IF sNake-Case   FUNCTION \u0300 wörks°  ';
    const after = 'TEST_IF_SNAKE_CASE_FUNCTION_WORKS';
    expect(normalizedSnakeCase(before)).toEqual(after);
  });

  it('normalizedCamelCase', () => {
    const before = ' test IF CamEl-case    function \u0300 WÔRKS°  ';
    const after = 'testIfCamelCaseFunctionWorks';
    expect(normalizedCamelCase(before)).toEqual(after);
  });

  it('stringifiedCleanScenario', () => {
    const before = JSON.parse(
      '{"data":{"scenarioItems":[{"intentDefinition":{"_sentences":"test"}}]},"__expando":"test"}'
    );
    const after = '{"data":{"scenarioItems":[{"intentDefinition":{}}]}}';
    expect(stringifiedCleanScenario(before as Scenario)).toEqual(after);
  });

  it('getContrastYIQ', () => {
    let before = '#ff6600';
    let after = 'black';
    expect(getContrastYIQ(before)).toEqual(after);
    before = '#00000test';
    after = 'white';
    expect(getContrastYIQ(before)).toEqual(after);
    before = 'FFFFFF';
    after = 'black';
    expect(getContrastYIQ(before)).toEqual(after);
  });

  it('revertTransformMatrix', () => {
    const parent = document.createElement('div');
    parent.style.width = '20px';
    parent.style.height = '20px';
    const elem = document.createElement('div');
    elem.style.width = '10px';
    elem.style.height = '10px';
    parent.appendChild(elem);
    document.body.insertBefore(parent, document.body.firstChild);
    parent.style.transform = `translate(10px,10px) scale(0.5,0.5)`;

    const res = revertTransformMatrix(elem, parent);

    const expected =
      '{"x":10,"y":10,"width":10,"height":10,"top":10,"right":20,"bottom":20,"left":10}';
    expect(JSON.stringify(res)).toEqual(JSON.stringify(expected));
  });

  it('getScenarioIntents', () => {
    const scenario = {
      data: {
        scenarioItems: [
          { from: 'bot' },
          { from: 'client' },
          { from: 'test' },
          { from: 'client' },
          { from: 'bot' }
        ]
      }
    };

    const res = getScenarioIntents(scenario as Scenario);
    const expected = [{ from: 'client' }, { from: 'client' }];

    expect(res).toEqual(expected as ScenarioItem[]);
  });

  it('getScenarioIntentDefinitions', () => {
    const scenario = {
      data: {
        scenarioItems: [
          { from: 'bot' },
          { from: 'client' },
          { from: 'client', tickActionDefinition: { attr: 'test' } },
          { from: 'bot', intentDefinition: { attr: 'test' } },
          { from: 'client', intentDefinition: { attr: 'test1' } },
          { from: 'client', intentDefinition: { attr: 'test2' } },
          { from: 'bot' }
        ]
      }
    };

    const res = getScenarioIntentDefinitions(scenario as unknown as Scenario);
    const expected = [{ attr: 'test1' }, { attr: 'test2' }];

    expect(res).toEqual(expected as any);
  });

  it('getScenarioActions', () => {
    const scenario = {
      data: {
        scenarioItems: [
          { from: 'bot' },
          { from: 'client' },
          { from: 'test' },
          { from: 'client' },
          { from: 'bot' }
        ]
      }
    };

    const res = getScenarioActions(scenario as Scenario);
    const expected = [{ from: 'bot' }, { from: 'bot' }];

    expect(res).toEqual(expected as ScenarioItem[]);
  });

  it('getScenarioActionDefinitions', () => {
    const scenario = {
      data: {
        scenarioItems: [
          { from: 'bot', tickActionDefinition: { attr: 'test1' } },
          { from: 'client', tickActionDefinition: { attr: 'test' } },
          { from: 'bot', intentDefinition: { attr: 'test' } },
          { from: 'client', intentDefinition: { attr: 'test' } },
          { from: 'client', intentDefinition: { attr: 'test' } },
          { from: 'bot', tickActionDefinition: { attr: 'test2' } },
          { from: 'bot' }
        ]
      }
    };

    const res = getScenarioActionDefinitions(scenario as unknown as Scenario);
    const expected = [{ attr: 'test1' }, { attr: 'test2' }];

    expect(res).toEqual(expected as any);
  });

  it('getSmTransitionByName', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          on: {
            helloBot: '#HELLO_CLIENT',
            TEST: '#TEST',
            Test: '#TESTOK'
          }
        }
      },
      on: {}
    };

    const res = getSmTransitionByName('Test', machine);
    const expected = '#TESTOK';

    expect(res).toEqual(expected as any);
  });

  it('getAllSmTransitions', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          on: {
            TEST: '#TEST2',
            Test: '#TEST3'
          }
        }
      },
      on: { start: '#TEST1' }
    };
    const res = getAllSmTransitions(machine);
    const expected = [
      ['start', '#TEST1'],
      ['TEST', '#TEST2'],
      ['Test', '#TEST3']
    ];

    expect(res).toEqual(expected as any);
  });

  it('getAllSmTransitionNames', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          on: {
            TEST: '#TEST2',
            Test: '#TEST3'
          }
        }
      },
      on: { start: '#TEST1' }
    };

    const res = getAllSmTransitionNames(machine);
    const expected = ['start', 'TEST', 'Test'];

    expect(res).toEqual(expected);
  });

  it('getSmTransitionParentsByname', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          states: {
            subMachine: { id: 'subMachine', on: { Test: '#TEST' } }
          },
          on: {
            TEST: '#TEST2',
            Test: '#TEST3'
          }
        }
      },
      on: { start: '#TEST1' }
    };

    const res = getSmTransitionParentsByname('Test', machine);

    const expected = [
      {
        id: 'Global',
        states: {
          subMachine: { id: 'subMachine', on: { Test: '#TEST' } }
        },
        on: {
          TEST: '#TEST2',
          Test: '#TEST3'
        }
      },
      { id: 'subMachine', on: { Test: '#TEST' } }
    ];

    expect(res).toEqual(expected);
  });

  it('getSmTransitionParentByTarget', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          states: {
            TEST: {
              id: 'TEST'
            }
          },
          on: {
            Test1: '#TEST',
            Test2: '#TEST'
          }
        }
      },
      on: { test: '#Global' }
    };
    const res = getSmTransitionParentByTarget('TEST', machine);

    const expected = {
      parent: machine.states.Global,
      intents: ['Test1', 'Test2']
    };

    expect(res).toEqual(expected);
  });

  it('renameSmStateById', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          states: {
            TEST: {
              id: 'TEST'
            }
          },
          on: {
            Test1: '#TEST',
            Test2: '#TEST'
          }
        }
      },
      on: { test: '#Global' }
    };

    renameSmStateById('TEST', 'TEST2', machine);

    const expected = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          states: {
            TEST2: {
              id: 'TEST2'
            }
          },
          on: {
            Test1: '#TEST2',
            Test2: '#TEST2'
          }
        }
      },
      on: { test: '#Global' }
    };

    expect(machine).toEqual(expected as any);
  });

  it('removeSmStateById', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          states: {
            TEST: {
              id: 'TEST'
            }
          },
          on: {
            Test1: '#TEST',
            Test2: '#TEST'
          }
        }
      },
      on: { test: '#Global' }
    };

    removeSmStateById('TEST', machine);

    const expected = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          states: {},
          on: {}
        }
      },
      on: { test: '#Global' }
    };

    expect(machine).toEqual(expected as any);
  });

  it('getSmStateById', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          states: {
            TEST: {
              id: 'TEST'
            }
          }
        }
      }
    };

    const res = getSmStateById('TEST', machine);

    const expected = { id: 'TEST' };

    expect(res).toEqual(expected);
  });

  it('getSmStateParentById', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          states: {
            TEST: {
              id: 'TEST'
            }
          }
        }
      }
    };

    const res = getSmStateParentById('TEST', machine);

    const expected = {
      id: 'Global',
      states: {
        TEST: {
          id: 'TEST'
        }
      }
    };

    expect(res).toEqual(expected);
  });

  it('getAllSmStatesNames', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          states: {
            TEST: {
              id: 'TEST'
            }
          }
        }
      }
    };

    const res = getAllSmStatesNames(machine);

    const expected = ['root', 'Global', 'TEST'];

    expect(res).toEqual(expected);
  });

  it('getAllSmNonGroupStatesNames', () => {
    const machine = {
      id: 'root',
      states: {
        Global: {
          id: 'Global',
          states: {
            TEST: {
              id: 'TEST'
            },
            TEST2: {
              id: 'TEST2',
              states: {}
            }
          }
        }
      }
    };

    const res = getAllSmNonGroupStatesNames(machine);

    const expected = ['TEST'];

    expect(res).toEqual(expected);
  });
});
