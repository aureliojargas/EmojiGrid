@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.emojigrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emojigrid.ui.theme.EmojiGridTheme


const val fontSize = 42


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmojiGridTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameBoard(game)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameBoardPreview() {
    GameBoard(game)
}

@Composable
fun GameBoard(game: GameEngine) {

    // Incremental variable to force the full board redraw at every click.
    // It must be used as content or modifier for some Composable.
    // https://developer.android.com/jetpack/compose/state
    var boardRedrawCount by remember { mutableStateOf(0) }

    // https://developer.android.com/jetpack/compose/components/scaffold
    // The bottomBar (if any) may overlap content
    // https://foso.github.io/Jetpack-Compose-Playground/material/scaffold/#tips
    Scaffold(
        topBar = {
            TopAppBar(
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.primary,
//                ),
                title = {
                    Text("Clicks: $boardRedrawCount")
                }

            )
        },
    ) {

        // https://alexzh.com/jetpack-compose-building-grids/
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = 4),
            contentPadding = it, //PaddingValues(8.dp),
        ) {
            items(game.board.size) {
                val card = game.board[it]
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        // https://foso.github.io/Jetpack-Compose-Playground/foundation/shape/
                        .clip(shape = RoundedCornerShape(27.dp))
                        .background(
                            color = when {
                                card.isMatched -> MaterialTheme.colorScheme.background
                                card.isFaceUp -> card.color
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        .clickable {
                            game.clicked(card)
                            boardRedrawCount += 1
                        }
                ) {
                    Text(
                        text = if (card.isFaceUp) card.emoji else "",
                        // I just need to use boardRedrawCount "somehow" to force a redraw
                        // The following use is a no-op
                        fontSize = ((boardRedrawCount * 0) + fontSize).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = (fontSize * 0.8).dp)
                    )
                }
            }
        }
    }
}

//            Card(
//                modifier = Modifier
//                    .padding(4.dp),
//                colors = cardColors(
//                    containerColor = Color(
//                        red = Random.nextInt(0, 255),
//                        green = Random.nextInt(0, 255),
//                        blue = Random.nextInt(0, 255)
//                    ),
//                )

//fun getRandomColor() = Color(
//    red = Random.nextInt(0, 255),
//    green = Random.nextInt(0, 255),
//    blue = Random.nextInt(0, 255)
//)

//enum class CardState {
//    FACE_DOWN, FACE_UP, MATCHED
//}


//-----------------------------------------------------------------------
// Emulate Android utils in Kotlin Playground

//class LogMock {
//    fun d(tag: String, message: String) { println(message) }
//}
//enum class Color { Yellow, Blue, Cyan, Green, Gray, Red, LightGray, Magenta }
//val Log = LogMock()

//-----------------------------------------------
// Data

val emojis = listOf("🐷", "🙂", "🥛", "🎉", "🌈", "🎯", "🧩", "🐳")
val colors = listOf(
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Cyan,
    Color.Magenta,
    Color.Yellow,
    Color.Gray,
    Color.LightGray,
)
val game = GameEngine(emojis, colors, shuffle = true, verbose = true)

//-----------------------------------------------
// Game engine

enum class GameState { MOVE1, MOVE2, MOVE3, END }

