import { IndexingSession, ProcessAdvancement, Source } from '../models';

export function getSourceMostRecentRunningIndexingSession(source: Source): IndexingSession {
  const runningSessions = source.indexing_sessions?.filter((is) =>
    [ProcessAdvancement.pristine, ProcessAdvancement.running].includes(is.status)
  );
  if (runningSessions?.length) {
    runningSessions.sort((a, b) => {
      return b.start_date.valueOf() - a.start_date.valueOf();
    });
    return runningSessions[0];
  }
}
