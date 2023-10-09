package com.funny.translation.translate

import com.funny.translation.helper.TextSplitter
import org.junit.Test

class TextSplitterTest  {
    @Test
    fun testTextSplitter() {
        val source = """根据您提供的数据结果，我们可以得出以下结论并进行简单分析：

纯Python计算（165次/秒）：使用纯Python实现的泊松求解器的每秒计算量较低。这是由于Python是一种解释型语言，其执行速度相对较慢，因此在大规模计算任务中性能较差。

纯Taichi计算（230次/秒）：使用纯Taichi的实现方式相比纯Python有显著的性能提升。Taichi是一种基于JIT（即时编译）的领域特定语言，与传统的Python相比，它可以更好地利用硬件资源，提高计算效率。

Taichi+MPI（n=1）计算（150次/秒）：使用Taichi结合MPI并行计算（节点数n=1）的性能相对较低。这可能是因为在单节点的情况下，并行化带来的额外开销超过了并行计算的收益。在这种情况下，多个进程之间的通信开销可能抵消了并行计算的优势，导致计算速度减慢。

Taichi+MPI（n=2）计算（230次/秒）：当节点数增加到2时，使用Taichi和MPI的并行计算性能得到了明显的提升。受益于节点间的并行计算，每秒的计算量达到了纯Taichi计算的水平。

Taichi+MPI（n=4）计算（370次/秒）：随着节点数增加到4个，使用Taichi和MPI的并行计算性能持续提升。并行化的计算进一步减少了计算时间，并且每秒的计算量大幅增加。

Taichi+MPI（n=5）计算（400次/秒）：当节点数增加到5时，每秒的计算量继续增加。然而，值得注意的是，随着节点数的增加，性能提升的幅度逐渐减小，可能是由于额外的通信开销和节点间的负载均衡问题。

简单来说，从性能角度考虑，纯Taichi的实现相比纯Python实现具有明显的优势。而结合MPI并行计算，可以进一步提升计算效率，特别是当节点数增加时。然而，需要注意节点数的增加可能会带来额外的通信开销和负载均衡问题，需要进行合理的调优和配置以获得更好的性能。"""

//        println(TextSplitter.splitTextNaturally(source, 1024))
        val splitLengths = intArrayOf(1024, 512, 256, 128, 64, 54, 44, 34, 24)
        for (len in splitLengths) {
            println("----------- len = $len ------------")
            val cut = TextSplitter.splitTextNaturally(source, len)
            print(cut)
            println("(actual len = ${cut.length})")
        }
    }
}