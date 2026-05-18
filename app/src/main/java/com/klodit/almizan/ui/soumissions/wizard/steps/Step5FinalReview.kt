package com.klodit.almizan.ui.soumissions.wizard.steps

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.klodit.almizan.R
import com.klodit.almizan.model.BidWizardState
import com.klodit.almizan.ui.bidwizard.components.NavButtons
import com.klodit.almizan.ui.bidwizard.components.SectionTitle
import com.klodit.almizan.ui.theme.*

@Composable
fun Step5FinalReview(
    state: BidWizardState,
    localizedContext: Context,
    isSubmitting: Boolean,
    onCertificationToggle: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        SectionTitle(
            title = localizedContext.getString(R.string.wizard_step6_title),
            subtitle = localizedContext.getString(R.string.wizard_step6_subtitle)
        )

        // Incomplete warning
        if (!state.canSubmit) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF57C00).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            null,
                            tint = Color(0xFFF57C00),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = localizedContext.getString(R.string.wizard_review_incomplete),
                        fontSize = 12.sp,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Summary Cards Grid — Top Row: AO + Lot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // AO Card
            SummaryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Inventory2,
                iconBgColor = Blue700.copy(alpha = 0.12f),
                iconColor = Blue700,
                label = localizedContext.getString(R.string.wizard_review_ao),
                value = state.appelOffreReference.ifEmpty { "-" },
                subtitle = state.appelOffreObjet.take(40)
            )
            // Lot Card
            SummaryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Gavel,
                iconBgColor = Green500.copy(alpha = 0.12f),
                iconColor = Green600,
                label = localizedContext.getString(R.string.wizard_review_lot),
                value = state.selectedLot?.let {
                    localizedContext.getString(R.string.wizard_lot_prefix, it.numero.toString())
                } ?: "-",
                subtitle = state.selectedLot?.designation?.take(30)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Tech Offer Card
        SummaryCard(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Description,
            iconBgColor = Navy50,
            iconColor = Navy600,
            label = localizedContext.getString(R.string.wizard_step3_title),
            value = state.offreTechnique?.document?.nom
                ?: localizedContext.getString(R.string.wizard_not_provided),
            statusBadge = if (state.offreTechnique?.document != null) SummaryBadgeType.SUCCESS else SummaryBadgeType.MISSING,
            badgeLabel = if (state.offreTechnique?.document != null)
                localizedContext.getString(R.string.wizard_provided) else
                localizedContext.getString(R.string.wizard_not_provided)
        )

        Spacer(Modifier.height(16.dp))

        // Caution Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyWhite),
            elevation = CardDefaults.cardElevation(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Grey200)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Green500.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.AccountBalance,
                            null,
                            tint = Green600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        localizedContext.getString(R.string.wizard_guarantee).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy500,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Grey100)
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            localizedContext.getString(R.string.wizard_ref_label_format, state.caution.reference.ifEmpty { "-" }),
                            fontSize = 12.sp,
                            color = Navy600
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            state.caution.banque.ifEmpty { "-" },
                            fontSize = 11.sp,
                            color = Navy400
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Green50
                    ) {
                        Text(
                            localizedContext.getString(R.string.wizard_amount_dzd_format, state.caution.montant.ifEmpty { "0" }),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Security Notice Card
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
                    Icon(Icons.Outlined.Lock, null, tint = Blue700, modifier = Modifier.size(18.dp))
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

        // Certification Area — distinct background
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (state.certificationAccepted) Green50 else Color(0xFFFFF8E1)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (state.certificationAccepted) Green500.copy(alpha = 0.3f) else Color(0xFFFFE082)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCertificationToggle(!state.certificationAccepted) }
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = state.certificationAccepted,
                    onCheckedChange = { onCertificationToggle(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Green500,
                        uncheckedColor = Navy400
                    )
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.VerifiedUser,
                            null,
                            tint = if (state.certificationAccepted) Green600 else Color(0xFFF57C00),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = localizedContext.getString(R.string.wizard_guarantee).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.certificationAccepted) Green700 else Color(0xFFE65100),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = localizedContext.getString(R.string.wizard_certification_text),
                        fontSize = 12.sp,
                        color = Navy700,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Ready indicator
        if (state.canSubmit && !isSubmitting) {
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Green50,
                border = androidx.compose.foundation.BorderStroke(1.dp, Green500.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        null,
                        tint = Green600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        localizedContext.getString(R.string.wizard_review_ready),
                        fontSize = 13.sp,
                        color = Green700,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        NavButtons(
            localizedContext = localizedContext,
            onBack = onBack,
            onNext = onSubmit,
            nextLabel = if (isSubmitting) localizedContext.getString(R.string.wizard_submit_processing) else localizedContext.getString(R.string.wizard_submit_final),
            isLast = true,
            disabled = !state.canSubmit || isSubmitting
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  REUSABLE SUMMARY CARD
// ─────────────────────────────────────────────────────────────────────────────

enum class SummaryBadgeType { SUCCESS, MISSING }

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    label: String,
    value: String,
    subtitle: String? = null,
    statusBadge: SummaryBadgeType? = null,
    badgeLabel: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Grey200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy500,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            subtitle?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    fontSize = 11.sp,
                    color = Navy500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            statusBadge?.let { badge ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (badge == SummaryBadgeType.SUCCESS) Green50 else Red50
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            if (badge == SummaryBadgeType.SUCCESS) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = if (badge == SummaryBadgeType.SUCCESS) Green700 else Red600,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = badgeLabel ?: "",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (badge == SummaryBadgeType.SUCCESS) Green700 else Red600
                        )
                    }
                }
            }
        }
    }
}
