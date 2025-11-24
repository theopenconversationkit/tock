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

import ai.tock.bot.admin.kotlin.compiler.EnvironmentManager.environment
import ai.tock.shared.error
import ai.tock.shared.listProperty
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.testFramework.LightVirtualFile
import mu.KotlinLogging
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.cli.jvm.compiler.CliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.container.getService
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.MainFunctionDetector
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.LazyTopDownAnalyzer
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Collections
import java.util.stream.Collectors

/**
 * Kotlin source code compiler.
 */
internal object KotlinCompiler {
    private val logger = KotlinLogging.logger {}

    fun init(classPath: List<String> = listProperty("tock_kotlin_compiler_classpath", emptyList())) {
        try {
            logger.info { "Init KotlinCompiler" }
            val paths = mutableListOf<Path>()
            loadPathFromClassLoader(KotlinCompiler::class.java.classLoader, paths, classPath)
            EnvironmentManager.init(paths)
            logger.info { "End KotlinCompiler initialization" }
        } catch (t: Throwable) {
            logger.error(t)
        }
    }

    private fun loadPathFromClassLoader(
        classLoader: ClassLoader,
        paths: MutableList<Path>,
        classPath: List<String>,
    ) {
        logger.debug { "load paths from $classLoader" }
        val cp = System.getProperty("java.class.path")
        logger.debug { "classpath: $cp" }
        // java 8
        paths.addAll(
            (classLoader as? URLClassLoader)
                ?.urLs
                ?.map {
                    logger.debug { "load url $it" }
                    Paths.get(it.toURI())
                }
                // java 9
                ?: (classPath + cp.split(File.pathSeparator))
                    .flatMap {
                        Paths.get(it).let {
                            if (Files.isDirectory(it)) {
                                Files.list(it).filter {
                                    it.toString().endsWith(".jar")
                                }.collect(Collectors.toList()) + listOf(it)
                            } else {
                                listOf(it)
                            }
                        }
                    }
                    .distinct()
                    .apply { logger.info { "class path used : $this" } },
        )
    }

    fun getErrors(files: Map<String, String>): Map<String, List<CompileError>> {
        val psiFiles = createPsiFiles(files)
        val analyzer = ErrorAnalyzer(psiFiles, EnvironmentManager.environment!!.project)
        return analyzer.getAllErrors()
    }

    fun compileCorrectFiles(
        projectFiles: Map<String, String>,
        fileName: String,
        searchForMain: Boolean,
    ): CompilationResult {
        val files = createPsiFiles(projectFiles)
        return compile(files, environment!!.project, environment!!.configuration, fileName, searchForMain)
    }

    private fun compile(
        currentPsiFiles: List<KtFile>,
        currentProject: Project,
        configuration: CompilerConfiguration,
        fileName: String,
        searchForMain: Boolean,
    ): CompilationResult {
        val generationState = getGenerationState(currentPsiFiles, currentProject, configuration)
        val context = getBindingContext(currentPsiFiles, currentProject)
        JvmIrCodegenFactory(configuration).convertAndGenerate(currentPsiFiles, generationState, context)
        val mainClass = findMainClass(context, currentPsiFiles, fileName, searchForMain)

        val factory = generationState.factory
        val files = HashMap<String, ByteArray>()
        for (file in factory.asList()) {
            files[file.relativePath] = file.asByteArray()
        }
        return CompilationResult(files, mainClass)
    }

    private fun getGenerationState(
        files: List<KtFile>,
        project: Project,
        compilerConfiguration: CompilerConfiguration,
    ): GenerationState {
        val analyzeExhaust = analyzeFileForJvm(files, project).getFirst()
        return GenerationState(
            project,
            analyzeExhaust.moduleDescriptor,
            compilerConfiguration,
            ClassBuilderFactories.BINARIES,
        )
    }

