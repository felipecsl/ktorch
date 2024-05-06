package ktorch

import java.util.ArrayList
import kotlin.reflect.KClass

class Tensor<T: Any> {
  private val data: MutableList<MutableList<T>>
  val rows: Int get() = data.size
  val cols: Int get() = data[0].size
  val kClass: KClass<T>

  private constructor(data: List<List<T>>, kClass: KClass<T>) {
    this.data = data.map { it.toMutableList() }.toMutableList()
    this.kClass = kClass
  }

  @Suppress("UNCHECKED_CAST")
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

  fun copy(): Tensor<T> {
    return Tensor(data, kClass)
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
      // return a 1xN tensor
      Tensor(1, cols, kClass).apply { data[0] = thisData[row] }
    else
      // return a 1x1 tensor
      Tensor(1, 1, kClass).apply { data[0][0] = thisData[row][col] }
  }

  @Suppress("UNCHECKED_CAST")
  operator fun plus(other: T): Tensor<T> {
    val newData = data.map { it.toMutableList() }.toMutableList()
    (0 until rows).forEach { row ->
      (0 until cols).forEach { col ->
        val v = newData[row][col]
        when (kClass) {
          Double::class -> newData[row][col] = (v as Double).plus(other as Double) as T
          Float::class -> newData[row][col] = (v as Float).plus(other as Float) as T
          Int::class -> newData[row][col] = (v as Int).plus(other as Int) as T
          else -> throw IllegalArgumentException("Unsupported type: $kClass")
        }
      }
    }
    return Tensor(newData, kClass)
  }

  // Value can be T or Tensor<T>
  @Suppress("UNCHECKED_CAST")
  operator fun set(row: Int, col: Int? = null, value: Any) {
    if (value.javaClass == kClass.java) {
      value as T
      if (col != null) {
        data[row][col] = value
      } else {
        // if col is null, assume single row tensor
        if (rows == 1) {
          data[0][row] = value
        } else {
          // set value to all columns in the row
          data[row] = (0 until cols).map { value }.toMutableList()
        }
      }
    } else if (value is Tensor<*>) {
      val tensor = value as Tensor<T>
      if (tensor.rows != 1 && tensor.cols != 1) {
        throw IllegalArgumentException("Unsupported tensor shape: ${tensor.rows}x${tensor.cols}")
      }
      val item = tensor.data[0][0]
      if (col != null) {
        data[row][col] = item
      } else {
        // if col is null, assume single row tensor
        if (rows == 1) {
          data[0][row] = item
        } else {
          // set value to all columns in the row
          data[row] = (0 until cols).map { item }.toMutableList()
        }
      }
    } else {
      throw IllegalArgumentException("Unsupported type: ${value.javaClass}")
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
