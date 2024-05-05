package ktorch

import java.util.ArrayList
import kotlin.reflect.KClass

class Tensor<T : Any> {
  private val data: MutableList<MutableList<T>>
  val rows: Int get() = data.size
  val cols: Int get() = data[0].size
  val kClass: KClass<T>

  constructor(rows: Int, cols: Int, kClass: KClass<T>) {
    data = ArrayList(rows)
    this.kClass = kClass
    (0..<rows).forEach { _ ->
      val row = ArrayList<T>(cols)
      (0..<cols).forEach { _ ->
        when (kClass) {
          Double::class -> row.add(0.0 as T)
          Float::class -> row.add(0F as T)
          Int::class -> row.add(0 as T)
          else -> throw IllegalArgumentException("Unsupported type: $kClass")
        }
      }
      data.add(row)
    }
  }

  fun data(): Array<Any> {
    return data.map {
      it.toArray { arrayOfNulls<Any>(cols) }
    }.toArray { arrayOf<Any>() }
  }

  override fun equals(other: Any?): Boolean {
    return if (this === other) true
    else if (other?.javaClass != javaClass) false
    else {
      other as Tensor<*>
      if (data != other.data) false
      else if (rows != other.rows) false
      else if (cols != other.cols) false
      else if (kClass != other.kClass) false
      else true
    }
  }

  override fun hashCode(): Int {
    var result = data.hashCode()
    result = 31 * result + rows
    result = 31 * result + cols
    result = 31 * result + kClass.hashCode()
    return result
  }

  override fun toString(): String {
    return data.joinToString(",\n", prefix = "[", postfix = "]") {
      it.joinToString(", ", prefix = "[", postfix = "]")
    }
  }

  operator fun get(row: Int, col: Int? = null): Tensor<T> {
    val thisData = data
    return if (col == null)
      Tensor(1, cols, kClass).apply { data[0] = thisData[row] }
    else
      // return a 1x1 tensor
      Tensor(1, 1, kClass).apply { data[0][0] = thisData[row][col] }
  }

  operator fun set(row: Int, col: Int? = null, value: T) {
    if (col != null) {
      data[row][col] = value
    } else {
      // if col is null, assume single row tensor
      if (rows == 1) {
        data[0][row] = value
      } else {
        data[row] = (0 until cols).map { value }.toMutableList()
      }
    }
  }

  companion object {
    inline fun <reified T : Any> zeros(xy: Pair<Int, Int>): Tensor<T> {
      return Tensor(xy.first, xy.second, T::class)
    }

    inline fun <reified T : Any> tensor(
      cols: Int,
      noinline init: (Tensor<T>.() -> Unit)? = null
    ): Tensor<T> {
      val tensor = Tensor(1, cols, T::class)
      return if (init != null) {
        tensor.apply { init() }
      } else {
        tensor
      }
    }
  }
}
