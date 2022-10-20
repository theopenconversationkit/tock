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

import org.chocosolver.solver.Model
import org.chocosolver.solver.Solver
import org.chocosolver.solver.search.strategy.Search
import org.chocosolver.solver.variables.IntVar

// TODO MASS
class ChocoSolverNumberTest {


}

fun main() {

    // 1. Create a Model
    val model = Model("my first problem")

    // 2. Create variables
    val x : IntVar = model.intVar("X", intArrayOf(0, 1, 2, 3, 4, 5)) // takes value in { 0, 1, 2, 3, 4, 5 }
    val y : IntVar = model.intVar("Y", 0, 5) // takes value in [0, 5]

    // 3. Create and post constraints thanks to the model
    //model.element(x, intArrayOf(5,0,4,1,3,2), y).post()

    // 3b. Or directly through variables
    x.add(y).le(2).post()

    // 4. Get the solver
    val solver : Solver = model.solver

    // 5. Define the search strategy
    solver.setSearch(Search.inputOrderLBSearch(x, y))

    // 6. Launch the resolution process
    // solver.solve()

    var i = 1
    // Computes all solutions : Solver.solve() returns true whenever a new feasible solution has been found
    while (solver.solve()) {
        println("Solution " + i++ + " found : " + x + ", " + y)
    }

    // 7. Print search statistics
    // solver.printStatistics()
}


