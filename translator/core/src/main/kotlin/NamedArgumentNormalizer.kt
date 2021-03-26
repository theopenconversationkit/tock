package ai.tock.translator

import mu.KotlinLogging

internal object NamedArgumentNormalizer {

    private val logger = KotlinLogging.logger {}
    private val enrichedArgPattern = "\\{:([a-zA-Z_]+)\\}".toRegex()
    private val vanillaArgPattern = "\\{[0-9]+\\}".toRegex()

    /**
     * @param label e.g: "{:arg_name} {0} {:customName}"
     * @return messageFormat label and compatible arguments
     */
    fun normalize(label: String, args: List<Any?> = emptyList()): NamedArgResult {
        try {
            val argNames = enrichedArgPattern.findAll(label)
                .sortedBy { it.range.first }
                .map { it.groupValues }
                .map { it.last() }
                .toList()

            if (argNames.isEmpty()) {
                return NamedArgResult(label, args)
            }

            val enrichedArgs = args.filterIsInstance<Pair<String, Any?>>()
            val vanillaArgs = args.subtract(enrichedArgs).toList()
            val newArgsOffset = vanillaArgPattern.findAll(label).count()

            val argsMap = enrichedArgs.toMap()

            return NamedArgResult(
                argNames.foldIndexed(label) { index, p, arg ->
                    p.replace(
                        "{:$arg}",
                        "{${index + newArgsOffset}}"
                    )
                },
                vanillaArgs + argNames.map { argsMap[it] ?: ":$it" }
            )
        } catch (e: Exception) {
            logger.error("error with $label and $args", e)
            return NamedArgResult(label, args)
        }
    }
}

internal data class NamedArgResult(val label: String, val args: List<Any?>)
