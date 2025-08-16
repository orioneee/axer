import io.github.classgraph.ClassGraph
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.jvm.kotlinProperty
import kotlin.test.Test
import kotlin.test.fail

class AxerCompatibilityTests {

    // --- Helper functions for API extraction ---
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
                        val params = m.parameterTypes.joinToString(", ") {
                            it.simpleName.replaceFirstChar { it.uppercase() }
                        }
                        "${clazz.name}.${m.name}($params): ${m.returnType.simpleName.replaceFirstChar { it.uppercase() }}"
                    } catch (e: Error) {
                        // Suppress errors for methods that can't be processed by reflection
                        null
                    }
                }

                val properties = clazz.declaredFields.mapNotNull { f ->
                    try {
                        f.kotlinProperty?.takeIf { it.visibility == KVisibility.PUBLIC }
                            ?: return@mapNotNull null
                        "${clazz.name}.${f.name}: ${f.type.simpleName.replaceFirstChar { it.uppercase() }}"
                    } catch (e: Error) {
                        // Suppress errors for fields that can't be processed by reflection
                        null
                    }
                }

                functions + properties
            }
            .filter { !it.contains(".internal.") && !it.contains(".generated.") }
            .filter { entry ->
                // Keep only getInstance() for AxerBundledSQLiteDriver$open$1
                if (entry.startsWith("io.github.orioneee.AxerBundledSQLiteDriver.")) {
                    entry.contains(".getInstance(") || entry.contains(".$") // allow compiler stuff if needed
                } else true
            }
            .toSet()
    }

    private fun getOriginalPublicApi(): List<String> {
        return extractPublicApi("io.github.orioneee")
            .filter { !it.contains(".internal.") }
            .filter { !it.contains(".generated.") }
            .filter { it != "io.github.orioneee.AxerUIEntryPoint.Screen(AxerDataProvider, Composer, Int): Void" }
            .filter { it != "io.github.orioneee.AxerServerKt.AXER_SERVER_PORT: Int" }
            .filter { it != "io.github.orioneee.AxerServerKt\$getKtorServer\$1\$4\$invokeSuspend\$\$inlined\$map\$1.collect(FlowCollector, Continuation): Object" }
            .toList()
    }

    private fun getNoOpPublicApi(): List<String> {
        return extractPublicApi("io.github.orioneee_no_op")
            .map { it.replace("io.github.orioneee_no_op", "io.github.orioneee") }
            .toList()
    }

    // --- List of required public APIs ---
    private val mainMethods = listOf(
        "io.github.orioneee.InitializeKt.initialize(Axer): Void",
        "io.github.orioneee.Axer.configure(Function1): Void",
        "io.github.orioneee.Axer.installAxerErrorHandler(): Void",
        "io.github.orioneee.AxerConfig",
        "io.github.orioneee.Axer.ktorPlugin: ClientPlugin",
        "io.github.orioneee.AxerOkhttpInterceptor\$Builder.build(): AxerOkhttpInterceptor",
        "io.github.orioneee.Axer.openAxerUI(): Void",
        "io.github.orioneee.ContentWithAxerFabKt.ContentWithAxerFab(Modifier, Boolean, Function2, Composer, Int, Int): Void",
        "io.github.orioneee.AxerWindowsKt.AxerWindows(WindowState, Function0, Composer, Int, Int): Void",
        "io.github.orioneee.Axer.recordException(Throwable, Function0): Void",
        "io.github.orioneee.Axer.i(String, String, Throwable, Boolean): Void",
        "io.github.orioneee.Axer.e(String, String, Throwable, Boolean): Void"
    )

    // --- Helper function to find missing methods ---
    /**
     * Compares the actual API against a list of required methods and returns the ones that are missing.
     */
    private fun findMissingMainMethods(
        actualApi: List<String>,
        requiredApi: List<String>
    ): List<String> {
        return requiredApi.filter { requiredMethod ->
            actualApi.none { it.contains(requiredMethod) }
        }
    }

    // --- Improved Tests ---

    @Test
    fun `original public API contains main methods`() {
        val original = getOriginalPublicApi()
        val missingMethods = findMissingMainMethods(original, mainMethods)

        if (missingMethods.isNotEmpty()) {
            val message = buildString {
                appendLine("❌ Original public API is missing required methods.")
                appendLine("\n--- Missing Methods ---")
                missingMethods.forEach { appendLine("  • $it") }
            }
            fail(message)
        }
    }

    @Test
    fun `no-op public API contains main methods`() {
        val noOp = getNoOpPublicApi()
        val missingMethods = findMissingMainMethods(noOp, mainMethods)

        if (missingMethods.isNotEmpty()) {
            val message = buildString {
                appendLine("❌ No-op public API is missing required methods.")
                appendLine("\n--- Missing Methods ---")
                missingMethods.forEach { appendLine("  • $it") }
            }
            fail(message)
        }
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
        val original = getOriginalPublicApi().toSet()
        val noOp = getNoOpPublicApi().toSet()

        println("Original API: ${original.joinToString("\n")}")
        println("No-op API: ${noOp.joinToString("\n")}")

        val missingInNoOp = (original - noOp).sorted()
        val extraInNoOp = (noOp - original).sorted()

        if (missingInNoOp.isNotEmpty() || extraInNoOp.isNotEmpty()) {
            val message = buildString {
                appendLine("❌ Public API mismatch between original and no-op packages.")
                if (missingInNoOp.isNotEmpty()) {
                    appendLine("\n--- Missing in no-op (but present in original) ---")
                    missingInNoOp.forEach { appendLine("  • $it") }
                }
                if (extraInNoOp.isNotEmpty()) {
                    appendLine("\n--- Extra in no-op (but missing from original) ---")
                    extraInNoOp.forEach { appendLine("  • $it") }
                }
            }
            fail(message)
        }
    }
}