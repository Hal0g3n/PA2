package model

data class Range<T>(var min : T, var max : T) {
    override fun toString(): String {
        return "[$min,$max]";
    }
}