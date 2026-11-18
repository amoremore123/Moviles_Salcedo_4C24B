package com.tecsup.campusgps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Selector de radio de cercanía (100 / 200 / 500 m). Solo dibuja y avisa la elección.
 */
@Composable
fun SelectorRadio(
    radios: List<Int>,
    actual: Int,
    onCambiar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Radio", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
            radios.forEachIndexed { i, metros ->
                SegmentedButton(
                    selected = metros == actual,
                    onClick = { onCambiar(metros) },
                    shape = SegmentedButtonDefaults.itemShape(index = i, count = radios.size)
                ) {
                    Text("$metros m")
                }
            }
        }
    }
}
