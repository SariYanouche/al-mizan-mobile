package com.klodit.almizan.ui.bidwizard.components

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.*
import androidx.compose.ui.draw.drawBehind


@Composable
fun StepIndicator(currentStep: Int, localizedContext: Context) {
    val steps = listOf(
        1 to R.string.wizard_step_label_ao,
        2 to R.string.wizard_step_label_tech,
        3 to R.string.wizard_step_label_fin,
        4 to R.string.wizard_step_label_caution,
        5 to R.string.wizard_step_label_review
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, (stepNum, labelRes) ->
            val isCompleted = stepNum < currentStep
            val isActive = stepNum == currentStep

            // Animated colors
            val circleBg by animateColorAsState(
                targetValue = when {
                    isCompleted -> Green500
                    isActive -> Green500
                    else -> Color.Transparent
                },
                animationSpec = tween(durationMillis = 350),
                label = "circleBg"
            )
            val circleBorder by animateColorAsState(
                targetValue = when {
                    isCompleted || isActive -> Green500
                    else -> Grey200
                },
                animationSpec = tween(durationMillis = 350),
                label = "circleBorder"
            )
            val contentColor by animateColorAsState(
                targetValue = when {
                    isCompleted || isActive -> Color.White
                    else -> Navy400
                },
                animationSpec = tween(durationMillis = 350),
                label = "contentColor"
            )
            val labelColor by animateColorAsState(
                targetValue = when {
                    isCompleted -> Green700
                    isActive -> Green700
                    else -> Navy400
                },
                animationSpec = tween(durationMillis = 350),
                label = "labelColor"
            )

            // Step circle + label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(circleBg)
                        .border(1.5.dp, circleBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = stepNum.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = localizedContext.getString(labelRes),
                    fontSize = 9.sp,
                    fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Medium,
                    color = labelColor,
                    maxLines = 1
                )
            }

            // Connecting line between steps
            if (index < steps.size - 1) {
                val nextCompleted = (stepNum + 1) <= currentStep
                val lineFraction by animateFloatAsState(
                    targetValue = when {
                        isCompleted && nextCompleted -> 1f
                        isCompleted && !nextCompleted -> 1f
                        else -> 0f
                    },
                    animationSpec = tween(durationMillis = 400),
                    label = "lineFraction"
                )
                val lineTrackColor = Grey200
                val lineFillColor = Green500.copy(alpha = 0.6f)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(lineTrackColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(lineFraction)
                            .background(lineFillColor)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Navy900
        )
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Navy500,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun NavButtons(
    localizedContext: Context,
    onBack: (() -> Unit)? = null,
    onNext: () -> Unit,
    nextLabel: String? = null,
    isLast: Boolean = false,
    disabled: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .drawBehind {
                drawLine(
                    color = Grey100,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(top = 16.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Grey200),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy500)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(localizedContext.getString(R.string.wizard_back), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Spacer(Modifier.width(1.dp))
        }

        Button(
            onClick = onNext,
            enabled = !disabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLast) Green700 else Green500,
                disabledContainerColor = Grey200,
                disabledContentColor = Navy400
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        ) {
            val label = nextLabel ?: if (isLast) localizedContext.getString(R.string.wizard_submit_final) else localizedContext.getString(R.string.wizard_next)
            if (isLast) {
                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (!isLast) {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun FileDropzone(
    label: String,
    sublabel: String,
    fileName: String?,
    fileSize: Long?,
    onFileClick: () -> Unit
) {
    val borderColor = if (fileName != null) Green500 else Grey200
    val bgColor = if (fileName != null) Green50 else Navy50

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onFileClick() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (fileName != null) {
                Icon(Icons.Outlined.CheckCircle, null, tint = Green500, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(8.dp))
                Text(fileName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Green700)
                Spacer(Modifier.height(2.dp))
                Text("${(fileSize ?: 0) / 1024} KB", fontSize = 11.sp, color = Navy400)
            } else {
                Icon(Icons.Outlined.CloudUpload, null, tint = Navy400, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(8.dp))
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Navy700)
                Spacer(Modifier.height(2.dp))
                Text(sublabel, fontSize = 11.sp, color = Navy400, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun WizardField(label: String, isRequired: Boolean = false, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Navy500,
                letterSpacing = 0.5.sp
            )
            if (isRequired) {
                Text("*", color = Red600, fontSize = 12.sp, modifier = Modifier.padding(start = 2.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
fun WizardInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 13.sp, color = Navy400) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = if (isError) Red600 else Grey200,
            focusedBorderColor = if (isError) Red600 else Green500,
            unfocusedContainerColor = NavyWhite,
            focusedContainerColor = NavyWhite
        )
    )
}