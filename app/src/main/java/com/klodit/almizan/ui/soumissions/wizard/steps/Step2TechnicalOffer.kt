package com.klodit.almizan.ui.soumissions.wizard.steps

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.klodit.almizan.R
import com.klodit.almizan.model.BidWizardState
import com.klodit.almizan.ui.bidwizard.components.FileDropzone
import com.klodit.almizan.ui.bidwizard.components.NavButtons
import com.klodit.almizan.ui.bidwizard.components.SectionTitle
import com.klodit.almizan.ui.theme.*

@Composable
fun Step2TechnicalOffer(
    state: BidWizardState,
    localizedContext: Context,
    onUploadClick: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        SectionTitle(
            title = localizedContext.getString(R.string.wizard_step3_title),
            subtitle = localizedContext.getString(R.string.wizard_step3_subtitle)
        )

        // Selected Lot Reminder Card
        state.selectedLot?.let { lot ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Navy50),
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
                            .background(Green500.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Green600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "${localizedContext.getString(R.string.wizard_lots_concerned)} :",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy500,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Green500.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = localizedContext.getString(R.string.wizard_lot_label_format, lot.numero, lot.designation),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green700,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Info Box — "Contenu attendu" with Blue50/Blue800 styling
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Blue50),
            border = androidx.compose.foundation.BorderStroke(1.dp, BlueBorder),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Blue700.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.ListAlt,
                            contentDescription = null,
                            tint = Blue700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = localizedContext.getString(R.string.wizard_tech_content),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue800
                    )
                }

                Spacer(Modifier.height(14.dp))

                HorizontalDivider(color = BlueBorder.copy(alpha = 0.5f))

                Spacer(Modifier.height(14.dp))

                val items = listOf(
                    R.string.wizard_tech_content_1,
                    R.string.wizard_tech_content_2,
                    R.string.wizard_tech_content_3,
                    R.string.wizard_tech_content_4,
                    R.string.wizard_tech_content_5
                )

                items.forEachIndexed { index, resId ->
                    Row(
                        modifier = Modifier.padding(bottom = if (index < items.size - 1) 10.dp else 0.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Blue700)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = localizedContext.getString(resId),
                            fontSize = 13.sp,
                            color = Blue800,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // Upload Area
        Text(
            text = "${localizedContext.getString(R.string.wizard_tech_file_label).uppercase()} *",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Navy500,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(10.dp))

        FileDropzone(
            label = localizedContext.getString(R.string.wizard_tech_dropzone_label),
            sublabel = localizedContext.getString(R.string.wizard_tech_dropzone_sub),
            fileName = state.offreTechnique?.document?.nom,
            fileSize = state.offreTechnique?.document?.tailleOctets,
            onFileClick = onUploadClick
        )

        // Security footer
        Spacer(Modifier.height(24.dp))
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
                    text = localizedContext.getString(R.string.wizard_secure_handling_desc),
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
            disabled = !state.canProceedToStep3
        )
    }
}
