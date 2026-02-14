import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// This helper stores the current scaling factor
object AppScaler {
    var ratio: Float = 1f

    fun toDp(value: Int): Dp = (value * ratio).dp
    fun toSp(value: Int): TextUnit = (value * ratio).sp
}

@Composable
fun InitAppScaler() {
    val configuration = LocalConfiguration.current
    // Base design width (360dp)
    AppScaler.ratio = configuration.screenWidthDp / 360f
}

// Extension properties for cleaner syntax: 20.pDp
val Int.pDp: Dp get() = AppScaler.toDp(this)
val Int.pSp: TextUnit get() = AppScaler.toSp(this)