    private fun findMainClass(
        bindingContext: BindingContext,
        files: List<KtFile>,
        fileName: String,
        searchForMain: Boolean,
    ): String {
        val mainFunctionDetector = MainFunctionDetector(bindingContext, LanguageVersionSettingsImpl.DEFAULT)
        for (file in files) {
            if (file.name.contains(fileName)) {
                if (!searchForMain ||
                    file.declarations.any {
                        it is KtNamedFunction &&
                            try {
                                mainFunctionDetector.isMain(it)
                            } catch (e: Exception) {
                                false
                            }
                    }
                ) {
                    return getMainClassName(file)
                }
            }
        }
        return files
            .firstOrNull {
                it.declarations.any {
                    it is KtNamedFunction &&
                        try {
                            mainFunctionDetector.isMain(it)
                        } catch (e: Exception) {
                            false
                        }
                }
            }
            ?.let { getMainClassName(it) }
            ?: getMainClassName(files.iterator().next())
    }

    private fun getMainClassName(file: KtFile): String {
        return PackagePartClassUtils.getPackagePartFqName(file.packageFqName, file.name).asString()
    }

    private fun createPsiFiles(files: Map<String, String>): List<KtFile> =
        files.entries.mapNotNull {
            createFile(EnvironmentManager.environment!!.project, it.key, it.value)
        }

    fun createFile(
        project: Project,
        name: String,
        text: String,
    ): KtFile? {
        var n = name
        if (!n.endsWith(".kt")) {
            n += ".kt"
        }
        val virtualFile = LightVirtualFile(n, KotlinLanguage.INSTANCE, text)
        virtualFile.charset = CharsetToolkit.UTF8_CHARSET
        return (PsiFileFactory.getInstance(project) as PsiFileFactoryImpl).trySetupPsiForFile(
            virtualFile,
            KotlinLanguage.INSTANCE,
            true,
            false,
        ) as KtFile?
    }

    fun getBindingContext(
        files: List<KtFile>,
        project: Project,
    ): BindingContext {
        val result = analyzeFileForJvm(files, project)
        val analyzeExhaust = result.getFirst()
        return analyzeExhaust.bindingContext
    }

    @Synchronized
    fun analyzeFileForJvm(
        files: List<KtFile>,
        project: Project,
    ): Pair<AnalysisResult, ComponentProvider> {
        val environment = EnvironmentManager.environment!!
        val trace = CliBindingTrace(project)

        val configuration = environment.configuration
        // configuration.put(JVMConfigurationKeys.ADD_BUILT_INS_FROM_COMPILER_TO_DEPENDENCIES, true)

        val container =
            TopDownAnalyzerFacadeForJVM.createContainer(
                environment.project,
                files,
                trace,
                configuration,
                { globalSearchScope -> environment.createPackagePartProvider(globalSearchScope) },
                { storageManager, ktFiles -> FileBasedDeclarationProviderFactory(storageManager, ktFiles) },
            )

        container.getService(LazyTopDownAnalyzer::class.java)
            .analyzeDeclarations(TopDownAnalysisMode.TopLevelDeclarations, files, DataFlowInfo.EMPTY)

        val moduleDescriptor = container.getService(ModuleDescriptor::class.java)
        for (extension in AnalysisHandlerExtension.getInstances(project)) {
            val result = extension.analysisCompleted(project, moduleDescriptor, trace, files)
            if (result != null) break
        }

        return Pair(
            AnalysisResult.success(trace.bindingContext, moduleDescriptor),
            container,
        )
    }

    private class ErrorAnalyzer(private val currentPsiFiles: List<KtFile>, private val currentProject: Project) {
        fun getAllErrors(): Map<String, List<CompileError>> {
            try {
                val errors = HashMap<String, MutableList<CompileError>>()
                for (psiFile in currentPsiFiles) {
                    errors[psiFile.name] = getErrorsByVisitor(psiFile)
                }
                val bindingContext = getBindingContext(currentPsiFiles, currentProject)
                getErrorsFromDiagnostics(bindingContext.getDiagnostics().all(), errors)
                return errors
            } catch (e: Throwable) {
                throw e
            }
        }

