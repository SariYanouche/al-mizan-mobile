package com.klodit.almizan.ui.bidwizard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.SoumissionDetailViewModel

@Composable
fun EvaluationResultsScreen(
    submissionId: String,
    localizedContext: Context,
    onBackClick: () -> Unit,
    onFileAppeal: () -> Unit,
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
    val isRetained = d.statut?.uppercase() == "RETENUE"

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Navy800) {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NavyWhite) }
                Text(localizedContext.getString(R.string.results_title), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = NavyWhite, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(48.dp))
            }
        }

        Column(modifier = Modifier.fillMaxSize().weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
            Card(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NavyWhite)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = if (isRetained) Green50 else Red50) {
                        Text(d.statut?.uppercase() ?: "STATUT INCONNU", color = if (isRetained) Green600 else Red600, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("AO Réf: ${d.reference}", color = Navy900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(localizedContext.getString(R.string.results_your_scores), color = Navy900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // Score fallback since DTO lacks specific score breakdown currently
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ScoreCard(localizedContext.getString(R.string.results_total), if (d.offreFinanciere?.isDechiffree == true) "Calculé" else "En attente", Modifier.weight(1f))
                ScoreCard("Montant TTC", d.offreFinanciere?.montantTtc?.toString() ?: "N/A", Modifier.weight(1f))
            }
        }

        if (!isRetained) {
            Column(modifier = Modifier.fillMaxWidth().background(NavyWhite).padding(20.dp).navigationBarsPadding()) {
                Button(
                    onClick = onFileAppeal,
                    colors = ButtonDefaults.buttonColors(containerColor = Red600),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(localizedContext.getString(R.string.results_file_appeal), color = NavyWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Outlined.Shield, null, tint = NavyWhite, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.shadow(1.dp, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Grey100)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Navy500, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, color = Navy900, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}