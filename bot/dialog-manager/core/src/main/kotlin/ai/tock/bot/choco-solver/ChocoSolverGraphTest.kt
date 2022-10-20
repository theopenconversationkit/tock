/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.`choco-solver`

import org.chocosolver.graphsolver.GraphModel
import org.chocosolver.solver.exception.ContradictionException
import org.chocosolver.solver.variables.IntVar
import org.chocosolver.util.objects.graphs.DirectedGraph
import org.chocosolver.util.objects.setDataStructures.SetType

// TODO MASS
class ChocoSolverGraphTest {


}

fun main() {

    val n = 5
    val model = GraphModel()

    // VARIABLE COUNTING THE NUMBER OF ARCS
    val nbArcs: IntVar = model.intVar("arcCount", 0, n * n, true)

    val GLB = DirectedGraph(model, n, SetType.BITSET, true)
    val GUB = DirectedGraph(model, n, SetType.BITSET, true)
    GLB.addArc(0, 1) // some arbitrary mandatory arcs
    GLB.addArc(1, 2)
    GLB.addArc(3, 1)

    for (i in 0 until n) {
        for (j in 0 until n) {
            GUB.addArc(i, j) // potential edge
        }
    }

    val dag = model.digraphVar("dag", GLB, GUB)

    // CONSTRAINTS
    model.noCircuit(dag).post()
    model.nbArcs(dag, nbArcs).post()

    // SOLVING AND PRINTS
    println(dag.graphVizExport()) // displays initial graph domain

    try {
        model.solver.propagate() // propagates constraints (without branching)
    } catch (e: ContradictionException) {
        e.printStackTrace()
    }

    println(dag.graphVizExport()); // displays graph domain after propagation
    if (model.solver.solve()){
        println("solution found : $nbArcs");
        println(dag.graphVizExport()); // displays solution graph
    }
    model.solver.printStatistics();
}


