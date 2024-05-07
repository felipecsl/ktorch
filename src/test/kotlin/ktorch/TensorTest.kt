package ktorch

import com.google.common.truth.Truth.assertThat
import ktorch.Tensor.Companion.tensor
import kotlin.test.Test

class TensorTest {
  @Test fun `zero doubles`() {
    val tensor = Tensor.zeros<Double>(3 to 5)
    println(tensor)
    val actual = tensor.data()
    val expected = Array(3) { DoubleArray(5).toTypedArray() }
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun `zero floats`() {
    val tensor = Tensor.zeros<Float>(3 to 5)
    val actual = tensor.data()
    val expected = Array(3) { FloatArray(5).toTypedArray() }
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun `zero ints`() {
    val tensor = Tensor.zeros<Int>(3 to 5)
    val actual = tensor.data()
    val expected = Array(3) { IntArray(5).toTypedArray() }
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun `tensor init and toString()`() {
    var tensor = tensor<Double>(3)
    assertThat(tensor.toString()).isEqualTo("tensor([0.0, 0.0, 0.0])")
    tensor = tensor<Double>(1) { this[0] = 3.0 }
    assertThat(tensor.toString()).isEqualTo("tensor([3.0])")
    tensor = Tensor.zeros<Double>(3 to 5)
    assertThat(tensor.toString()).isEqualTo("""
      tensor([[0.0, 0.0, 0.0, 0.0, 0.0],
      [0.0, 0.0, 0.0, 0.0, 0.0],
      [0.0, 0.0, 0.0, 0.0, 0.0]])
    """.trimIndent())
  }

  @Test fun `indexing get and set`() {
    val tensor = Tensor.zeros<Double>(3 to 5)
    tensor[1, 2] = 3.0
    assertThat(tensor[1, 2]).isEqualTo(tensor<Double>(1) { this[0] = 3.0 })
    assertThat(tensor[1, 2].item()).isEqualTo(3.0)
    assertThat(tensor[1]).isEqualTo(tensor<Double>(5) { this[2] = 3.0 })
    val expected = arrayOf(
      arrayOf(0.0, 0.0, 0.0, 0.0, 0.0),
      arrayOf(0.0, 0.0, 3.0, 0.0, 0.0),
      arrayOf(0.0, 0.0, 0.0, 0.0, 0.0),
    )
    assertThat(tensor.data()).isEqualTo(expected)
  }

  @Test fun `set single cell`() {
    val tensor = tensor<Double>(3)
    tensor[1] = 4.0
    val expected = arrayOf(arrayOf(0.0, 4.0, 0.0))
    assertThat(tensor.data()).isEqualTo(expected)
  }

  @Test fun `set row`() {
    val tensor = Tensor.zeros<Double>(3 to 5)
    tensor[1] = 4.0
    val expected = arrayOf(
      arrayOf(0.0, 0.0, 0.0, 0.0, 0.0),
      arrayOf(4.0, 4.0, 4.0, 4.0, 4.0),
      arrayOf(0.0, 0.0, 0.0, 0.0, 0.0),
    )
    assertThat(tensor.data()).isEqualTo(expected)
  }

  @Test fun `plus and plusAssign`() {
    val tensor = Tensor.zeros<Double>(3 to 5)
    tensor[1, 1] = 2.0
    tensor[1, 1] = tensor[1, 1] + 3.0
    val expected = arrayOf(
      arrayOf(0.0, 0.0, 0.0, 0.0, 0.0),
      arrayOf(0.0, 5.0, 0.0, 0.0, 0.0),
      arrayOf(0.0, 0.0, 0.0, 0.0, 0.0),
    )
    assertThat(tensor.data()).isEqualTo(expected)
    tensor[1, 1] += 1.0
    val newExpected = arrayOf(
      arrayOf(0.0, 0.0, 0.0, 0.0, 0.0),
      arrayOf(0.0, 6.0, 0.0, 0.0, 0.0),
      arrayOf(0.0, 0.0, 0.0, 0.0, 0.0),
    )
    assertThat(tensor.data()).isEqualTo(newExpected)
  }

  @Test fun `data conversion`() {
    val tensor = tensor<Int>(3) { this[0] = 1 }
    assertThat(tensor.data()).isEqualTo(arrayOf(arrayOf(1, 0, 0)))
    assertThat(tensor.float().data()).isEqualTo(arrayOf(arrayOf(1f, 0f, 0f)))
    assertThat(tensor.double().data()).isEqualTo(arrayOf(arrayOf(1.0, 0.0, 0.0)))
    assertThat(tensor.double().int().data()).isEqualTo(arrayOf(arrayOf(1, 0, 0)))
  }

  @Test fun `tensor sum`() {
    val tensor = Tensor.of<Int>(listOf(1, 2, 3))
    assertThat(tensor.sum().item()).isEqualTo(6)
  }
}
