import io.github.classgraph.ClassGraph
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.jvm.kotlinProperty
import kotlin.test.Test
import kotlin.test.fail

class AxerCompatibilityTests {
    private fun extractPublicApi(pkg: String): Set<String> {
        val scanResult = ClassGraph()
            .acceptPackages(pkg)
            .enableClassInfo()
            .scan()

        return scanResult.allClasses
            .flatMap { classInfo ->
                val clazz = classInfo.loadClass()

                val functions = clazz.declaredMethods.mapNotNull { m ->
                    try {
                        val kfun = m.kotlinFunction
                        if (kfun == null || kfun.visibility != KVisibility.PUBLIC) return@mapNotNull null
                        val params = m.parameterTypes.joinToString(",") {
                            it.simpleName.replaceFirstChar { it.uppercase() }
                        }
                        "${clazz.name}#${m.name}($params):${m.returnType.simpleName}"
                    } catch (e: Error) {
//                        println("Error processing method ${m.name} in class ${clazz.name}: ${e.message}")
                        null
                    }
                }

                // 2️⃣ Extract public fields / properties
                val properties = clazz.declaredFields.mapNotNull { f ->
                    try {
                        f.kotlinProperty?.takeIf { it.visibility == KVisibility.PUBLIC }
                            ?: return@mapNotNull null
                        "${clazz.name}#${f.name}:${f.type.simpleName}"
                    } catch (e: Error) {
//                        println("Error processing field ${f.name} in class ${clazz.name}: ${e.message}")
                        null
                    }
                }

                functions + properties
            }
            .filter { !it.contains(".internal.") && !it.contains(".generated.") }
            .filter { entry ->
                // Keep only getInstance() for AxerBundledSQLiteDriver$open$1
                if (entry.startsWith("io.github.orioneee.AxerBundledSQLiteDriver#")) {
                    entry.contains("#getInstance(") || entry.contains("#$") // allow compiler stuff if needed
                } else true
            }
            .toSet()
    }


    fun getOriginalPublicApi(): List<String> {
        return extractPublicApi("io.github.orioneee")
            .filter { !it.contains(".internal.") }
            .filter { !it.contains(".generated.") }
            .filter { it != "io.github.orioneee.AxerUIEntryPoint#Screen(AxerDataProvider,Composer,Int):void" }
            .filter { it != "io.github.orioneee.AxerServerKt#AXER_SERVER_PORT:int" }
    }

    fun getNoOpPublicApi(): List<String> {
        return extractPublicApi("io.github.orioneee_no_op")
            .map { it.replace("io.github.orioneee_no_op", "io.github.orioneee") }
    }

    @Test
    fun `original public API is not empty`() {
        val original = getOriginalPublicApi()
        assert(original.isNotEmpty()) {
            "Original public API is empty. Please check the package structure or the ClassGraph configuration."
        }
    }

    @Test
    fun `no-op public API is not empty`() {
        val noOp = getNoOpPublicApi()
        assert(noOp.isNotEmpty()) {
            "No-op public API is empty. Please check the package structure or the ClassGraph configuration."
        }
    }


    @Test
    fun `original and no-op public API are the same`() {
        val original = getOriginalPublicApi().sorted()
        val noOp = getNoOpPublicApi().sorted()

        println("Original Public API:\n${original.joinToString("\n")}")
        println("No-Op Public API:\n${noOp.joinToString("\n")}")

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