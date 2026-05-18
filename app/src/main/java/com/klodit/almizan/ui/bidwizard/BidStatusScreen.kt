package com.klodit.almizan.ui.bidwizard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.SoumissionDetailViewModel

@Composable
fun BidStatusScreen(
    submissionId: String,
    localizedContext: Context,
    onBackClick: () -> Unit,
    onContactSupport: () -> Unit = {},
    viewModel: SoumissionDetailViewModel = viewModel()
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(submissionId) {
        viewModel.loadDetail(submissionId)
    }

    if (isLoading || detail == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Green500)
        }
        return
    }

    val d = detail!!
    val isEvaluated = d.statut?.uppercase() == "EVALUEE" || d.statut?.uppercase() == "RETENUE" || d.statut?.uppercase() == "REJETEE"
    val isAwarded = d.statut?.uppercase() == "RETENUE"

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
        Surface(modifier = Modifier.fillMaxWidth(), color = NavyWhite, shadowElevation = 2.dp) {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Navy700) }
                Text(localizedContext.getString(R.string.status_title), modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Navy800, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(48.dp))
            }
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            
            // Status Header
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NavyWhite)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE3F2FD)) {
                        Text(d.statut?.uppercase() ?: "INCONNU", color = Color(0xFF1976D2), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Appel d'offres : ${d.reference}", color = Navy900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Crypto Receipt
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Navy800)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Lock, null, tint = NavyWhite, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(localizedContext.getString(R.string.status_crypto_receipt), color = NavyWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(localizedContext.getString(R.string.status_official_timestamp), color = Navy300, fontSize = 13.sp)
                        Text(d.horodatageServeur?.take(10) ?: "-", color = NavyWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(localizedContext.getString(R.string.status_tech_hash), color = Navy300, fontSize = 13.sp)
                        Surface(shape = RoundedCornerShape(4.dp), color = Navy700) {
                            Text(d.offreTechnique?.hashFichier?.take(16) ?: "-", color = NavyWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Timeline
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NavyWhite)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(localizedContext.getString(R.string.status_live_progress), color = Navy900, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))
                    TimelineStep(localizedContext.getString(R.string.status_bid_submitted), d.createdAt?.take(10), true)
                    TimelineStep(localizedContext.getString(R.string.status_plis_opened), null, d.offreFinanciere?.isDechiffree == true)
                    TimelineStep(localizedContext.getString(R.string.bid_status_evaluation), null, isEvaluated)
                    TimelineStep(localizedContext.getString(R.string.status_attribution), null, isAwarded, isLast = true)
                }
            }
        }
    }
}

@Composable
private fun TimelineStep(title: String, date: String?, isCompleted: Boolean, isLast: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(if (isCompleted) Color(0xFF4CAF50) else Navy100), contentAlignment = Alignment.Center) {
                if (isCompleted) Icon(Icons.Filled.Check, null, tint = NavyWhite, modifier = Modifier.size(10.dp))
            }
            if (!isLast) Box(modifier = Modifier.width(2.dp).weight(1f).background(if (isCompleted) Color(0xFF4CAF50) else Navy100))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 24.dp)) {
            Text(title, color = if (isCompleted) Navy800 else Navy400, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            if (date != null) Text(date, color = Navy500, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}