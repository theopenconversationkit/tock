/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.admin.kotlin.compiler

import com.google.common.collect.Lists
import com.intellij.codeInsight.ContainerProvider
import com.intellij.codeInsight.runner.JavaMainMethodProvider
import com.intellij.core.CoreApplicationEnvironment.registerExtensionPoint
import com.intellij.lang.ASTNode
import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.FileContextProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.psi.codeStyle.ChangedRangesInfo
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.Indent
import com.intellij.psi.impl.compiled.ClsCustomNavigationPolicy
import com.intellij.psi.meta.MetaDataContributor
import com.intellij.util.ThrowableRunnable
import mu.KotlinLogging
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import java.io.File
import java.nio.file.Path

/**
 *
 */
internal object EnvironmentManager {
    private val logger = KotlinLogging.logger {}
    var environment: KotlinCoreEnvironment? = null
    private val disposable = Disposable { }

    fun init(libraries: List<Path>) {
        environment = createEnvironment(libraries)
    }

    private fun createEnvironment(libraries: List<Path>): KotlinCoreEnvironment {
        val arguments = K2JVMCompilerArguments()
        val configuration = CompilerConfiguration()

        configuration.addJvmClasspathRoots(getClasspath(arguments, libraries))

        configuration.put(JVMConfigurationKeys.DISABLE_PARAM_ASSERTIONS, arguments.noParamAssertions)
        configuration.put(JVMConfigurationKeys.DISABLE_CALL_ASSERTIONS, arguments.noCallAssertions)
        configuration.put(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            object : MessageCollector {
                private var error: Boolean = false

                override fun clear() {
                    error = false
                }

                override fun hasErrors(): Boolean {
                    return error
                }

                override fun report(
                    severity: CompilerMessageSeverity,
                    message: String,
                    location: CompilerMessageSourceLocation?,
                ) {
                    logger.error { "$severity $message $location" }
                    error = true
                }
            },
        )

        configuration.put(CommonConfigurationKeys.MODULE_NAME, "tockScript")

        val environment =
            KotlinCoreEnvironment.createForProduction(
                disposable,
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES,
            )
        val project = environment.project as MockProject

        project.registerService(CodeStyleManager::class.java, DummyCodeStyleManager())

        registerExtensionPoints(Extensions.getRootArea())

        return environment
    }

    private fun registerExtensionPoints(area: ExtensionsArea) {
        registerExtensionPoint(area, FileContextProvider.EP_NAME, FileContextProvider::class.java)

        registerExtensionPoint(area, MetaDataContributor.EP_NAME, MetaDataContributor::class.java)
        registerExtensionPoint(area, PsiAugmentProvider.EP_NAME, PsiAugmentProvider::class.java)
        registerExtensionPoint(area, JavaMainMethodProvider.EP_NAME, JavaMainMethodProvider::class.java)

        registerExtensionPoint(area, ContainerProvider.EP_NAME, ContainerProvider::class.java)
        registerExtensionPoint(area, ClsCustomNavigationPolicy.EP_NAME, ClsCustomNavigationPolicy::class.java)
    }

    private fun getClasspath(
        arguments: K2JVMCompilerArguments,
        libraries: List<Path>,
    ): List<File> {
        val classpath = Lists.newArrayList<File>()
        // classpath.addAll(PathUtil.getJdkClassesRoots(File(CommonSettings.JAVA_HOME)))
        for (library in libraries) {
            classpath.add(library.toFile())
        }
        if (arguments.classpath != null) {
            val elements = arguments.classpath!!.split(File.pathSeparatorChar)
            for (element in elements) {
                classpath.add(File(element))
            }
        }
        return classpath
    }
}

private class DummyCodeStyleManager : CodeStyleManager() {
    override fun getProject(): Project {
        throw UnsupportedOperationException()
    }

    override fun reformat(psiElement: PsiElement): PsiElement {
        return psiElement
    }

    override fun reformat(
        psiElement: PsiElement,
        b: Boolean,
    ): PsiElement {
        return psiElement
    }

    override fun reformatRange(
        psiElement: PsiElement,
        i: Int,
        i1: Int,
    ): PsiElement {
        return psiElement
    }

    override fun reformatRange(
        psiElement: PsiElement,
        i: Int,
        i1: Int,
        b: Boolean,
    ): PsiElement {
        return psiElement
    }

    override fun reformatText(
        psiFile: PsiFile,
        i: Int,
        i1: Int,
    ) {
    }

    override fun reformatText(
        psiFile: PsiFile,
        collection: Collection<TextRange>,
    ) {
    }

    override fun reformatTextWithContext(
        psiFile: PsiFile,
        changedRangesInfo: ChangedRangesInfo,
    ) {
    }

    override fun reformatTextWithContext(
        psiFile: PsiFile,
        collection: Collection<TextRange>,
    ) {
    }

    override fun adjustLineIndent(
        psiFile: PsiFile,
        textRange: TextRange,
    ) {
    }

    override fun adjustLineIndent(
        psiFile: PsiFile,
        i: Int,
    ): Int {
        return i
    }

    override fun adjustLineIndent(
        document: Document,
        i: Int,
    ): Int {
        return i
    }

    override fun isLineToBeIndented(
        psiFile: PsiFile,
        i: Int,
    ): Boolean {
        return false
    }

    override fun getLineIndent(
        psiFile: PsiFile,
        i: Int,
    ): String? {
        return null
    }

    override fun getLineIndent(
        document: Document,
        i: Int,
    ): String? {
        return null
    }

    override fun getIndent(
        s: String,
        fileType: FileType,
    ): Indent? {
        return null
    }

    override fun fillIndent(
        indent: Indent,
        fileType: FileType,
    ): String? {
        return null
    }

    override fun zeroIndent(): Indent? {
        return null
    }

    override fun reformatNewlyAddedElement(
        astNode: ASTNode,
        astNode1: ASTNode,
    ) {
    }

    override fun isSequentialProcessingAllowed(): Boolean {
        return false
    }

    override fun performActionWithFormatterDisabled(runnable: Runnable) {
        runnable.run()
    }

    override fun <T : Throwable> performActionWithFormatterDisabled(throwableRunnable: ThrowableRunnable<T>) {
        throwableRunnable.run()
    }

    override fun <T> performActionWithFormatterDisabled(computable: Computable<T>): T {
        return computable.compute()
    }
}
