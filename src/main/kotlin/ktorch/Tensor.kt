package ktorch

import java.util.ArrayList
import kotlin.reflect.KClass

class Tensor<T: Any> {
  private val data: MutableList<MutableList<T>>
  val rows: Int get() = data.size
  val cols: Int get() = data[0].size
  val kClass: KClass<T>

  constructor(data: List<List<T>>, kClass: KClass<T>) {
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

  fun float(): Tensor<Float> {
    if (kClass == Float::class) {
      return this as Tensor<Float>
    }
    val newData = data.map {
      it.map { c ->
        when (kClass) {
          Double::class -> (c as Double).toFloat()
          Int::class -> (c as Int).toFloat()
          else -> throw IllegalArgumentException("Unsupported type: $kClass")
        }
      }
    }
    return Tensor(newData, Float::class)
  }

  fun double(): Tensor<Double> {
    if (kClass == Double::class) {
      return this as Tensor<Double>
    }
    val newData = data.map {
      it.map { c ->
        when (kClass) {
          Float::class -> (c as Float).toDouble()
          Int::class -> (c as Int).toDouble()
          else -> throw IllegalArgumentException("Unsupported type: $kClass")
        }
      }
    }
    return Tensor(newData, Double::class)
  }

  fun int(): Tensor<Int> {
    if (kClass == Int::class) {
      return this as Tensor<Int>
    }
    val newData = data.map {
      it.map { c ->
        when (kClass) {
          Float::class -> (c as Float).toInt()
          Double::class -> (c as Double).toInt()
          else -> throw IllegalArgumentException("Unsupported type: $kClass")
        }
      }
    }
    return Tensor(newData, Int::class)
  }

  fun data(): Array<Any> {
    return data.map {
      it.toArray { arrayOfNulls<Any>(cols) }
    }.toArray { arrayOf<Any>() }
  }

  fun sum(): Tensor<T> {
    val newData = data.flatten().reduce { acc, t ->
      when (kClass) {
        Double::class -> (acc as Double).plus(t as Double) as T
        Float::class -> (acc as Float).plus(t as Float) as T
        Int::class -> (acc as Int).plus(t as Int) as T
        else -> throw IllegalArgumentException("Unsupported type: $kClass")
      }
    }
    return Tensor(listOf(listOf(newData)), kClass)
  }

  fun item(): T {
    if (rows != 1 || cols != 1) {
      throw IllegalArgumentException("Tensor is not a 1x1 tensor")
    }
    return data[0][0]
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
    return if (rows == 1) {
      // this matches the PyTorch behavior
      data[0].joinToString(", ", prefix = "tensor([", postfix = "])")
    } else {
      data.joinToString(",\n", prefix = "tensor([", postfix = "])") {
        it.joinToString(", ", prefix = "[", postfix = "]")
      }
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
  operator fun plus(other: Any): Tensor<T> {
    if (other is Tensor<*>) {
      return plus(other.item())
    }
    if (!kClass.javaObjectType.isAssignableFrom(other.javaClass)) {
      return plus(other.asType(kClass))
    }
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

  operator fun div(other: Any): Tensor<T> {
    if (other is Tensor<*>) {
      return div(other.item())
    }
    if (!kClass.javaObjectType.isAssignableFrom(other.javaClass)) {
      return div(other.asType(kClass))
    }
    val newData = data.map { it.toMutableList() }.toMutableList()
    (0 until rows).forEach { row ->
      (0 until cols).forEach { col ->
        val v = newData[row][col]
        when (kClass) {
          Double::class -> newData[row][col] = (v as Double).div(other as Double) as T
          Float::class -> newData[row][col] = (v as Float).div(other as Float) as T
          Int::class -> newData[row][col] = (v as Int).div(other as Int) as T
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

    inline fun <reified T: Any> of(
      data: List<T>
    ): Tensor<T> {
      return Tensor(listOf(data), T::class)
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

    private inline fun <T: Any> Any.asType(klass: KClass<T>): T {
      return when (klass) {
        Double::class -> {
          return when (this) {
            is Double -> this as T
            is Float -> this.toDouble() as T
            is Int -> this.toDouble() as T
            else -> throw IllegalArgumentException("Unsupported type: $klass")
          }
        }
        Float::class -> {
          return when (this) {
            is Float -> this as T
            is Double -> this.toFloat() as T
            is Int -> this.toFloat() as T
            else -> throw IllegalArgumentException("Unsupported type: $klass")
          }
        }
        Int::class -> {
          return when (this) {
            is Int -> this as T
            is Double -> this.toInt() as T
            is Float -> this.toInt() as T
            else -> throw IllegalArgumentException("Unsupported type: $klass")
          }
        }
        else -> throw IllegalArgumentException("Unsupported type: $klass")
      }
    }
  }
}
