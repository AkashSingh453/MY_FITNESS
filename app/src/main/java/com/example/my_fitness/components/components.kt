package com.example.my_fitness.components

import InitAppScaler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import pDp
import pSp
import kotlin.math.abs
import kotlin.math.roundToInt

// ==========================================
// 1. GENDER SCREEN
// ==========================================
@Composable
fun GenderSelectionScreen(onSaveClick: (String) -> Unit) {
    InitAppScaler() // Initialize scaling for current device
    val genderList = remember { listOf("Male", "Female", "Other") }
    var selectedIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // ✅ Changed: Use Theme background
            .background(MaterialTheme.colorScheme.background)
            .padding(16.pDp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection("Tell us about yourself")

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            WheelPicker(
                items = genderList,
                initialIndex = 0,
                onSelectionChanged = { selectedIndex = it }
            )
        }

        NextButton(onClick = { onSaveClick(genderList[selectedIndex]) })
    }
}

// ==========================================
// 2. HEIGHT SCREEN
// ==========================================
@Composable
fun HeightSelectionScreen(
    onSaveClick: (Int, String) -> Unit,
    isCmInitially: Boolean = true,
    currentHeightCm: Int = 170
) {
    InitAppScaler()
    var isCm by remember { mutableStateOf(isCmInitially) }
    val cmList = remember { (100..250).map { "$it" } }
    val ftList = remember { generateFeetList() }
    var internalCm by remember { mutableIntStateOf(if (currentHeightCm > 0) currentHeightCm else 170) }

    val initialIndex = remember(isCmInitially) {
        if (isCmInitially) {
            (internalCm - 100).coerceIn(0, cmList.lastIndex)
        } else {
            val ftString = cmToFootString(internalCm)
            ftList.indexOf(ftString).coerceIn(0, ftList.lastIndex)
        }
    }

    var wheelIndex by remember { mutableIntStateOf(initialIndex) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // ✅ Changed: Use Theme background
            .background(MaterialTheme.colorScheme.background)
            .padding(16.pDp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection("Your height")

        Spacer(modifier = Modifier.height(24.pDp))

        UnitSelector(isCm = isCm, onToggle = {
            val currentSelectionCm = if (isCm) {
                cmList[wheelIndex].toInt()
            } else {
                footStringToCm(ftList[wheelIndex])
            }
            isCm = !isCm
            wheelIndex = if (isCm) {
                (currentSelectionCm - 100).coerceIn(0, cmList.lastIndex)
            } else {
                val ftStr = cmToFootString(currentSelectionCm)
                ftList.indexOf(ftStr).coerceIn(0, ftList.lastIndex)
            }
        })

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            // Use key to force recomposition when unit changes
            key(isCm) {
                WheelPicker(
                    items = if (isCm) cmList else ftList,
                    initialIndex = wheelIndex,
                    onSelectionChanged = { newIndex -> wheelIndex = newIndex }
                )
            }
        }

        NextButton(onClick = {
            val finalCm = if (isCm) cmList[wheelIndex].toInt() else footStringToCm(ftList[wheelIndex])
            onSaveClick(finalCm, if (isCm) "cm" else "ft")
        })
    }
}

// ==========================================
// 3. WEIGHT SCREEN
// ==========================================
@Composable
fun WeightSelectionScreen(
    onNextClick: (Float) -> Unit,
    title: String = "Your Target weight",
    initialWeight: Float = 70f
) {
    InitAppScaler()
    val startWeight = 30
    val endWeight = 150
    val weightList = remember { (startWeight..endWeight).map { "$it" } }
    val calculatedIndex = (initialWeight.toInt() - startWeight).coerceIn(0, weightList.lastIndex)
    var selectedIndex by remember { mutableIntStateOf(calculatedIndex) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // ✅ Changed: Use Theme background
            .background(MaterialTheme.colorScheme.background)
            .padding(16.pDp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection(title)

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            WheelPicker(
                items = weightList,
                initialIndex = selectedIndex,
                unit = "kg",
                onSelectionChanged = { selectedIndex = it }
            )
        }

        NextButton(onClick = { onNextClick(weightList[selectedIndex].toFloat()) })
    }
}

@Composable
fun DecimalWeightSelectionScreen(
    onNextClick: (Float) -> Unit,
    title: String = "Your current weight",
    initialWeight: Float = 70.0f
) {
    InitAppScaler()

    val wholeNumbers = remember { (30..150).map { "$it" } }
    val decimals = remember { (0..9).map { ".$it" } }

    val initialWhole = initialWeight.toInt()
    val initialDecimal = ((initialWeight - initialWhole) * 10).roundToInt().coerceIn(0, 9)

    var selectedWholeIndex by remember {
        mutableIntStateOf((initialWhole - 30).coerceIn(0, wholeNumbers.lastIndex))
    }
    var selectedDecimalIndex by remember {
        mutableIntStateOf(initialDecimal)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // ✅ Changed: Use Theme background
            .background(MaterialTheme.colorScheme.background)
            .padding(16.pDp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection(title)

        // --- DUAL WHEEL CONTAINER ---
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // LEFT WHEEL: Whole Numbers (kg)
                Box(modifier = Modifier.weight(1f)) {
                    WheelPicker(
                        items = wholeNumbers,
                        initialIndex = selectedWholeIndex,
                        onSelectionChanged = { selectedWholeIndex = it }
                    )
                }

                // RIGHT WHEEL: Decimal points
                Box(modifier = Modifier.weight(0.7f)) {
                    WheelPicker(
                        items = decimals,
                        initialIndex = selectedDecimalIndex,
                        onSelectionChanged = { selectedDecimalIndex = it }
                    )
                }
                // STATIC KG LABEL
                Text(
                    text = "kg",
                    fontSize = 24.pSp,
                    fontWeight = FontWeight.Bold,
                    // ✅ Changed: Use Theme onBackground
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 8.pDp)
                )
            }
        }

        NextButton(onClick = {
            val wholePart = wholeNumbers[selectedWholeIndex].toFloat()
            val decimalPart = decimals[selectedDecimalIndex].toFloat()
            onNextClick(wholePart + decimalPart)
        })
    }
}

