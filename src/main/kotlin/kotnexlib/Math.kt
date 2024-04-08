package kotnexlib

import kotlin.math.pow
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
            dotProduct += vectorA[it] * vectorB[it]
            normA += vectorA[it].pow(2)
            normB += vectorB[it].pow(2)
        }

        return dotProduct / (sqrt(normA) * sqrt(normB))
    }

}

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