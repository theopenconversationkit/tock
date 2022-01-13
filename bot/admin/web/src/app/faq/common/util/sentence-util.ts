import { SentenceStatus } from "src/app/model/nlp";

// TODO: includes it in NLP part

export const statusAsText = (status: SentenceStatus) => {
  switch (status) {
    case SentenceStatus.deleted :
      return "Deleted";
    case SentenceStatus.inbox :
      return "Inbox";
    case SentenceStatus.model :
      return "Included in model";
    case SentenceStatus.validated :
      return "Validated";
  }
  return "unknown";
};

export const statusAsColor = (status: SentenceStatus) => {
  switch (status) {
    case SentenceStatus.deleted :
      return "red";
    case SentenceStatus.inbox :
      return "lightblue";
    case SentenceStatus.model :
      return "#00d68f";
    case SentenceStatus.validated :
      return "mediumspringgreen ";
  }
  return "orange";
};
