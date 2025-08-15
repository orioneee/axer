package org.example.project

import io.github.classgraph.ClassGraph
import java.lang.Error
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.kotlinFunction
import kotlin.test.Test
import kotlin.test.fail

class AxerCompatibilityTests{
    private fun extractPublicApi(pkg: String): Set<String> {
        val scanResult = ClassGraph()
            .acceptPackages(pkg)
            .enableClassInfo()
            .scan()

        return scanResult.allClasses
            .filter { !it.isInterface && !it.isAbstract }
            .flatMap { classInfo ->
                val clazz = classInfo.loadClass()
                clazz.declaredMethods
                    .mapNotNull { m ->
                        try {
                            val kfun = m.kotlinFunction ?: return@mapNotNull null
                            if (kfun.visibility != KVisibility.PUBLIC) return@mapNotNull null
                            val params = m.parameterTypes.joinToString(",") { it.simpleName }
                            "${clazz.name}#${m.name}($params):${m.returnType.simpleName}"
                        } catch (e: Error) {
                            null
                        }
                    }
            }
            .filter { !it.contains(".internal.") }
            .filter { !it.contains(".generated.") }
            .filter { entry ->
                if (entry.startsWith("io.github.orioneee.AxerBundledSQLiteDriver#")) {
                    entry.contains("#getInstance(") // keep only getInstance()
                } else true
            }
            .toSet()
    }


    @Test
    fun `original and no-op public API are the same`() {
        val original = extractPublicApi("io.github.orioneee")
            .filter { it != "io.github.orioneee.AxerUIEntryPoint#Screen(AxerDataProvider,Composer,int):void" }
        val noOp = extractPublicApi("io.github.orioneee_no_op")
            .map { it.replace("io.github.orioneee_no_op", "io.github.orioneee") }
            .toSet()

        val missing = (original - noOp).sorted()
        val extra = (noOp - original).sorted()

        if (missing.isNotEmpty() || extra.isNotEmpty()) {
            val message = buildString {
                appendLine("❌ Public API mismatch between original and no-op packages")
                if (missing.isNotEmpty()) {
                    appendLine("\n--- Missing in no-op ---")
                    missing.forEach { appendLine("  • $it") }
                }
                if (extra.isNotEmpty()) {
                    appendLine("\n--- Extra in no-op ---")
                    extra.forEach { appendLine("  • $it") }
                }
            }
            fail(message)
        }
    }
}