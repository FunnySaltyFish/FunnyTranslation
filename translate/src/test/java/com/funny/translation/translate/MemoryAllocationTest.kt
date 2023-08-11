import org.junit.Test
import kotlin.system.measureNanoTime

class MemoryAllocationTest {
    @Test
    fun test() {
        val iterations = 1000 // Adjust the number of iterations as needed

        // Test the old implementation
        var oldTimeUsage = 0L
        val oldMemoryUsage = measureMemoryUsage {
            repeat(iterations) {
                val toComplete: MutableSet<String> = mutableSetOf("A", "B", "C")
                val toApply: MutableList<String> = mutableListOf("D", "E", "F")

                oldTimeUsage += measureNanoTime {
                    toComplete += toApply
                    toApply.fastForEach { composition ->
                        composition.applyChanges()
                    }
                }
            }
        }

        // Test the new implementation
        var newTimeUsage = 0L
        val newMemoryUsage = measureMemoryUsage {
            repeat(iterations) {
                val toCompleteNew: MutableSet<String> = mutableSetOf("A", "B", "C")
                val toApplyNew: MutableList<String> = mutableListOf("D", "E", "F")

                newTimeUsage += measureNanoTime {
                    toApplyNew.fastForEach { composition ->
                        toCompleteNew.add(composition)
                    }
                    toApplyNew.fastForEach { composition ->
                        composition.applyChanges()
                    }
                }
            }
        }

        println("Old time usage: $oldTimeUsage, new time usage: $newTimeUsage, ratio: ${oldTimeUsage.toDouble() / newTimeUsage}")
        println("Old memory usage: $oldMemoryUsage, new memory usage: $newMemoryUsage, ratio: ${oldMemoryUsage.toDouble() / newMemoryUsage}")
    }

    @Test
    fun test5times(){
        repeat(5) {
            //
            Runtime.getRuntime().gc()
            test()
            println()
        }
    }

    private inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
        for (index in indices) {
            val item = get(index)
            action(item)
        }
    }

    private fun String.applyChanges() {
        // Simulate applying changes to the string
    }

    private inline fun measureMemoryUsage(block: () -> Unit): Long {
        val runtime = Runtime.getRuntime()
        val before = runtime.freeMemory()
        block()
        val after = runtime.freeMemory()
        return before - after
    }
}