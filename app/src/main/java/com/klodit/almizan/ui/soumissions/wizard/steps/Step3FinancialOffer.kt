package com.klodit.almizan.ui.soumissions.wizard.steps

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.klodit.almizan.R
import com.klodit.almizan.model.BidWizardState
import com.klodit.almizan.model.BpuLine
import com.klodit.almizan.ui.bidwizard.components.NavButtons
import com.klodit.almizan.ui.bidwizard.components.SectionTitle
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.BidWizardViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun Step3FinancialOffer(
    state: BidWizardState,
    localizedContext: Context,
    viewModel: BidWizardViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale.FRANCE).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun parseAmount(value: String): Double {
        return value.replace("\\s".toRegex(), "").replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    fun calculateLineTotal(line: BpuLine): Double {
        val qty = parseAmount(line.quantite)
        val price = parseAmount(line.prixUnitaire)
        return qty * price
    }

    val grandTotal = state.lotBpus.sumOf { bpu ->
        bpu.lines.sumOf { calculateLineTotal(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        SectionTitle(
            title = localizedContext.getString(R.string.wizard_step4_title),
            subtitle = localizedContext.getString(R.string.wizard_step4_subtitle)
        )

        // Security Banner — premium card style
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
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = Blue700, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = localizedContext.getString(R.string.wizard_fin_security_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue800
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = localizedContext.getString(R.string.wizard_fin_security_desc),
                        fontSize = 11.sp,
                        color = Blue700,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Dynamic BPU Cards
        state.lotBpus.forEach { bpu ->
            val lotTotal = bpu.lines.sumOf { calculateLineTotal(it) }
            val lotName = state.availableLots.find { it.id == bpu.lotId }?.numero?.toString() ?: "1"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Grey200)
            ) {
                // Table Header — Navy50 background
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Navy50)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Green500.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Payments,
                                contentDescription = null,
                                tint = Green600,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Green500.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = localizedContext.getString(R.string.wizard_lot_prefix, lotName),
                                color = Green700,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            localizedContext.getString(R.string.wizard_total_ht).uppercase(),
                            fontSize = 9.sp,
                            color = Navy400,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            localizedContext.getString(R.string.wizard_amount_dzd_format, formatter.format(lotTotal)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                    }
                }

                // BPU Lines
                bpu.lines.forEachIndexed { index, line ->
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Line header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Navy50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy500
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    localizedContext.getString(R.string.wizard_prestation_number, index + 1),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy700
                                )
                            }
                            if (bpu.lines.size > 1) {
                                Surface(
                                    onClick = { viewModel.removeBpuLine(bpu.lotId, line.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Red50
                                ) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = localizedContext.getString(R.string.wizard_bpu_delete),
                                        tint = Red600,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Designation field
                        OutlinedTextField(
                            value = line.designation,
                            onValueChange = { viewModel.updateBpuLine(bpu.lotId, line.id, "designation", it) },
                            label = { Text(localizedContext.getString(R.string.wizard_bpu_designation), fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Grey200,
                                focusedBorderColor = Green500,
                                unfocusedContainerColor = NavyWhite,
                                focusedContainerColor = NavyWhite
                            )
                        )

                        Spacer(Modifier.height(10.dp))

                        // Unit + Quantity row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = line.unite,
                                onValueChange = { viewModel.updateBpuLine(bpu.lotId, line.id, "unite", it) },
                                label = { Text(localizedContext.getString(R.string.wizard_bpu_unit), fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Grey200,
                                    focusedBorderColor = Green500,
                                    unfocusedContainerColor = NavyWhite,
                                    focusedContainerColor = NavyWhite
                                )
                            )
                            OutlinedTextField(
                                value = line.quantite,
                                onValueChange = { viewModel.updateBpuLine(bpu.lotId, line.id, "quantite", it) },
                                label = { Text(localizedContext.getString(R.string.wizard_bpu_qty), fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Grey200,
                                    focusedBorderColor = Green500,
                                    unfocusedContainerColor = NavyWhite,
                                    focusedContainerColor = NavyWhite
                                )
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Unit price
                        OutlinedTextField(
                            value = line.prixUnitaire,
                            onValueChange = { viewModel.updateBpuLine(bpu.lotId, line.id, "prixUnitaire", it) },
                            label = { Text(localizedContext.getString(R.string.wizard_bpu_unit_price), fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            trailingIcon = {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Navy50
                                ) {
                                    Text(
                                        localizedContext.getString(R.string.wizard_currency_dzd),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy500,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Grey200,
                                focusedBorderColor = Green500,
                                unfocusedContainerColor = NavyWhite,
                                focusedContainerColor = NavyWhite
                            )
                        )

                        // Line total
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy30)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${localizedContext.getString(R.string.wizard_bpu_total_ht)} : ",
                                fontSize = 11.sp,
                                color = Navy500
                            )
                            Text(
                                localizedContext.getString(R.string.wizard_amount_dzd_format, formatter.format(calculateLineTotal(line))),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        }
                    }
                    if (index < bpu.lines.size - 1) {
                        HorizontalDivider(color = Grey100, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                // Add Line Button
                HorizontalDivider(color = Grey100)
                Surface(
                    onClick = { viewModel.addBpuLine(bpu.lotId) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Navy30,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, null, tint = Green600, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            localizedContext.getString(R.string.wizard_bpu_add_line),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green600
                        )
                    }
                }
            }
        }

        // Grand Total — always visible
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Green50),
            border = androidx.compose.foundation.BorderStroke(1.dp, Green500.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        localizedContext.getString(R.string.wizard_bpu_grand_total).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy600,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = localizedContext.getString(R.string.wizard_total_ht),
                        fontSize = 11.sp,
                        color = Navy500
                    )
                }
                Text(
                    localizedContext.getString(R.string.wizard_amount_dzd_format, formatter.format(grandTotal)),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Security footer
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
                    localizedContext.getString(R.string.wizard_fin_local_prep),
                    fontSize = 11.sp,
                    color = Navy500
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        NavButtons(
            localizedContext = localizedContext,
            onBack = onBack,
            onNext = onNext,
            disabled = !state.canProceedToStep4
        )
    }
}
