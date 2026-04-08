package com.yourgame.tcg.ui.components

import androidx.compose.foundation.background

import androidx.compose.foundation.gestures.detectDragGestures

import androidx.compose.foundation.layout.*

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier

import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.input.pointer.consume

import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.unit.IntOffset

import androidx.compose.ui.unit.dp

import com.yourgame.tcg.vm.GameViewModel

@Composable

fun DraggableCard(cardId: Int, vm: GameViewModel) {

    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(

        Modifier

            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }

            .pointerInput(Unit) {

                detectDragGestures { change, drag ->

                    change.consume()

                    offset += drag

                    vm.moveCard(cardId, offset.x, offset.y, 0)

                }

            }

            .size(120.dp)

            .background(Color.White)

    )

}
 