        fun getErrorsFromDiagnostics(
            diagnostics: Collection<Diagnostic>,
            errors: Map<String, MutableList<CompileError>>,
        ) {
            try {
                for (diagnostic in diagnostics) {
                    // fix for errors in js library files
                    val virtualFile = diagnostic.psiFile.virtualFile
                    if (virtualFile == null) {
                        continue
                    }
                    val render = DefaultErrorMessages.render(diagnostic)
                    if (render.contains("This cast can never succeed")) {
                        continue
                    }
                    if (diagnostic.severity != org.jetbrains.kotlin.diagnostics.Severity.INFO) {
                        val textRangeIterator = diagnostic.textRanges.iterator()
                        if (!textRangeIterator.hasNext()) {
                            continue
                        }
                        val firstRange = textRangeIterator.next()

                        var className = diagnostic.severity.name
                        if (!(diagnostic.factory === Errors.UNRESOLVED_REFERENCE) && diagnostic.severity == org.jetbrains.kotlin.diagnostics.Severity.ERROR) {
                            className = "red_wavy_line"
                        }
                        val interval =
                            getInterval(
                                firstRange.startOffset,
                                firstRange.endOffset,
                                diagnostic.psiFile.viewProvider.document!!,
                            )
                        errors[diagnostic.psiFile.name]!!.add(
                            CompileError(
                                interval,
                                render,
                                convertSeverity(diagnostic.severity),
                                className,
                            ),
                        )
                    }
                }

                for (key in errors.keys) {
                    Collections.sort<CompileError>(
                        errors[key],
                        Comparator { o1, o2 ->
                            if (o1.interval.start.line > o2.interval.start.line) {
                                return@Comparator 1
                            } else if (o1.interval.start.line < o2.interval.start.line) {
                                return@Comparator -1
                            } else if (o1.interval.start.line == o2.interval.start.line) {
                                if (o1.interval.start.ch > o2.interval.start.ch) {
                                    return@Comparator 1
                                } else if (o1.interval.start.ch < o2.interval.start.ch) {
                                    return@Comparator -1
                                } else if (o1.interval.start.ch == o2.interval.start.ch) {
                                    return@Comparator 0
                                }
                            }
                            -1
                        },
                    )
                }
            } catch (e: Throwable) {
                throw e
            }
        }

        private fun getErrorsByVisitor(psiFile: PsiFile): MutableList<CompileError> {
            val errorElements = ArrayList<PsiErrorElement>()
            val visitor =
                object : PsiElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        element.acceptChildren(this)
                    }

                    override fun visitErrorElement(element: PsiErrorElement) {
                        errorElements.add(element)
                    }
                }

            val errors = ArrayList<CompileError>()
            visitor.visitFile(psiFile)
            for (errorElement in errorElements) {
                val start = errorElement.textRange.startOffset
                val end = errorElement.textRange.endOffset
                val interval = getInterval(start, end, psiFile.viewProvider.document!!)
                errors.add(
                    CompileError(
                        interval,
                        errorElement.errorDescription,
                        convertSeverity(org.jetbrains.kotlin.diagnostics.Severity.ERROR),
                        "red_wavy_line",
                    ),
                )
            }
            return errors
        }

        private fun convertSeverity(severity: org.jetbrains.kotlin.diagnostics.Severity): Severity {
            return when (severity) {
                org.jetbrains.kotlin.diagnostics.Severity.ERROR -> Severity.ERROR
                org.jetbrains.kotlin.diagnostics.Severity.INFO -> Severity.INFO
                org.jetbrains.kotlin.diagnostics.Severity.WARNING -> Severity.WARNING
                else -> Severity.INFO
            }
        }

        private fun getInterval(
            start: Int,
            end: Int,
            currentDocument: Document,
        ): TextInterval {
            val lineNumberForElementStart = currentDocument.getLineNumber(start)
            val lineNumberForElementEnd = currentDocument.getLineNumber(end)
            var charNumberForElementStart = start - currentDocument.getLineStartOffset(lineNumberForElementStart)
            var charNumberForElementEnd = end - currentDocument.getLineStartOffset(lineNumberForElementStart)
            if (start == end && lineNumberForElementStart == lineNumberForElementEnd) {
                charNumberForElementStart--
                if (charNumberForElementStart < 0) {
                    charNumberForElementStart++
                    charNumberForElementEnd++
                }
            }
            val startPosition =
                TextPosition(
                    lineNumberForElementStart,
                    charNumberForElementStart,
                )
            val endPosition =
                TextPosition(lineNumberForElementEnd, charNumberForElementEnd)
            return TextInterval(startPosition, endPosition)
        }
    }
}
