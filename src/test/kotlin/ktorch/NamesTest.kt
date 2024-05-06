package ktorch

import ktorch.Tensor.Companion.zeros
import java.io.File
import kotlin.streams.toList
import kotlin.test.Test

class NamesTest {
  @Test fun `names`() {
    val classLoader = javaClass.classLoader
    val file = File(classLoader.getResource("names.txt")!!.file)
    val words = file.readLines()
    val tensor = zeros<Int>(28 to 28)
    val chars = 'a'..'z'
    val stoi = chars.mapIndexed { index, c -> c to index }.toMap()
    words.forEach { w ->
      w.chars().toList().zip(w.drop(1).chars().toList().toTypedArray()).forEach { (ch1, ch2) ->
        val c1 = ch1.toChar()
        val c2 = ch2.toChar()
        val ix1 = stoi[c1]!!
        val ix2 = stoi[c2]!!
        tensor[ix1, ix2] += 1
      }
    }
    println(tensor)
  }
}
