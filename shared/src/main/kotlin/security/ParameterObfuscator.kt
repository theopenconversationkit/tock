package ai.tock.shared.security

interface ParameterObfuscator {

    /**
     * Obfuscate the parameter.
     */
    fun obfuscate(parameters: Map<String, String>): Map<String, String>
}