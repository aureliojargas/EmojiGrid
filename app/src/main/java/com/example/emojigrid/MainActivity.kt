package com.example.emojigrid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emojigrid.ui.theme.EmojiGridTheme
import kotlin.random.Random

val emojis = listOf("🐷", "🙂", "🥛", "🎉", "🌈", "🎯", "🧩", "🐳")

//val emojis = "🐷🙂🥛🎉🌈🎯🧩🐳".toList()
var found = mutableListOf("")
val colors = (0..7).map { getRandomColor() }

//val data = emojis zip colors
//val board = (data + data).shuffled()
const val fontSize = 42
val slots = ((0 until emojis.size) + (0 until emojis.size)).shuffled()


fun getRandomColor() = Color(
    red = Random.nextInt(0, 255),
    green = Random.nextInt(0, 255),
    blue = Random.nextInt(0, 255)
)
// also Color.LightGray and so on

enum class CardState {
    FACE_DOWN, FACE_UP, MATCHED
}

class GameCard(var emoji: String, var color: Color, var state: CardState = CardState.FACE_DOWN) {
//    override fun toString() = "$emoji $color $state"

    fun clicked() {
        Log.d("XXX", "Clicked in $emoji")
        color = Color.Yellow
        emoji = "✅"
        Log.d("XXX", "${this.emoji}")
//        Log.d("XXX", this.toString())
    }
}

var board = slots.map {
    GameCard(emoji = emojis[it], color = colors[it])
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d("X", cards.toString())
        setContent {
            EmojiGridTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameBoard(board)
                    Row() {
                        Button() {
                            Text("Foo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameCardCompose(card: GameCard, emoji: String, color: Color) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            // https://foso.github.io/Jetpack-Compose-Playground/foundation/shape/
            .clip(shape = RoundedCornerShape(27.dp))
            .background(color)
            .clickable { card.clicked() }

    ) {
        Text(
            text = emoji,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = (fontSize * 0.8).dp)
        )
    }
}


//https://developer.android.com/jetpack/compose/mental-model
//private fun NamePickerItem(name: String, onClicked: (String) -> Unit) {
//    Text(name, Modifier.clickable(onClick = { onClicked(name) }))
//}

//@Preview(showBackground = true)
@Composable
fun GameBoard(board: List<GameCard>) {
    var x = GameCard("X", Color.Yellow)
    x.clicked()
    Log.d("XXX-", x.emoji)
    // https://alexzh.com/jetpack-compose-building-grids/
    LazyVerticalGrid(
        columns = GridCells.Fixed(count = 4),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(board.size) {
            GameCardCompose(board[it], board[it].emoji, board[it].color)
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
