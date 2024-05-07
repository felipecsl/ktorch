package ktorch

import ktorch.Tensor.Companion.zeros
import java.io.File
import kotlin.streams.toList
import kotlin.test.Test

class NamesTest {
  @Test fun `bigrams`() {
    val classLoader = javaClass.classLoader
    val file = File(classLoader.getResource("names.txt")!!.file)
    val words = file.readLines()
    val N = zeros<Int>(27 to 27)
    val chars = 'a'..'z'
    val stoi = chars.mapIndexed { index, c -> c to index + 1 }.toMap().toMutableMap()
    stoi['.'] = 0
    val itos = stoi.entries.associate { (k, v) -> v to k }
    words.forEach { w ->
      val chs = ".$w."
      chs.chars().toList().zip(chs.drop(1).chars().toList().toTypedArray()).forEach { (ch1, ch2) ->
        val c1 = ch1.toChar()
        val c2 = ch2.toChar()
        val ix1 = stoi[c1]!!
        val ix2 = stoi[c2]!!
        N[ix1, ix2] += 1
      }
    }
    N.data().mapIndexed { i, row ->
      (row as Array<Any>).mapIndexed { j, _ ->
        print("(${itos[i]}${itos[j]} ${N[i, j].item()}) ")
      }
      println()
    }
    val p = N[0].float()
    val p0 = p / p.sum()
    println(p0)
  }
}
