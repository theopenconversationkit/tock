% Implementation of the action resolver logic in Answer Set Programming.
% Given a BotAction graph, a target and currently activated contexts,
%  yield the best candidate handler(s) for execution.
% «Best» is defined by 4 optimizations.

% Inputs:
%    - target(X): X is the targeted node.
%    - link(X,Y): there is a link from node X to node Y.
%    - enabled(X): node X is enabled.
%    - raw(X): node X is a raw context.
%    - handler(X): node X is a handler (possibly the target one).
%    - already_used(H): H is an handler already sent to user.
% Constants:  define the priority of each of the four optimizations.
#const priority_raw_context=10.
#const priority_branch_length=8.
#const priority_contexts_use=6.
#const priority_handler_new=4.
#const priority_handler_exec=2.
#const priority_profile=1.


% contexts and handlers alike.
node(N) :- link(N,_).
node(N) :- link(_,N).
node(N) :- handler(N).  % necessary ?


% Only already runnable handlers are choosable, i.e
choosable_handler(H) :- handler(H)
    ; enabled(C): link(C,H)      % if all its dependencies are enabled, and
    ; not enabled(P): link(H,P)  % there is at least one output we need from it.
.

% Choose any number of handlers to execute.
1 { exec(H) : choosable_handler(H) }.

% Enabled contexts used by choosen handler are already reached.
reached(C) :- enabled(C) ; link(C,H) ; exec(H).

% if executed, an handler is reached/enabled.
reached(H) :- exec(H).

% An enabled handler enables its products.
reached(P) :- handler(H) ; reached(H) ; link(H,P).

% A handler is enabled if *all* of its predecessors (contexts) are enabled,
%  and if it is not a free context.
reached(H) :- reached(C): link(C,H) ; handler(H) ; link(_,H).

% Discard models that do not reach targets.
:- not reached(T) ; target(T).


% Maximize the use of used raw contexts.
nb(raw_context,N) :- N={reached(R): raw(R)}.
#maximize{X@priority_raw_context: nb(raw_context,X)}.

% Maximize the number of used contexts.
nb(context,N) :- N={reached(C): node(C), not handler(C)}.
#maximize{X@priority_contexts_use: nb(context,X)}.

% Minimize the number of handlers to execute.
nb(handlers,N) :- N={exec(_)}.
#minimize{X@priority_handler_exec: nb(handlers,X)}.

% Minimize the number of handlers to be ran.
topo(T,0):- target(T).
topo(H,L) :- link(H,C) ; link(C,S) ; topo(S,L-1) ; L < {handler(_)}.
nb(branch_length,L) :- L=#min{T: topo(H,T)} ; exec(H).
#minimize{L@priority_branch_length: nb(branch_length,L)}.

% Optimize the number of profile-discriminated bot actions.
nb(profile_affinity,A) :- A=#sum{N,H,T: exec(H), discriminate(T,H,N,_)}.
#maximize{N@priority_profile: nb(profile_affinity,N)}.

% Minimize the number of already performed bot action.
nb(handler_reused,N) :- N={reached(H): handler(H), already_used(H)}.
#minimize{N@priority_handler_new: nb(handler_reused,N)}.


#show.
#show nb/2.
#show exec/1.
