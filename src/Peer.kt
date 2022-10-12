data class Peer<T>(
    val branch: RTree<T>,
    val skipGraphPtr: Array<Peer<T>>,
    val id: String
)  {
    fun query(): Int = 1;
}