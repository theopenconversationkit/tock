import { StoryDefinitionConfiguration } from '../../bot/model/story';

export function getStoryIcon(story: StoryDefinitionConfiguration): string {
  if (story.isBuiltIn()) {
    return 'cube';
  }
  if (story.isSimpleAnswer()) {
    return 'chat-left';
  }
  if (story.isScriptAnswer()) {
    return 'code';
  }
}