// ==========================================
// 4. AGE SCREEN
// ==========================================
@Composable
fun AgeSelectionScreen(onSaveClick: (Int) -> Unit) {
    InitAppScaler()
    val ageList = remember { (14..100).map { "$it" } }
    var selectedIndex by remember { mutableIntStateOf(10) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // ✅ Changed: Use Theme background
            .background(MaterialTheme.colorScheme.background)
            .padding(16.pDp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection("Your age")

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            WheelPicker(
                items = ageList,
                initialIndex = selectedIndex,
                onSelectionChanged = { selectedIndex = it }
            )
        }

        NextButton(text = "Save", onClick = { onSaveClick(ageList[selectedIndex].toInt()) })
    }
}

// ==========================================
// 🛠️ THE UNIVERSAL WHEEL PICKER (SCALED)
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    unit: String = "",
    onSelectionChanged: (Int) -> Unit
) {
    val itemHeight = 60.pDp
    val visibleItems = 5
    val pickerHeight = itemHeight * visibleItems
    val spacerHeight = (pickerHeight - itemHeight) / 2

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Using snapshotFlow or derivedStateOf logic remains the same
    val currentIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val centerOffset = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val centerItem = layoutInfo.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2) - centerOffset)
            }
            (centerItem?.index?.minus(1))?.coerceIn(0, items.lastIndex) ?: 0
        }
    }

    LaunchedEffect(currentIndex) {
        onSelectionChanged(currentIndex)
    }

    LazyColumn(
        state = listState,
        flingBehavior = snapBehavior,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.height(pickerHeight).fillMaxWidth()
    ) {
        item { Spacer(modifier = Modifier.height(spacerHeight)) }
        items(count = items.size) { index ->
            val isSelected = currentIndex == index
            val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == index + 1 }
            var scale = 0.7f
            var opacity = 0.3f

            if (itemInfo != null) {
                val centerOffset = listState.layoutInfo.viewportEndOffset / 2
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                val distance = abs(centerOffset - itemCenter)
                val maxDistance = (itemHeight.value * (visibleItems / 2f))
                val normalized = (distance / maxDistance).coerceIn(0f, 1f)
                scale = 1f - (normalized * 0.3f)
                opacity = 1f - (normalized * 0.7f)
            }

            Box(
                modifier = Modifier.height(itemHeight).fillMaxWidth().graphicsLayer {
                    scaleX = scale; scaleY = scale; alpha = opacity
                },
                contentAlignment = Alignment.Center
            ) {
                val text = if(unit.isNotEmpty()) "${items[index]} $unit" else items[index]
                Text(
                    text = text,
                    fontSize = 32.pSp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    // ✅ Changed: Use onBackground for selected, onSurfaceVariant for unselected
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        item { Spacer(modifier = Modifier.height(spacerHeight)) }
    }
}

// ==========================================
// 🧩 HELPER COMPONENTS (SCALED)
// ==========================================
@Composable
fun HeaderSection(title: String) {
    Spacer(modifier = Modifier.height(16.pDp))
    // ✅ Changed: Use onSurfaceVariant for subtitle
    Text("Body Data", fontSize = 18.pSp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(40.pDp))
    // ✅ Changed: Use onBackground for main title
    Text(title, fontSize = 26.pSp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun NextButton(text: String = "Next", onClick: () -> Unit) {
    Spacer(modifier = Modifier.height(16.pDp))
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.pDp),
        // ✅ Changed: Use Primary Theme Colors
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(25.pDp)
    ) {
        Text(text, fontSize = 16.pSp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(16.pDp))
}

@Composable
fun UnitSelector(isCm: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .width(160.pDp)
            .height(40.pDp)
            .clip(RoundedCornerShape(20.pDp))
            // ✅ Changed: Use Surface Variant (Grey) for background
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onToggle() }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Side (FT)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.pDp)
                    .clip(RoundedCornerShape(16.pDp))
                    // ✅ Changed: Highlight with Primary if selected
                    .background(if (!isCm) MaterialTheme.colorScheme.primary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "FT",
                    // ✅ Changed: Text color adapts
                    color = if (!isCm) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.pSp
                )
            }

            // Right Side (CM)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.pDp)
                    .clip(RoundedCornerShape(16.pDp))
                    // ✅ Changed: Highlight with Primary if selected
                    .background(if (isCm) MaterialTheme.colorScheme.primary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "CM",
                    // ✅ Changed: Text color adapts
                    color = if (isCm) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.pSp
                )
            }
        }
    }
}

// 📏 UTILS
fun generateFeetList(): List<String> = (4..7).flatMap { feet -> (0..11).map { inch -> "$feet'$inch\"" } }

fun cmToFootString(cm: Int): String {
    if (cm == 0) return "0'0\""
    val totalInches = cm / 2.54
    val feet = (totalInches / 12).toInt()
    val inches = (totalInches % 12).roundToInt()
    return if (inches == 12) "${feet + 1}'0\"" else "$feet'$inches\""
}

fun footStringToCm(ftStr: String): Int {
    return try {
        val parts = ftStr.replace("\"", "").split("'")
        val totalInches = (parts[0].toInt() * 12) + parts[1].toInt()
        (totalInches * 2.54).roundToInt()
    } catch (e: Exception) { 170 }
}