import kotlin.random.Random

class MineExplosionException(message: String) : Exception(message)

fun main() {
    var gameEnd = false
    print("How many mines do you want on the field? ")
    val n = readln().toInt()
    val mineField = MineField(X, Y)
    mineField.addMines(n)
    while (!gameEnd) {
        println(mineField.toString())
        print("Set/delete mines marks (x and y coordinates): ")
        val input = readln().split(" ")
        println()
        try {
            val x = input[1].toInt() - 1
            val y = input[0].toInt() - 1
            val action = input[2]
            when (action) {
                "mine" -> {
                    if (mineField.mineField[x][y].displayValue != "." && mineField.mineField[x][y].displayValue != "*") {
                        println("There is a number here!")
                    } else {
                        mineField.mineField[x][y].setMark()
                    }
                }

                "free" -> {
                    mineField.tryFieldFree(x, y, true)
                }

                else -> continue
            }

        } catch (e: MineExplosionException) {
            mineField.showMines()
            println(mineField.toString())
            println(e.message)
            break
        } catch (e: Exception) {
            throw e
        }
        gameEnd = mineField.checkGameEnd()
    }
}

const val X = 9
const val Y = 9

data class FieldEntry(var mine: Boolean, var displayValue: String) {
    var marked = false
    var free = false
    var isHint = false
    var hintVal = 0
    fun setMine() {
        this.mine = true
    }

    fun setFieldValue(s: String) {
        this.displayValue = s
    }

    fun setMark() {
        if (!this.marked) {
            this.marked = true
            this.setFieldValue("*")
        } else {
            unsetMark()
        }
    }

    fun unsetMark() {
        if (this.marked) {
            this.marked = false
            this.setFieldValue(".")
        } else {
            setMark()
        }
    }

    fun setFree() {
        if (!free) {
            this.free = true
            this.setFieldValue("/")
        }
    }
}

class IllegalNumberOfMines(message: String) : Exception(message)
class MineField(val x: Int, val y: Int) {
    val mineField: MutableList<MutableList<FieldEntry>> = MutableList(x) { mutableListOf<FieldEntry>() }
    var totalMines: Int = 0
    var marksSet: Int = 0

    init {
        for (i in 0 until x) {
            mineField[i] = mutableListOf()
            repeat(y) { mineField[i].add(FieldEntry(false, ".")) }
        }
    }

    fun showMines() {
        mineField.forEach {
            it.map { field -> if (field.mine) field.setFieldValue("X") }
        }
    }

    fun isInField(x: Int, y: Int): Boolean {
        return !(x > 8 || x < 0 || y > 8 || y < 0)
    }

    private fun getNeighbors(x: Int, y: Int): List<Pair<Int, Int>> {
        val neighbors = mutableListOf<Pair<Int, Int>>()
        if (isInField(x + 1, y + 1)) neighbors.add(Pair(x + 1, y + 1))
        if (isInField(x + 1, y)) neighbors.add(Pair(x + 1, y))
        if (isInField(x + 1, y - 1)) neighbors.add(Pair(x + 1, y - 1))

        if (isInField(x, y + 1)) neighbors.add(Pair(x, y + 1))
        if (isInField(x, y - 1)) neighbors.add(Pair(x + 1, y - 1))

        if (isInField(x - 1, y + 1)) neighbors.add(Pair(x - 1, y + 1))
        if (isInField(x - 1, y - 1)) neighbors.add(Pair(x - 1, y - 1))
        if (isInField(x - 1, y)) neighbors.add(Pair(x - 1, y))

        return neighbors
    }

    fun addMines(numberOfMines: Int) {
        totalMines = numberOfMines
        var addedMines = 0
        var randX = Random.nextInt(0, x)
        var randY = Random.nextInt(0, y)
        if (numberOfMines >= x * y) {
            throw IllegalNumberOfMines("At least 1 field should be safe!")
        }
        while (addedMines < numberOfMines) {
            if (mineField[randX][randY].mine) {
                randX = Random.nextInt(0, x)
                randY = Random.nextInt(0, y)
                continue
            } else {
                mineField[randX][randY].setMine()
                addedMines++
            }
        }
        addHints()
    }

    fun tryFieldFree(x: Int, y: Int, player: Boolean) {
        if (x <= 8 && y <= 8) {
            if (mineField[x][y].isHint) {
                if (mineField[x][y].displayValue == "." || mineField[x][y].displayValue == "*") {
                    mineField[x][y].setFieldValue(mineField[x][y].hintVal.toString())
                    //mineField[x][y].setFree()
                    val test = getNeighbors(x, y)
                    for (i in 0..test.size - 1) {
                        tryFieldFree(test[i].first, test[i].second, false)
                    }
                }
            } else if (mineField[x][y].mine) {
                if (player) {
                    throw MineExplosionException("You stepped on a mine and failed!")
                }
            } else if (mineField[x][y].free) {
            } else {
                mineField[x][y].setFree()
                val test = getNeighbors(x, y)
                for (i in 0..test.size - 1) {

                    tryFieldFree(test[i].first, test[i].second, false)
                }
            }
        }
    }

    fun addHints() {
        mineField.forEachIndexed { indexOfLists, strings ->
            strings.forEachIndexed { index, s ->
                if (s.mine) {
                    tryAddHint(indexOfLists - 1, index - 1)
                    tryAddHint(indexOfLists - 1, index)
                    tryAddHint(indexOfLists - 1, index + 1)

                    tryAddHint(indexOfLists, index + 1)
                    tryAddHint(indexOfLists, index - 1)

                    tryAddHint(indexOfLists + 1, index + 1)
                    tryAddHint(indexOfLists + 1, index)
                    tryAddHint(indexOfLists + 1, index - 1)
                    println("test $indexOfLists")
                    println("Moin $index")
                }
            }
        }
    }

    private fun tryAddHint(x: Int, y: Int) {
        try {
            when {
                mineField[x][y].mine -> return
                mineField[x][y].displayValue == "." -> {
                    mineField[x][y].hintVal = 1
                    mineField[x][y].isHint = true
                }

                else -> {
                    val i = mineField[x][y].hintVal + 1
                    mineField[x][y].hintVal = i
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            return
        }
    }

    override fun toString(): String {
        var erg = " │123456789│\n"
        erg += "—│—————————│\n"
        mineField.forEachIndexed { i, it ->
            erg += "${i + 1}|" + it.map { it.displayValue }.joinToString().replace(",", "").replace(" ", "") + "|\n"
        }
        erg += "—│—————————│"
        return erg
    }

    fun checkGameEnd(): Boolean {
        var markedFields = 0
        mineField.forEach {
            it.map { a -> if (a.marked) markedFields++ }
        }
        if (totalMines == markedFields) {
            var markedMines = 0
            mineField.forEach {
                it.map { a -> if (a.marked && a.mine) markedMines++ }
                if (markedMines == totalMines) {
                    println("Congratulations! You found all the mines!")
                    return true
                }
            }
        } else {
            return false
        }
        return false
    }
}
