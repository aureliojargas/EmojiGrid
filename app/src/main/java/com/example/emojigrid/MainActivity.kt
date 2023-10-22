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
                        .background(if (card.isMatched) Color.White else card.color)
                        .clickable {
                            if (!card.isMatched) {
                                card.turn()
                                game.process()
                                boardRedrawCount += 1
                            }
                        }
                ) {
                    Text(
                        text = card.emoji,
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

enum class GameState { FIRST, SECOND, END }

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
    var state = GameState.FIRST

    override fun toString(): String {
        return board.indices.map { "$it${board[it]}" }.joinToString(separator = " ")
    }

    fun nextState() {
        state = when (state) {
            GameState.FIRST -> GameState.SECOND
            GameState.SECOND -> GameState.FIRST
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

    fun process() {
        if (state == GameState.SECOND) {
            val faceUp = board.filter { it.isFaceUp && !it.isMatched }
            if (faceUp.size == 2 && faceUp.first().emoji == faceUp.last().emoji) {
                faceUp.map { it.isMatched = true }
                if (verbose) {
                    println(this)
                }
                maybeVictory()
            } else {
                if (verbose) {
                    println(this)
                }
                faceUp.map { it.turn() }
            }
        } else {
            if (verbose) {
                println(this)
            }
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
        "$emoji${if (isFaceUp) "⬆︎" else ""}${if (isMatched) "🆗" else ""}"

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
    val moves = listOf(0, 1, 2, 3)

    moves.map {
        game.board[it].turn()
        game.process()
    }

    assert(game.board.none { it.isMatched })
    assert(game.state == GameState.FIRST)
}

fun testVictoryNonShuffled() {
    val game = GameEngine(emojis.slice(0..3), colors.slice(0..3), shuffle = false)
    val moves = listOf(0, 4, 1, 5, 2, 6, 3, 7)

    moves.map {
        game.board[it].turn()
        game.process()
    }

    assert(game.board.all { it.isMatched })
    assert(game.state == GameState.END)
}

fun testVictoryShuffled() {
    val game = GameEngine(emojis.slice(0..3), colors.slice(0..3))
    val moves = game.board.indices

    // Use all possible moves, always win
    loop@ for (move1 in moves) {
        for (move2 in moves) {
            game.board[move1].turn()
            game.process()
            game.board[move2].turn()
            game.process()
            if (game.state == GameState.END) {
                break@loop
            }
        }
    }

    assert(game.board.all { it.isMatched })
    assert(game.state == GameState.END)
}

fun runTests() {
    testZeroMatches()
    testVictoryShuffled()
    testVictoryNonShuffled()
    println("Tests ended")
}


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
