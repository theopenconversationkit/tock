package ai.tock.bot.admin.story

/**
 * A flow tree node.
 */
data class StoryDefinitionConfigurationFlowNode(
        val ownerStoryId: String,
        val childrenStoryIds: List<String>
)