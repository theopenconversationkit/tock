#  Copyright (C) 2017/2022 e-voyageurs technologies
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

from collections import defaultdict
import clyngor
import biseau

class BotAction:
    def __init__(self, uid, dependencies, products):
        self.uid = uid
        self.dependencies = tuple(dependencies)
        self.products = tuple(products)

def create_whole_graph_asp(current, bot_actions, target:BotAction=None, available_contexts=set(), ran_handlers=set()):
    atoms = [f'target("{target.uid}")'] if target else []
    if current:
        atoms.append(f'current("{current}")')
    # create the nodes and edges
    for action in bot_actions:
        for product in action.products:
            atoms.append(f'link("{action.uid}","{product}")')
        for dependence in action.dependencies:
            atoms.append(f'link("{dependence}","{action.uid}")')

        atoms.append(f'handler("{action.uid}")')  # that node is a handler
    # add metadata on nodes
    for context in available_contexts:
        atoms.append(f'enabled("{context.uid}")')  # that context is known
        if context.raw:
            atoms.append(f'raw("{context.uid}")')  # that context is raw
    for handler in ran_handlers:
        atoms.append(f'already_used("{handler}")')  # that handler was already ran before
    return ''.join(atom + '.\n' for atom in atoms)

def reduce_graph_asp(graph):
    answers = tuple(clyngor.solve(pythonScriptPath+'/action-reducer.lp', inline=graph))
    assert len(answers) == 1, answers
    ret = clyngor.utils.answer_set_to_str(answers[0], atom_end='.')
    return ret

def solve_graph_asp(graph, priorities):
    answers = clyngor.solve(pythonScriptPath+'/action-resolver.lp', options='--opt-mode=optN', inline=graph, delete_tempfile=False, constants=dict(priorities))
    #print('COMMAND:', answers.command)
    solution = None
    opt_answers = clyngor.opt_models_from_clyngor_answers(answers.by_predicate)
    solutions = []
    for idx, answer in enumerate(opt_answers, start=1):
        solution = tuple(e for e, in answer['exec'])
        #print(f'ANSWER {idx}:', solution)
        solutions.append(frozenset(s.strip('"') for s in solution))
    return solutions

# FIXME (WITH DERCBOT-321)
# debugEnabled, save_graph_asp, png
def callClyngor(debugEnabled, current, bot_actions, target:BotAction=None, available_contexts=set(), ran_handlers=set()):
    graph = create_whole_graph_asp(current, bot_actions, target, available_contexts, ran_handlers)
    if debugEnabled:
        save_graph_asp(graph, '/tmp/action-graph-full-new.png')
    graph = reduce_graph_asp(graph)
    if debugEnabled:
            save_graph_asp(graph, '/tmp/action-graph-reduced-new.png')
    candidate_uids = solve_graph_asp(graph, {
        'raw_context': 10,
        'branch_length': 8,
        'contexts_use': 6,
        'handler_new': 4,
        'handler_exec': 2,
        'profile': 1,
    })

    return [list(x)[0] for x in candidate_uids]

def save_graph_asp(graph:str, outfile:str):
    ASP_BISEAU_RULES = """
    node(X) :- link(X,_).  node(X) :- link(_,X).
    shape(H,rectangle):- handler(H).
    color(X,white) :- node(X) ; not handler(X) ; not enabled(X).
    color(X,green) :- enabled(X).
    color(X,green) :- already_used(X); not target(X); not current(X).
    color(X,cyan) :- current(X).
    color(X,red) :- target(X); not current(X).
    obj_property(edge,arrowhead,vee).
    % Representation of handler discrimation by the profile.
    annot(upper,H,L) :- discriminate(_,H,S,L) ; S>0.
    annot(lower,H,L) :- discriminate(_,H,S,L) ; S<0.
    annot(upper,H,labelfontcolor,"darkgreen") :- discriminate(_,H,S,_) ; S>0.
    annot(lower,H,labelfontcolor,"red") :- discriminate(_,H,S,_) ; S<0.
    """
    biseau.compile_to_single_image(graph + ASP_BISEAU_RULES, outfile=outfile)
    #print(f'ActionResolver: {outfile} saved.')
