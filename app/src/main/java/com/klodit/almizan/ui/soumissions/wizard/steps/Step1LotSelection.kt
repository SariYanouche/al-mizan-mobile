package com.klodit.almizan.ui.soumissions.wizard.steps

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.klodit.almizan.R
import com.klodit.almizan.model.BidWizardState
import com.klodit.almizan.ui.bidwizard.components.NavButtons
import com.klodit.almizan.ui.bidwizard.components.SectionTitle
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.BidWizardViewModel

@Composable
fun Step1LotSelection(
    state: BidWizardState,
    localizedContext: Context,
    viewModel: BidWizardViewModel,
    onLotSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    val lots = state.availableLots
    val selectedLotId = state.selectedLotId

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        SectionTitle(
            title = localizedContext.getString(R.string.wizard_step1_title),
            subtitle = localizedContext.getString(R.string.wizard_step1_subtitle)
        )

        // AO Summary Banner
        if (state.appelOffreReference.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Blue50,
                border = androidx.compose.foundation.BorderStroke(1.dp, BlueBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Blue700.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = Blue700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.appelOffreReference,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = state.appelOffreObjet,
                            fontSize = 12.sp,
                            color = Navy500,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // Lots Label
        Text(
            text = localizedContext.getString(R.string.wizard_select_lot).uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Navy500,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))

        // Lot Cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            lots.forEach { lot ->
                val isSelected = lot.id == selectedLotId
                val animatedBorderColor by animateColorAsState(
                    targetValue = if (isSelected) Green500 else Grey200,
                    label = "borderColor"
                )
                val animatedBgColor by animateColorAsState(
                    targetValue = if (isSelected) Green50 else NavyWhite,
                    label = "bgColor"
                )

                Card(
                    onClick = { onLotSelected(lot.id) },
                    colors = CardDefaults.cardColors(containerColor = animatedBgColor),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = animatedBorderColor
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Selection indicator circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Green500 else Grey200
                                )
                                .then(
                                    if (!isSelected) Modifier.border(1.5.dp, Grey300, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NavyWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        // Lot details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = localizedContext.getString(R.string.wizard_lot_number, lot.numero),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Green500.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = localizedContext.getString(R.string.wizard_selected_badge),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Green700,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = lot.designation,
                                fontSize = 12.sp,
                                color = Navy500,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                            lot.montantEstime?.let { montant ->
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Navy50
                                ) {
                                    Text(
                                        text = "${localizedContext.getString(R.string.wizard_estimated_amount)} $montant DZD",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Navy600,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        NavButtons(
            localizedContext = localizedContext,
            onNext = onNext,
            disabled = !state.canProceedToStep2
        )
    }
}