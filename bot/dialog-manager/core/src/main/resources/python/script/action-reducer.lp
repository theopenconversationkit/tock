% Implementation of the action reducer logic in Answer Set Programming.

% Inputs:
%    - target(X): X is the targeted node.
%    - link(X,Y): there is a link from node X to node Y.
%    - enabled(X): node X is enabled.
%    - raw(X): node X is a raw context.
%    - handler(X): node X is a handler (possibly the target one).
%    - already_used(H): H is an handler already sent to user.


% The target is relevant.
relevant(T):- target(T).

% Any predecessor of a relevant node is relevant itself.
relevant(N):- relevant(K) ; link(N,K).

% outputs the relevant part of the graph.
#show.
#show target/1.
#show link(X,Y): link(X,Y), relevant(X), relevant(Y).
#show enabled(X): enabled(X), relevant(X).
#show raw(X): raw(X), relevant(X).
#show handler(X): handler(X), relevant(X).
#show node(X): relevant(X).  % show single nodes in biseau
#show discriminate(P,H,N,L): discriminate(P,H,N,L), relevant(H).
#show already_used(H): already_used(H), relevant(H).
