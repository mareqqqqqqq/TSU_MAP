package com.example.tsuappmap

import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private data class LayoutNode(
    val id: Int,
    val label: String,
    val isLeaf: Boolean,
    val x: Float,
    val y: Float,
    val children: List<Pair<String, Int>>
)

private fun layoutTree(
    root: TreeNode, nodeRadius: Float = 48f, levelHeight: Float = 130f, minHGap: Float = 24f
): Pair<List<LayoutNode>, Pair<Float, Float>> {
    var idCounter = 0 // индексы узла типо айдишка только номер)
    val nodes = mutableListOf<LayoutNode>() // ищменяемый список с нодами

    fun buildLayout(node: TreeNode, depth: Int, xOffset: Float): Pair<Int, Float> {
        val myId = idCounter++
        val y = depth * levelHeight + nodeRadius + 20f

        return when (node) {
            is TreeNode.Leaf -> {
                val label = localPlace(node.label)
                val width = maxOf(nodeRadius * 2 + minHGap, label.length * 9f + minHGap)
                nodes.add(LayoutNode(myId, label, true, xOffset + width / 2, y, emptyList()))
                Pair(myId, width)
            }

            is TreeNode.Decision -> {
                val label = localFeature(node.feature).split(" ").take(2).joinToString("\n")
                val sortedBranches = node.branches.entries.sortedBy { it.key }
                val childResults = mutableListOf<Triple<String, Int, Float>>()
                var totalChildWidth = 0f

                for ((branchVal, child) in sortedBranches) {
                    val (childId, childWidth) = buildLayout(
                        child, depth + 1, xOffset + totalChildWidth
                    )
                    childResults.add(Triple(localValue(branchVal), childId, childWidth))
                    totalChildWidth += childWidth
                }

                val myX = xOffset + totalChildWidth / 2
                val children = childResults.map { Pair(it.first, it.second) }
                nodes.add(LayoutNode(myId, label, false, myX, y, children))
                Pair(myId, totalChildWidth)
            }
        }
    }

    val (_, totalWidth) = buildLayout(root, 0, 0f)
    val maxDepth = nodes.maxOf { it.y }
    return Pair(nodes.sortedBy { it.id }, Pair(totalWidth, maxDepth + nodeRadius + 20f))
}

private fun DrawScope.drawArrow(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    color: Int,
    strokeWidth: Float,
    arrowSize: Float
) {
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.color = color
            this.strokeWidth = strokeWidth
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
        }
        drawLine(fromX, fromY, toX, toY, paint)

        val angle = atan2((toY - fromY).toDouble(), (toX - fromX).toDouble())
        val arrowAngle = Math.toRadians(25.0)
        val fillPaint = android.graphics.Paint().apply {
            this.color = color
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }
        val path = android.graphics.Path().apply {
            moveTo(toX, toY)
            lineTo(
                (toX - arrowSize * cos(angle - arrowAngle)).toFloat(),
                (toY - arrowSize * sin(angle - arrowAngle)).toFloat()
            )
            lineTo(
                (toX - arrowSize * cos(angle + arrowAngle)).toFloat(),
                (toY - arrowSize * sin(angle + arrowAngle)).toFloat()
            )
            close()
        }
        drawPath(path, fillPaint)
    }
}

@Composable
fun TreeGraphContent(tree: TreeNode) {
    val density = LocalDensity.current
    val nodeRadiusDp = 32.dp
    val levelHeightDp = 140.dp
    val minHGapDp = 28.dp

    val nodeRadiusPx = with(density) { nodeRadiusDp.toPx() }
    val levelHeightPx = with(density) { levelHeightDp.toPx() }
    val minHGapPx = with(density) { minHGapDp.toPx() }

    val (nodes, dimensions) = remember(tree) {
        layoutTree(
            tree, nodeRadiusPx, levelHeightPx, minHGapPx
        )
    }
    val (totalWidthPx, totalHeightPx) = dimensions

    val paddingPx = nodeRadiusPx + 16f
    val canvasWidthDp = with(density) { (totalWidthPx + paddingPx * 2).toDp() }
    val canvasHeightDp = with(density) { (totalHeightPx + paddingPx).toDp() }

    val nodeMap = remember(nodes) { nodes.associateBy { it.id } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TsuGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
        ) {
            Canvas(
                modifier = Modifier
                    .width(canvasWidthDp)
                    .height(canvasHeightDp)
            ) {
                val offsetX = paddingPx

                for (node in nodes) {
                    for ((edgeLabel, childId) in node.children) {
                        val child = nodeMap[childId] ?: continue
                        val startX = node.x + offsetX
                        val startY = node.y
                        val endX = child.x + offsetX
                        val endY = child.y

                        val fromX = startX
                        val fromY = startY + nodeRadiusPx
                        val toX = endX
                        val toY = endY - nodeRadiusPx

                        drawArrow(
                            fromX,
                            fromY,
                            toX,
                            toY,
                            color = Color(0xFFAAAAAA).toArgb(),
                            strokeWidth = 2f,
                            arrowSize = 12f
                        )

                        val midX = (fromX + toX) / 2f
                        val midY = (fromY + toY) / 2f

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = TsuAccent.toArgb()
                                textSize = 28f
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                            }
                            val textWidth = paint.measureText(edgeLabel)
                            val bgPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(200, 245, 247, 250)
                                isAntiAlias = true
                            }
                            drawRoundRect(
                                midX - textWidth / 2 - 6f,
                                midY - 18f,
                                midX + textWidth / 2 + 6f,
                                midY + 10f,
                                8f,
                                8f,
                                bgPaint
                            )
                            drawText(edgeLabel, midX, midY, paint)
                        }
                    }
                }

                for (node in nodes) {
                    val cx = node.x + offsetX
                    val cy = node.y

                    drawContext.canvas.nativeCanvas.apply {
                        val shadowPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(40, 0, 0, 0)
                            isAntiAlias = true
                            maskFilter = android.graphics.BlurMaskFilter(
                                8f, android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                        }
                        drawCircle(cx + 3f, cy + 3f, nodeRadiusPx, shadowPaint)

                        val fillPaint = android.graphics.Paint().apply {
                            color = if (node.isLeaf) TsuSuccess.toArgb() else TsuBlue.toArgb()
                            isAntiAlias = true
                        }
                        drawCircle(cx, cy, nodeRadiusPx, fillPaint)

                        val strokePaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            isAntiAlias = true
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 2.5f
                        }
                        drawCircle(cx, cy, nodeRadiusPx, strokePaint)

                        val lines = node.label.split("\n")
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = if (lines.any { it.length > 8 }) 22f else 26f
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        val lineHeight = textPaint.textSize * 1.2f
                        val totalTextHeight = lineHeight * lines.size
                        val startTextY = cy - totalTextHeight / 2f + textPaint.textSize * 0.8f

                        lines.forEachIndexed { idx, line ->
                            drawText(line, cx, startTextY + idx * lineHeight, textPaint)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(TsuBlue)
            )
            Text("Вопрос", fontSize = 11.sp, color = TsuDark)
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(TsuSuccess)
            )
            Text("Ответ", fontSize = 11.sp, color = TsuDark)
        }
    }
}