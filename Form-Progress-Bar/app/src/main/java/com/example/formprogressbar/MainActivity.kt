package com.example.formprogressbar

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.formprogressbar.ui.theme.FormProgressBarTheme

const val TAG_MA = "MainActivity"
val edgeShape: Shape = RoundedCornerShape(16.dp)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FormProgressBarTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    var numState by remember { mutableStateOf("") }
                    var percentState by remember { mutableStateOf("") }
                    var progressBarNum by remember { mutableStateOf(0) }
                    var progressBarPercent by remember { mutableStateOf(0f) }

                    // get total number and percentage value from users
                    FormField(
                        modifier = Modifier
                            .fillMaxHeight(0.5f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        number = numState,
                        percent = percentState,
                        changeNum = { numState = it },
                        changePercent = { percentState = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // generate progress bar on click (also hides keyboard)
                    GenerateButton {
                        progressBarNum = numState.toIntOrNull() ?: 0
                        progressBarPercent = (percentState.toFloatOrNull() ?: 0f) / 100f
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // green circular progress bar with customizable animation and color
                    CircularProgressBar(
                        modifier = Modifier.weight(1f),
                        number = progressBarNum,
                        percentage = progressBarPercent,
                        animDelay = 400
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                }
            }
        }
    }
}

/* Input Form Fields */
@Composable
fun FormField(
    modifier: Modifier,
    number: String,
    percent: String,
    changeNum: (String) -> Unit,
    changePercent: (String) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {


        // Number Input
        var numHolder by remember { mutableStateOf("") }
        TextField(
            value = number,
            placeholder = { Text(text = "Enter Number") },
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
            singleLine = true,

            modifier = Modifier
                .fillMaxWidth()
                .clip(edgeShape)
                .border(2.dp, Color.Black, shape = edgeShape),

            onValueChange = {
                if (it.all { char -> char.isDigit() }) {
                    numHolder = it
                    changeNum(it)
                }
            },

            // input label
            trailingIcon = {
                if (numHolder.isNotEmpty()) {
                    Indicator("Total")
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Percentage Input
        var percentHolder by remember { mutableStateOf("") }
        TextField(
            value = percent,
            placeholder = { Text(text = "Enter Percentage") },
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
            singleLine = true,

            modifier = Modifier
                .fillMaxWidth()
                .clip(edgeShape)
                .border(2.dp, Color.Black, shape = edgeShape),

            onValueChange = {
                if (
                    (it.all { char -> char.isDigit() }) &&
                    ((it.toIntOrNull() ?: 0) in 0..100)
                ) {
                    percentHolder = it
                    changePercent(it)
                }
            },

            // input label
            trailingIcon = {
                if (percentHolder.isNotEmpty()) {
                    Indicator("%")
                }
            },
        )

    }
}

/* Button to Handle User Input Processing */
@Composable
fun GenerateButton(handleClick: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current

    Button(
        modifier = Modifier.clip(edgeShape),
        onClick = {
            // hide keyboard to display progressbar
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            // updates progress bar values from user input
            handleClick()
        })
    {
        Text(text = "Generate!")
    }
}

/* Form Field Label to be Displayed on the Right Edged */
@Composable
fun Indicator(text: String) {
    // label decoration
    Box(
        modifier = Modifier
            .padding(14.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
            .background(Color.LightGray)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(6.dp),
            style = TextStyle(
                fontSize = 10.sp
            )
        )
    }
}

/* Customizable Circular Progress Bar */
@Composable
fun CircularProgressBar(
    modifier: Modifier,
    number: Int,
    percentage: Float,
    radius: Dp = 50.dp,
    fontSize: TextUnit = 24.sp,
    color: Color = Color.Green,
    strokeWidth: Dp = 8.dp,
    animDuration: Int = 1400,
    animDelay: Int = 0
) {

    var animationPlayed by remember { mutableStateOf(false) }
    val curPercentage = animateFloatAsState(
        targetValue = if ((animationPlayed) && number != 0) percentage else 0f,
        animationSpec = tween(
            durationMillis = animDuration, delayMillis = animDelay
        )
    )

    // do i need this?
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    // container to place progress bar container
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // progress bar container
        Box(
            modifier = Modifier.size(radius * 2f),
            contentAlignment = Alignment.Center,
        ) {
            // holder for progress bar animation
            Canvas(modifier = Modifier.size(radius * 2f)) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360 * curPercentage.value,
                    useCenter = false,
                    style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }

            // percentage of user input number
            Text(
                text = (curPercentage.value * number).toInt().toString(),
                color = Color.Black,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }

}
