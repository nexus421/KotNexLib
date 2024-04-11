package kotnexlib

import kotlin.math.sqrt

object Math {

    /**
     * Simple way to calculate the dot product from two vectors.
     * You may use this to compare two embeddings.
     * Both vectors need to be the same size.
     *
     * @return the dot product between -1.0 and 1.0. The closer it is to 1, the more similar the two vectors are.
     */
    fun dotProduct(vectorA: List<Double>, vectorB: List<Double>): Double {
        require(vectorA.size == vectorB.size) { "Both vectors need to be the same size!" }
        var dotProduct = 0.0
        vectorA.indices.forEach { dotProduct += vectorA[it] * vectorB[it] }
        return dotProduct
    }

    /**
     * Simple way to calculate the cosine similarity from two vectors.
     * You may use this to compare two embeddings.
     * Both vectors need to be the same size.
     *
     * @return cosine similarity between -1.0 and 1.0. The closer it is to 1, the more similar the two vectors are.
     */
    fun cosineSimilarity(vectorA: List<Double>, vectorB: List<Double>): Double {
        require(vectorA.size == vectorB.size) { "Both vectors need to be the same size!" }
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        vectorA.indices.forEach {
            val vA = vectorA[it]
            val vB = vectorB[it]
            dotProduct += vA * vB
            normA += vA * vA
            normB += vB * vB
        }

        return dotProduct / (sqrt(normA) * sqrt(normB))
    }

    /**
     * Normalizes a vector.
     * It is the caller's responsibility to check whether the vector is already normalized or not!
     *
     * @return normalized vector
     */
    fun List<Double>.normalizeVector(): List<Double> {
        var count = 0.0
        forEach { count += (it * it) }
        val length = sqrt(count)
        return map { it / length }
    }


    /**
     * Checks whether this vector is normalized or not.
     *
     * Important: Strictly speaking, the sum should be exactly 1 for it to be considered normalized.
     * However, thanks to the inaccuracy of floating-point numbers, tiny deviations are still acceptable here.
     */
    fun List<Double>.isNormalizedVector(): Boolean {
        var count = 0.0
        forEach { count += (it * it) }

        return count < 1.0000009 && count > 0.9999999
    }


}

fun Double.powOfTwo() = this * this
fun Int.powOfTwo() = this * this
fun Float.powOfTwo() = this * this
fun Long.powOfTwo() = this * this

//fun Iterable<Double>.isNormalized(): Boolean {
//    var sum = 0.0
//    forEach { sum += it.pow(2) }
//    return sqrt(sum) == 1.0
//}
//
//fun Array<Double>.isNormalized(): Boolean {
//    var sum = 0.0
//    forEach { sum += it.pow(2) }
//    return sqrt(sum) == 1.0
//}
//
//fun DoubleArray.isNormalized(): Boolean {
//    var sum = 0.0
//    forEach { sum += it.pow(2) }
//    return sqrt(sum) == 1.0
//}