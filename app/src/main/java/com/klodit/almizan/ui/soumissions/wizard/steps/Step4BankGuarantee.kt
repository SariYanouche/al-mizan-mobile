package com.klodit.almizan.ui.soumissions.wizard.steps

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.klodit.almizan.R
import com.klodit.almizan.model.BidWizardState
import com.klodit.almizan.ui.bidwizard.components.NavButtons
import com.klodit.almizan.ui.bidwizard.components.SectionTitle
import com.klodit.almizan.ui.bidwizard.components.WizardField
import com.klodit.almizan.ui.bidwizard.components.WizardInput
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.BidWizardViewModel
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step4BankGuarantee(
    state: BidWizardState,
    localizedContext: Context,
    viewModel: BidWizardViewModel,
    onUploadClick: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    // Date picker state
    var showEmissionPicker by remember { mutableStateOf(false) }
    var showExpirationPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        SectionTitle(
            title = localizedContext.getString(R.string.wizard_step5_title),
            subtitle = localizedContext.getString(R.string.wizard_step5_subtitle)
        )

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Blue50),
            border = androidx.compose.foundation.BorderStroke(1.dp, BlueBorder),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Blue700.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = localizedContext.getString(R.string.wizard_caution_info),
                    fontSize = 12.sp,
                    color = Blue800,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Form Fields Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyWhite),
            elevation = CardDefaults.cardElevation(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Grey200)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Reference
                WizardField(label = localizedContext.getString(R.string.wizard_caution_ref), isRequired = true) {
                    WizardInput(
                        value = state.caution.reference,
                        onValueChange = { viewModel.updateCaution(state.caution.copy(reference = it)) },
                        placeholder = localizedContext.getString(R.string.wizard_caution_ref_hint)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Bank + Amount in grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        WizardField(label = localizedContext.getString(R.string.wizard_caution_bank), isRequired = true) {
                            WizardInput(
                                value = state.caution.banque,
                                onValueChange = { viewModel.updateCaution(state.caution.copy(banque = it)) },
                                placeholder = localizedContext.getString(R.string.wizard_caution_bank_hint)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        WizardField(label = localizedContext.getString(R.string.wizard_caution_amount), isRequired = true) {
                            WizardInput(
                                value = state.caution.montant,
                                onValueChange = { viewModel.updateCaution(state.caution.copy(montant = it)) },
                                placeholder = localizedContext.getString(R.string.wizard_caution_amount_hint)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                HorizontalDivider(color = Grey100)

                Spacer(Modifier.height(16.dp))

                // Date fields header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = Navy400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = localizedContext.getString(R.string.wizard_caution_emission).uppercase() + " / " +
                               localizedContext.getString(R.string.wizard_caution_expiry).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy500,
                        letterSpacing = 0.5.sp
                    )
                }

                // Date fields — clickable OutlinedTextFields with DatePickerDialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        WizardField(label = localizedContext.getString(R.string.wizard_caution_emission), isRequired = true) {
                            OutlinedTextField(
                                value = state.caution.dateEmission,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                placeholder = { Text(localizedContext.getString(R.string.wizard_date_hint), fontSize = 13.sp, color = Navy400) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = Green500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clickable { showEmissionPicker = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Navy900,
                                    disabledBorderColor = Grey200,
                                    disabledPlaceholderColor = Navy400,
                                    disabledContainerColor = NavyWhite,
                                    disabledTrailingIconColor = Green500
                                )
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        WizardField(label = localizedContext.getString(R.string.wizard_caution_expiry), isRequired = true) {
                            OutlinedTextField(
                                value = state.caution.dateExpiration,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                placeholder = { Text(localizedContext.getString(R.string.wizard_date_hint), fontSize = 13.sp, color = Navy400) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = Green500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clickable { showExpirationPicker = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Navy900,
                                    disabledBorderColor = Grey200,
                                    disabledPlaceholderColor = Navy400,
                                    disabledContainerColor = NavyWhite,
                                    disabledTrailingIconColor = Green500
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // Upload Area — Dashed border dropzone
        Text(
            text = "${localizedContext.getString(R.string.wizard_caution_scan_label).uppercase()} *",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Navy500,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(10.dp))

        val hasFile = state.caution.document != null
        val dashedBorderColor = if (hasFile) Green500 else Grey300
        val dropBgColor = if (hasFile) Green50 else Navy30

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(dropBgColor)
                .drawBehind {
                    val stroke = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                    drawRoundRect(
                        color = dashedBorderColor,
                        style = stroke,
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
                .clickable { onUploadClick() }
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hasFile) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        null,
                        tint = Green500,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        state.caution.document!!.nom,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Green700
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${(state.caution.document!!.tailleOctets) / 1024} KB",
                        fontSize = 11.sp,
                        color = Navy400
                    )
                } else {
                    Icon(
                        Icons.Outlined.CloudUpload,
                        null,
                        tint = Navy400,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        localizedContext.getString(R.string.wizard_caution_dropzone_label),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Navy700
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        localizedContext.getString(R.string.wizard_caution_dropzone_sub),
                        fontSize = 11.sp,
                        color = Navy400,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Security note
        Spacer(Modifier.height(16.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Navy30
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Shield, null, tint = Navy400, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    localizedContext.getString(R.string.wizard_caution_security_note),
                    fontSize = 11.sp,
                    color = Navy500,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        NavButtons(
            localizedContext = localizedContext,
            onBack = onBack,
            onNext = onNext,
            disabled = !state.canProceedToStep5
        )
    }

    // ── DATE PICKER DIALOGS ──

    if (showEmissionPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEmissionPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatted = formatMillisToDdMmYyyy(millis)
                        viewModel.updateCaution(state.caution.copy(dateEmission = formatted))
                    }
                    showEmissionPicker = false
                }) {
                    Text(localizedContext.getString(R.string.wizard_next), color = Green500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmissionPicker = false }) {
                    Text(localizedContext.getString(R.string.wizard_back), color = Navy500)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = NavyWhite)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Green500,
                    todayDateBorderColor = Green500,
                    todayContentColor = Green700
                )
            )
        }
    }

    if (showExpirationPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showExpirationPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatted = formatMillisToDdMmYyyy(millis)
                        viewModel.updateCaution(state.caution.copy(dateExpiration = formatted))
                    }
                    showExpirationPicker = false
                }) {
                    Text(localizedContext.getString(R.string.wizard_next), color = Green500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpirationPicker = false }) {
                    Text(localizedContext.getString(R.string.wizard_back), color = Navy500)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = NavyWhite)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Green500,
                    todayDateBorderColor = Green500,
                    todayContentColor = Green700
                )
            )
        }
    }
}

/**
 * Converts millis (from DatePicker) to DD/MM/YYYY string.
 * The SoumissionRepository.toIsoDate() handles conversion to ISO 8601 on submission.
 */
private fun formatMillisToDdMmYyyy(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}
