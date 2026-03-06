package com.klodit.almizan.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.*

sealed class BottomNavDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home   : BottomNavDestination("home",    Icons.Filled.Home,   Icons.Outlined.Home)
    object Tenders : BottomNavDestination("tenders", Icons.Filled.Description, Icons.Outlined.Description)
    object MyBids : BottomNavDestination("my_bids", Icons.Outlined.ShoppingBag, Icons.Outlined.ShoppingBag)
    object Profile: BottomNavDestination("profile", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(
    BottomNavDestination.Home,
    BottomNavDestination.Tenders,
    BottomNavDestination.MyBids,
    BottomNavDestination.Profile
)

@Composable
fun AlMizanBottomBar(
    currentRoute: String,
    localizedContext: Context,
    onDestinationSelected: (BottomNavDestination) -> Unit
) {
    // Read labels from strings.xml using the localized context
    val labels = mapOf(
        BottomNavDestination.Home.route    to localizedContext.getString(R.string.tab_home),
        BottomNavDestination.Tenders.route to localizedContext.getString(R.string.tab_search),
        BottomNavDestination.MyBids.route  to localizedContext.getString(R.string.tab_my_bids),
        BottomNavDestination.Profile.route to localizedContext.getString(R.string.tab_profile)
    )

    Surface(
        modifier       = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        shape          = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color          = NavyWhite,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            bottomNavItems.forEach { destination ->
                BottomNavItem(
                    destination = destination,
                    label       = labels[destination.route] ?: "",
                    isSelected  = currentRoute == destination.route,
                    onClick     = { onDestinationSelected(destination) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    destination: BottomNavDestination,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier            = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Green50 else Color.Transparent) // Solid visible light green
        ) {
            Icon(
                imageVector        = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = label,
                tint               = if (isSelected) Green500 else Navy400,
                modifier           = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text          = label,
            color         = if (isSelected) Green500 else Navy400,
            fontSize      = 11.sp,
            fontWeight    = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}