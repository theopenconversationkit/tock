import { ScenarioVersion, ScenarioItem } from '../models';
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
  normalizedSnakeCaseUpper,
  removeSmStateById,
  renameSmStateById,
  revertTransformMatrix,
  stringifiedCleanObject
} from './utils';

describe('Scenarios Utils', () => {
  it('Should normalize a string', () => {
    const before = 'é&"(-è_çà)=æɛß,@ـسحـ \u0300 \u0301 \u0310 êôïö°';
    const after = 'e&"(-e_ca)=æɛß,@ـسحـ    eoio°';
    expect(normalize(before)).toEqual(after);
  });

  it('Should normalize and convert a string to snake case', () => {
    const before = ' test IF sNake-Case   FUNCTION \u0300 wörks°  ';
    const after = 'TEST_IF_SNAKE_CASE_FUNCTION_WORKS';
    expect(normalizedSnakeCaseUpper(before)).toEqual(after);
  });

  it('Should normalize and convert a string to camel case', () => {
    const before = ' test IF CamEl-case    function \u0300 WÔRKS°  ';
    const after = 'testIfCamelCaseFunctionWorks';
    expect(normalizedCamelCase(before)).toEqual(after);
  });

  it('Should remove expandos begining by an underscore and stringify an object', () => {
    const before = JSON.parse('{"data":{"scenarioItems":[{"intentDefinition":{"_sentences":"test"}}]},"__expando":"test"}');
    const after = '{"data":{"scenarioItems":[{"intentDefinition":{}}]}}';
    expect(stringifiedCleanObject(before as ScenarioVersion)).toEqual(after);
  });

  it('Should find the correct contrast shade for a given hex color', () => {
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

  it('Should return the inverse transformation matrix of an element whose parent has a transformation', () => {
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

    const expected = '{"x":10,"y":10,"width":10,"height":10,"top":10,"right":20,"bottom":20,"left":10}';
    expect(JSON.stringify(res)).toEqual(JSON.stringify(expected));
  });

  it('Should return the items of a scenario that are of type intent', () => {
    const scenario = {
      data: {
        scenarioItems: [{ from: 'bot' }, { from: 'client' }, { from: 'test' }, { from: 'client' }, { from: 'bot' }]
      }
    };

    const res = getScenarioIntents(scenario as ScenarioVersion);
    const expected = [{ from: 'client' }, { from: 'client' }];

    expect(res).toEqual(expected as ScenarioItem[]);
  });

  it('Should return the items definitions of a scenario that are of type intent and have intentDefinition', () => {
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

    const res = getScenarioIntentDefinitions(scenario as unknown as ScenarioVersion);
    const expected = [{ attr: 'test1' }, { attr: 'test2' }];

    expect(res).toEqual(expected as any);
  });

  it('Should return the items of a scenario that are of type action', () => {
    const scenario = {
      data: {
        scenarioItems: [{ from: 'bot' }, { from: 'client' }, { from: 'test' }, { from: 'client' }, { from: 'bot' }]
      }
    };

    const res = getScenarioActions(scenario as ScenarioVersion);
    const expected = [{ from: 'bot' }, { from: 'bot' }];

    expect(res).toEqual(expected as ScenarioItem[]);
  });

  it('Should return the items definitions of a scenario that are of type action and have actionDefinition', () => {
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

    const res = getScenarioActionDefinitions(scenario as unknown as ScenarioVersion);
    const expected = [{ attr: 'test1' }, { attr: 'test2' }];

    expect(res).toEqual(expected as any);
  });

  it('Should return a SM transition target by name', () => {
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

  it('Should return all SM transitions', () => {
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

  it('Should return all SM transitions names', () => {
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

  it('Should return all parent states of a given transition name', () => {
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

  it('Should return the name of all transitions pointing to a given state id as well as its parent state.', () => {
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

  it('Should rename a state and update its referencies', () => {
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

  it('Should remove a state and its referencies', () => {
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

  it('Should return a state by its id', () => {
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

  it('Should return the parent of a state by its id', () => {
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

  it('Should return the name of all states of a SM ', () => {
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

  it('Should return the name of all states in a SM that are not group states', () => {
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
