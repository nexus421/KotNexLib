package kotnexlib

object Math {

    /**
     * Simple way to calculate the dot product from two vectors.
     * You may use this to compare two embeddings.
     * Both vectors need to be the same size.
     */
    fun dotProduct(vectorA: List<Double>, vectorB: List<Double>): Double {
        require(vectorA.size == vectorB.size) { "Both vectors need to be the same size!" }
        var dotProduct = 0.0
        vectorA.indices.forEach { dotProduct += vectorA[it] * vectorB[it] }
        return dotProduct
    }


}