class GameEngine(
    val emojis: List<String>,
    val colors: List<Color>,
    val shuffle: Boolean = true,
    val verbose: Boolean = false,
) {
    val slots = (emojis.indices + emojis.indices).let { if (shuffle) it.shuffled() else it }
    val board = slots.indices.map {
        GameCard(emoji = emojis[slots[it]], color = colors[slots[it]])
    }
    var state = GameState.MOVE1

    override fun toString(): String {
        return board.indices.map { "$it${board[it]}" }.joinToString(separator = " ")
    }

    fun nextState() {
        state = when (state) {
            GameState.MOVE1 -> GameState.MOVE2
            GameState.MOVE2 -> GameState.MOVE3
            GameState.MOVE3 -> GameState.MOVE1
            else -> state
        }
    }

    fun maybeVictory() {
        if (board.all { it.isMatched }) {
            state = GameState.END
            if (verbose) {
                println("VICTORY! 🏆")
            }
        }
    }

    fun pendingCards(): List<GameCard> {
        return board.filter { it.isFaceUp && !it.isMatched }
    }

    fun clicked(card: GameCard) {
        when {
            // Nothing to do
            card.isMatched -> return
            card.isFaceUp && state == GameState.MOVE1 -> return // no Undo
            state == GameState.END -> return

            // Turn cards
            state == GameState.MOVE1 -> card.turn()
            state == GameState.MOVE2 -> card.turn()
            state == GameState.MOVE3 -> pendingCards().map { it.turn() }
        }

        // Show board
        if (verbose) {
            println(this)
        }

        // Detect match
        val faceUp = pendingCards()
        if (state == GameState.MOVE2 && faceUp.first().emoji == faceUp.last().emoji) {
            faceUp.map { it.isMatched = true }
            nextState() // will skip step 3
            maybeVictory()
        }

        nextState()
    }
}

class GameCard(
    val emoji: String,
    val color: Color,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false,
) {
    override fun toString() =
        "$emoji${if (isMatched) "🆗" else if (isFaceUp) "⬆︎" else ""}"

    fun turn() {
        if (!isMatched) {
            isFaceUp = !isFaceUp
        }
    }
}

//-----------------------------------------------
// Tests

fun testZeroMatches() {
    val game = GameEngine(emojis.slice(0..3), colors.slice(0..3), shuffle = false)
    val moves = listOf(0, 1, 7, 2, 3, 7)

    moves.map { game.clicked(card = game.board[it]) }

    assert(game.board.none { it.isMatched })
    assert(game.board.none { it.isFaceUp })
    assert(game.state == GameState.MOVE1)
    println("testZeroMatches passed")

}

fun testVictoryNonShuffled() {
    val game = GameEngine(emojis.slice(0..3), colors.slice(0..3), shuffle = false)
    val moves = listOf(0, 4, 1, 5, 2, 6, 3, 7)

    moves.map { game.clicked(card = game.board[it]) }

    assert(game.board.all { it.isMatched })
    assert(game.state == GameState.END)
    println("testVictoryNonShuffled passed")
}

fun testVictoryShuffled() {
    val game = GameEngine(emojis.slice(0..3), colors.slice(0..3))
    val moves = game.board.indices

    // Use all possible moves, always win
    loop@ for (move1 in moves) {
        for (move2 in moves) {
            game.clicked(card = game.board[move1])
            game.clicked(card = game.board[move2])
            if (game.state == GameState.END) {
                break@loop
            }
        }
    }

    assert(game.board.all { it.isMatched })
    assert(game.state == GameState.END)
    println("testVictoryShuffled passed")
}

fun runTests() {
    testZeroMatches()
    testVictoryShuffled()
    testVictoryNonShuffled()
    println("Tests ended")
}


//fun main() {
//    val game = GameEngine(emojis.slice(0..3), colors.slice(0..3), shuffle = false, verbose = true)
//    val moves = listOf(0, 4, 1, 5, 2, 6, 3, 7)
//    moves.map { game.clicked(card = game.board[it]) }
//
//    runTests()
//}


/** Reference:
 * Modifier combination and how it is applied:
 * https://www.youtube.com/watch?v=iEk3ySILgwk&list=PLWz5rJ2EKKc94tpHND8pW8Qt8ZfT1a4cq&index=10
 * https://developer.android.com/jetpack/compose/layouts/constraints-modifiers
 *
 * List of modifiers:
 * https://developer.android.com/jetpack/compose/modifiers-list
 *
 * ## Kotlin
 *
 * Similar to Python's @property (in Kotlin, the "by" keyword)
 * https://kotlinlang.org/docs/delegated-properties.html#standard-delegates
 *
 * Similar to Python's @functools.lru_cache()
 * https://kotlinlang.org/docs/delegated-properties.html#lazy-properties
 *
 */
