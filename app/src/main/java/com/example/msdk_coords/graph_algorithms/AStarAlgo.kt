package com.example.msdk_coords.graph_algorithms

import java.util.PriorityQueue

class AStarAlgo(private val graph: Array<DoubleArray>, private val start: Int = 0) {

    private fun heuristic(node: Int, visited: Set<Int>): Double {
        val unvisited = (0 until graph.size).filter { it !in visited }
        return if (unvisited.isEmpty()) {
            graph[node][start]
        } else {
            unvisited.minOf { graph[node][it] } + graph[node][start]
        }
    }

    fun findPath(): Pair<List<Int>, Double> {
        data class QueueItem(
            val estimatedTotal: Double,
            val node: Int,
            val visited: Set<Int>,
            val path: List<Int>
        )

        val priorityQueue = PriorityQueue<QueueItem>(compareBy { it.estimatedTotal })
        priorityQueue.add(QueueItem(0.0, start, setOf(start), listOf(start)))

        var bestCost = Double.MAX_VALUE
        var bestPath = emptyList<Int>()

        while (priorityQueue.isNotEmpty()) {
            val current = priorityQueue.poll()
            val (cost, node, visited, path) = current

            if (visited.size == graph.size) {
                val totalCost = cost + graph[node][start]
                val newPath = path + start
                if (totalCost < bestCost) {
                    bestCost = totalCost
                    bestPath = newPath
                }
                continue
            }

            for (neighbor in 0 until graph.size) {
                if (neighbor !in visited) {
                    val newVisited = visited + neighbor
                    val newCost = cost + graph[node][neighbor]
                    val heuristicValue = heuristic(neighbor, newVisited)
                    val estimatedTotal = newCost + heuristicValue
                    val newPath = path + neighbor
                    priorityQueue.add(QueueItem(estimatedTotal, neighbor, newVisited, newPath))
                }
            }
        }

        return bestPath to bestCost
    }
}