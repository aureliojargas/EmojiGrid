package com.example.emojigrid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emojigrid.ui.theme.EmojiGridTheme
import kotlin.random.Random

val emojis = mutableListOf("🐷️", "🙂", "🥛", "🎉", "🌈", "🎯", "🧩", "🐳")
val found = mutableListOf("")
val colors = (0..7).map { getRandomColor() }

//    val data = emojis zip colors
//    val board = (data + data).shuffled()
const val fontSize = 42
val board = ((0 until emojis.size) + (0 until emojis.size)).shuffled()

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
                    GameBoard()
                }
            }
        }
    }
}

fun getRandomColor() = Color(
    red = Random.nextInt(0, 255),
    green = Random.nextInt(0, 255),
    blue = Random.nextInt(0, 255)
)
// also Color.LightGray and so on


@Composable
fun CardText(text: String) {
    Text(
        text = text,
        fontSize = fontSize.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = (fontSize * 0.8).dp)
    )
}

@Composable
fun CardBox(slot: Int, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            // https://foso.github.io/Jetpack-Compose-Playground/foundation/shape/
            .clip(shape = RoundedCornerShape(27.dp))
            .background(color = if (emojis[board[slot]] in found) Color.White else colors[board[slot]])
            .clickable {
                if (emojis[board[slot]] !in found) {
                    Log.println(Log.INFO, "Emoji", "CLICKED")
                    Log.println(Log.INFO, "Emoji", found.toString())
                    found.add(emojis[board[slot]])
                    Log.println(Log.INFO, "Emoji", found.toString())
//                        Log.d(TAG, "focusRequester.requestFocus()")
                }
            }
    ) {
        content()
    }
}

@Composable
fun GameCard(slot: Int) {
    CardBox(slot) { CardText(text = emojis[board[slot]]) }
}


@Preview(showBackground = true)
@Composable
fun GameBoard() {
    // https://alexzh.com/jetpack-compose-building-grids/
    LazyVerticalGrid(
        columns = GridCells.Fixed(count = 4),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(board.size) {
            GameCard(it)
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
