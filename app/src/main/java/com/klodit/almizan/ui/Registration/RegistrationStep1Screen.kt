package com.klodit.almizan.ui.Registration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.klodit.almizan.R
import com.klodit.almizan.ui.auth.AuthFieldLabel
import com.klodit.almizan.ui.auth.LanguageSwitcher
import com.klodit.almizan.ui.auth.authFieldColors
import com.klodit.almizan.ui.theme.AppLanguage
import com.klodit.almizan.ui.theme.Grey200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationStep1Screen(
    onContinueClick: (orgName: String, nif: String, nis: String, rc: String, type: String, role: String, wilaya: String, commune: String, adresse: String) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onBackClick     : () -> Unit = {},
    onInfoClick     : () -> Unit = {},
    onTermsClick    : () -> Unit = {},
    onPrivacyClick  : () -> Unit = {},
    selectedLang    : AppLanguage = AppLanguage.FRENCH,
    onLanguageChange: (AppLanguage) -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme

    var orgName       by remember { mutableStateOf("") }
    var nif           by remember { mutableStateOf("") }
    var nis           by remember { mutableStateOf("") }
    var rc            by remember { mutableStateOf("") }
    var wilaya  by remember { mutableStateOf("") }
    var commune by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    val canContinue = orgName.isNotBlank() && nif.length == 15 && nis.isNotBlank()
            && rc.isNotBlank() && wilaya.isNotBlank() && agreedToTerms

    val screenWidth   = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth     = if (screenWidth < 500.dp) screenWidth * 0.90f else 420.dp
    val overlapAmount = 32.dp

    // Add state
    var selectedType by remember { mutableStateOf("MINISTERE") }
    var selectedRole by remember { mutableStateOf("SERVICE_CONTRACTANT") }

    var typeExpanded by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }

    val typeOptions = listOf("EPA","EPIC","MINISTERE","ENTREPRISE_PRIVEE","ENTREPRISE_PUBLIQUE","GROUPEMENT")
    val roleOptions = listOf("SERVICE_CONTRACTANT","OPERATEUR_ECONOMIQUE")

    Column(modifier = Modifier.fillMaxSize().background(cs.background)) {

        // scrollable body
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // header
            Box(
                modifier = Modifier.fillMaxWidth().background(cs.primary)
                    .statusBarsPadding()
                    .padding(bottom = overlapAmount + 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = cs.onPrimary, modifier = Modifier.size(24.dp))
                    }
                    Text("Registration", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        color = cs.onPrimary)
                    IconButton(onClick = onInfoClick) {
                        Icon(Icons.Outlined.Info, "Info",
                            tint = cs.onPrimary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // step progress card
            Card(
                modifier  = Modifier.width(cardWidth).offset(y = -overlapAmount).zIndex(1f),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = cs.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.reg1_step_label),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = cs.secondary, letterSpacing = 1.sp)
                        Text("33%", fontSize = 11.sp, color = cs.onSurfaceVariant,
                            fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(5.dp)
                        .clip(RoundedCornerShape(3.dp)).background(Grey200)) {
                        Box(modifier = Modifier.fillMaxWidth(0.33f).fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp)).background(cs.secondary))
                    }
                }
            }

            Spacer(Modifier.height((-overlapAmount.value + 8).dp))

            // content
            Column(modifier = Modifier.width(cardWidth)) {

                Text(stringResource(R.string.reg1_section_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.reg1_section_sub),
                    fontSize = 13.sp, color = cs.secondary)

                Spacer(Modifier.height(24.dp))

                // org name
                AuthFieldLabel(stringResource(R.string.reg1_field_org_name))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = orgName,
                    onValueChange = { orgName = it },
                    placeholder   = { Text(stringResource(R.string.reg1_ph_org),
                        color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(8.dp),
                    colors     = authFieldColors()
                )

                Spacer(Modifier.height(16.dp))

                // NIF
                AuthFieldLabel(stringResource(R.string.reg1_field_nif))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = nif,
                    onValueChange = { if (it.length <= 15 && it.all { c -> c.isDigit() }) nif = it },
                    placeholder   = { Text(stringResource(R.string.reg1_ph_nif),
                        color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText  = {
                        Text("${nif.length}/15", fontSize = 10.sp,
                            color = if (nif.length == 15) cs.secondary else cs.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    },
                    colors = authFieldColors()
                )

                Spacer(Modifier.height(16.dp))

                // NIS
                AuthFieldLabel(stringResource(R.string.reg1_field_nis))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = nis,
                    onValueChange = { if (it.length <= 15 && it.all { c -> c.isDigit() }) nis = it },
                    placeholder   = { Text(stringResource(R.string.reg1_ph_nis),
                        color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText  = {
                        Text("${nis.length}/15", fontSize = 10.sp,
                            color = if (nis.length == 15) cs.secondary else cs.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    },
                    colors = authFieldColors()
                )

                Spacer(Modifier.height(16.dp))

                // RC
                AuthFieldLabel(stringResource(R.string.reg1_field_rc))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = rc,
                    onValueChange = { rc = it },
                    placeholder   = { Text(stringResource(R.string.reg1_ph_rc),
                        color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    leadingIcon   = { Icon(Icons.Outlined.Edit, null,
                        tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(8.dp),
                    colors     = authFieldColors()
                )



                Spacer(Modifier.height(16.dp))

                // Wilaya
                AuthFieldLabel("Wilaya")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = wilaya,
                    onValueChange = { wilaya = it },
                    placeholder   = { Text("Ex: Alger", color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(8.dp),
                    colors     = authFieldColors()
                )

                Spacer(Modifier.height(16.dp))

            // Commune
                AuthFieldLabel("Commune")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = commune,
                    onValueChange = { commune = it },
                    placeholder   = { Text("Ex: Sidi M'Hamed", color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(8.dp),
                    colors     = authFieldColors()
                )

                Spacer(Modifier.height(16.dp))

// Adresse
                AuthFieldLabel("Adresse")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = adresse,
                    onValueChange = { adresse = it },
                    placeholder   = { Text("Ex: Rue Didouche Mourad", color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(8.dp),
                    colors     = authFieldColors()
                )

                Spacer(Modifier.height(24.dp))

                // Type dropdown
                AuthFieldLabel("Type d'organisation")
                Spacer(Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = authFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = 13.sp) },
                                onClick = { selectedType = option; typeExpanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

// Role dropdown
                AuthFieldLabel("Rôle")
                Spacer(Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = authFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        roleOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = 13.sp) },
                                onClick = { selectedRole = option; roleExpanded = false }
                            )
                        }
                    }
                }


                // secure notice
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(cs.secondaryContainer).padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.Lock, null, tint = cs.secondary,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(stringResource(R.string.reg1_secure_title),
                            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                        Spacer(Modifier.height(3.dp))
                        Text(stringResource(R.string.reg1_secure_body),
                            fontSize = 12.sp, color = cs.onSurfaceVariant, lineHeight = 17.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                LanguageSwitcher(selectedLang, onLanguageChange)

                Spacer(Modifier.height(24.dp))

                // terms checkbox
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked         = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it },
                        modifier        = Modifier.size(20.dp),
                        colors          = CheckboxDefaults.colors(
                            checkedColor   = cs.secondary,
                            uncheckedColor = cs.outline)
                    )
                    Spacer(Modifier.width(8.dp))
                    val annotated = buildAnnotatedString {
                        append(stringResource(R.string.reg1_terms_text))
                        pushStringAnnotation("TERMS", "terms")
                        withStyle(SpanStyle(color = cs.secondary, fontWeight = FontWeight.SemiBold)) {
                            append(stringResource(R.string.reg1_terms_link))
                        }
                        pop()
                        append(stringResource(R.string.reg1_and))
                        pushStringAnnotation("PRIVACY", "privacy")
                        withStyle(SpanStyle(color = cs.secondary, fontWeight = FontWeight.SemiBold)) {
                            append(stringResource(R.string.reg1_privacy_link))
                        }
                        pop()
                        append(".")
                    }
                    ClickableText(
                        text  = annotated,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp, color = cs.onSurfaceVariant),
                        onClick = { offset ->
                            annotated.getStringAnnotations("TERMS", offset, offset)
                                .firstOrNull()?.let { onTermsClick() }
                            annotated.getStringAnnotations("PRIVACY", offset, offset)
                                .firstOrNull()?.let { onPrivacyClick() }
                        }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // fixed bottom bar
        Column(
            modifier = Modifier.fillMaxWidth().background(cs.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Outlined.Lock, null, tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(5.dp))
                Text(stringResource(R.string.bank_encryption),
                    fontSize = 9.sp, color = cs.onSurfaceVariant,
                    letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { onContinueClick(orgName, nif, nis, rc, selectedType, selectedRole, wilaya, commune, adresse) },
                enabled  = canContinue,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = cs.secondary,
                    disabledContainerColor = cs.secondaryContainer)
            ) {
                Text(stringResource(R.string.reg1_continue_btn),
                    fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = cs.onSecondary)
            }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.reg1_back_label),
                fontSize = 13.sp, color = cs.onSurfaceVariant,
                modifier = Modifier.clickable { onBackClick() })
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.footer_ministry),
                fontSize = 9.sp, color = cs.onSurfaceVariant,
                letterSpacing = 0.5.sp, textAlign = TextAlign.Center)
        }
    }
}