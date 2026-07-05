package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.MaintenanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceMainScreen(viewModel: MaintenanceViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentEntityId by viewModel.currentEntityId.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val entities by viewModel.allEntities.collectAsState()
    val activeEntity by viewModel.activeEntity.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val unreadNotifications = notifications.filter { !it.isRead }.size

    var showRoleMenu by remember { mutableStateOf(false) }
    var showEntityMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "EMMS Portal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        activeEntity?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    // Entity Switcher
                    Box(modifier = Modifier.padding(end = 4.dp)) {
                        IconButton(
                            onClick = { showEntityMenu = true },
                            modifier = Modifier.testTag("entity_selector")
                        ) {
                            Icon(Icons.Default.Business, contentDescription = "Switch Entity")
                        }
                        DropdownMenu(
                            expanded = showEntityMenu,
                            onDismissRequest = { showEntityMenu = false }
                        ) {
                            entities.forEach { entity ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val icon = when (entity.type) {
                                                "House" -> Icons.Default.Home
                                                "Company" -> Icons.Default.CorporateFare
                                                else -> Icons.Default.LocationCity
                                            }
                                            Icon(
                                                icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(entity.name)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setEntityId(entity.id)
                                        showEntityMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Role Switcher
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        Button(
                            onClick = { showRoleMenu = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("role_selector")
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentRole, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            val roles = listOf("Super Admin", "Entity Admin", "Technician", "End User")
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        viewModel.setRole(role)
                                        showRoleMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav")
            ) {
                val tabs = listOf(
                    Triple("Home", Icons.Default.Home, Icons.Outlined.Home),
                    Triple("New Request", Icons.Default.AddCircle, Icons.Outlined.AddCircleOutline),
                    Triple("My Requests", Icons.Default.Build, Icons.Outlined.Build),
                    Triple(
                        "Notifications",
                        Icons.Default.Notifications,
                        Icons.Outlined.Notifications
                    ),
                    Triple("Profile", Icons.Default.Person, Icons.Outlined.Person)
                )

                tabs.forEach { (tabName, filledIcon, outlinedIcon) ->
                    val isSelected = selectedTab == tabName
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(tabName) },
                        icon = {
                            Box {
                                Icon(
                                    imageVector = if (isSelected) filledIcon else outlinedIcon,
                                    contentDescription = tabName
                                )
                                if (tabName == "Notifications" && unreadNotifications > 0) {
                                    Badge(
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(unreadNotifications.toString())
                                    }
                                }
                            }
                        },
                        label = { Text(tabName, fontSize = 10.sp) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                "Home" -> HomeScreen(viewModel)
                "New Request" -> NewRequestScreen(viewModel)
                "My Requests" -> MyRequestsScreen(viewModel)
                "Notifications" -> NotificationsScreen(viewModel)
                "Profile" -> ProfileScreen(viewModel)
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: MaintenanceViewModel) {
    val activeServices by viewModel.activeServices.collectAsState()
    val activeRequests by viewModel.activeRequests.collectAsState()
    val activeSchedules by viewModel.activeScheduledTasks.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    val pendingCount = activeRequests.filter { it.status == "Assigned" || it.status == "In Progress" }.size
    val resolvedCount = activeRequests.filter { it.status == "Resolved" || it.status == "Verified" }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Welcome Banner
        item {
            WelcomeBannerCard()
        }

        // Active Quick Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Pending Issues",
                    value = pendingCount.toString(),
                    icon = Icons.Default.ErrorOutline,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Resolved Issues",
                    value = resolvedCount.toString(),
                    icon = Icons.Default.CheckCircleOutline,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Service Health Matrix
        item {
            Text(
                text = "Interconnected Utilities Status",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Tap a service to toggle status & simulate cascading dependency failures",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(activeServices) { service ->
                    ServiceHealthCard(
                        service = service,
                        onToggle = { newStatus ->
                            viewModel.changeServiceStatus(service.category, newStatus)
                        }
                    )
                }
            }
        }

        // Interactive Interconnection Graph
        item {
            InterconnectedGraphCard(activeServices)
        }

        // Preventive Schedules
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Preventive Inspections",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (currentRole == "Super Admin" || currentRole == "Entity Admin") {
                    Text(
                        text = "Admin Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeSchedules.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No preventive maintenance schedules defined.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeSchedules.forEach { schedule ->
                        PreventiveScheduleItem(
                            schedule = schedule,
                            canComplete = currentRole == "Super Admin" || currentRole == "Entity Admin",
                            onComplete = {
                                viewModel.performScheduledTask(schedule.id)
                            }
                        )
                    }
                }
            }
        }

        // Clear simulation helper button
        item {
            Button(
                onClick = { viewModel.resetActiveEntityServices() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore All Services to Healthy")
            }
        }
    }
}

@Composable
fun WelcomeBannerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F766E)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                    drawRect(brush)
                }
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                Text(
                    text = "Essential Maintenance Management",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Real-time cascading impacts tracking, scheduling, and SLA resolutions.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ServiceHealthCard(
    service: ServiceModel,
    onToggle: (String) -> Unit
) {
    val statusColor = when (service.status) {
        "Healthy" -> Color(0xFF10B981)
        "Maintenance" -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    var showStatusDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(135.dp)
            .height(130.dp)
            .clickable { showStatusDialog = true }
            .testTag("service_health_card_${service.category}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getServiceIcon(service.category),
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor, CircleShape)
                )
            }

            Column {
                Text(
                    text = service.category,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = service.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Simulate Status: ${service.category}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Manually set status to trigger notifications & cascading offline impacts on dependent elements.")
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onToggle("Healthy")
                                showStatusDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(12.dp).background(Color(0xFF10B981), CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Healthy (Green)", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onToggle("Maintenance")
                                showStatusDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(12.dp).background(Color(0xFFF59E0B), CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Maintenance (Yellow)", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onToggle("Down")
                                showStatusDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(12.dp).background(Color(0xFFEF4444), CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Critical Down (Red)", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InterconnectedGraphCard(services: List<ServiceModel>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dependency_mapping_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interconnected Dependency Tree",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "How power supply links downstream water pumps, HVAC, and elevators.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Graph visualization
            val electricityService = services.find { it.category == "Electricity" }
            val otherServices = services.filter { it.category != "Electricity" }

            val parentColor = getStatusColor(electricityService?.status ?: "Healthy")

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Parent Node
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(parentColor.copy(alpha = 0.15f))
                        .border(1.5.dp, parentColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ElectricalServices,
                            contentDescription = null,
                            tint = parentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Electricity (Main Power)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Downward Flow Connectors
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Drawing vertical down arrow
                    drawLine(
                        color = parentColor.copy(alpha = 0.6f),
                        start = Offset(w / 2, 0f),
                        end = Offset(w / 2, h),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                }

                // Downstream Dependent Nodes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    val displayList = otherServices.take(3) // take first 3 downstream services to keep it gorgeous and readable on mobile
                    displayList.forEach { child ->
                        val childColor = getStatusColor(child.status)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(childColor.copy(alpha = 0.1f))
                                .border(1.dp, childColor, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = getServiceIcon(child.category),
                                    contentDescription = null,
                                    tint = childColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = child.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = child.status,
                                    fontSize = 9.sp,
                                    color = childColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (electricityService?.status == "Down") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Power failure is cascading! Downstream systems forced OFFLINE.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreventiveScheduleItem(
    schedule: ScheduledTask,
    canComplete: Boolean,
    onComplete: () -> Unit
) {
    val dateString = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(schedule.nextDueDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getServiceIcon(schedule.serviceCategory),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = schedule.description,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Due: $dateString • ${schedule.frequency}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (canComplete) {
                Button(
                    onClick = onComplete,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = schedule.frequency,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NewRequestScreen(viewModel: MaintenanceViewModel) {
    val activeServices by viewModel.activeServices.collectAsState()

    val categories = activeServices.map { it.category }
    val priorities = listOf("Low", "Medium", "High", "Emergency")

    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "Electricity") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var categoryExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Raise Maintenance Complaint",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Report plumbing, generator, HVAC, lift, gas, or electrical grid concerns instantly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category selection
                    Column {
                        Text("Service Category", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { categoryExpanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_category_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(getServiceIcon(selectedCategory), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(selectedCategory, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        onClick = {
                                            selectedCategory = category
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Priority selection
                    Column {
                        Text("Priority Level", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            priorities.forEach { priority ->
                                val isSelected = selectedPriority == priority
                                val badgeColor = when (priority) {
                                    "Low" -> Color(0xFF10B981)
                                    "Medium" -> Color(0xFF3B82F6)
                                    "High" -> Color(0xFFF59E0B)
                                    else -> Color(0xFFEF4444)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) badgeColor else badgeColor.copy(alpha = 0.12f))
                                        .border(1.dp, if (isSelected) Color.Transparent else badgeColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .clickable { selectedPriority = priority }
                                        .padding(vertical = 10.dp)
                                        .testTag("priority_chip_$priority"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = priority,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else badgeColor
                                    )
                                }
                            }
                        }
                    }

                    // Location Input
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Exact Location (e.g. Floor 2, Block A)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_location"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Description Input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Describe the issue in detail...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("input_description"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 5
                    )

                    // Simulated Photo upload placeholder
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Attach Photo (Optional)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Automatic GPS and unit details will be attached.", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            if (description.isBlank() || location.isBlank()) {
                                // show error toast or action
                            } else {
                                viewModel.raiseRequest(selectedCategory, description, selectedPriority, location)
                                description = ""
                                location = ""
                                viewModel.setSelectedTab("My Requests")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("raise_request_submit"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("File Maintenance Complaint", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MyRequestsScreen(viewModel: MaintenanceViewModel) {
    val activeRequests by viewModel.activeRequests.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Pending", "Resolved"

    val filteredRequests = activeRequests.filter { req ->
        val matchesSearch = req.serviceCategory.contains(searchQuery, ignoreCase = true) || req.description.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (selectedFilter) {
            "Pending" -> req.status == "Assigned" || req.status == "In Progress"
            "Resolved" -> req.status == "Resolved" || req.status == "Verified"
            else -> true
        }
        matchesSearch && matchesStatus
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Maintenance Requests Log",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Track real-time SLA progress, assignments, and provide feedback ratings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Search & Filters Row
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by category, description...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_requests"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Pending", "Resolved").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedFilter = filter }
                            .padding(vertical = 8.dp)
                            .testTag("filter_tab_$filter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (filteredRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ContentPasteOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No matching maintenance complaints found.", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(filteredRequests) { request ->
                RequestCardItem(request, currentRole, viewModel)
            }
        }
    }
}

@Composable
fun RequestCardItem(
    request: MaintenanceRequest,
    role: String,
    viewModel: MaintenanceViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (request.status) {
        "Assigned" -> Color(0xFF3B82F6)
        "In Progress" -> Color(0xFFF59E0B)
        "Resolved" -> Color(0xFF10B981)
        else -> Color(0xFF8B5CF6) // Verified
    }

    val priorityColor = when (request.priority) {
        "Low" -> Color(0xFF10B981)
        "Medium" -> Color(0xFF3B82F6)
        "High" -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    // SLA hours limit logic
    val slaLimitHours = when (request.priority) {
        "Emergency" -> 2
        "High" -> 6
        "Medium" -> 24
        else -> 48
    }
    val elapsedMs = System.currentTimeMillis() - request.createdAt
    val remainingHours = slaLimitHours - (elapsedMs / 3600000)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("request_item_${request.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(statusColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getServiceIcon(request.serviceCategory),
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = request.serviceCategory,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = request.location,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = request.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(request.createdAt)),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${request.priority} Priority",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }

                    // SLA countdown badge
                    if (request.status != "Resolved" && request.status != "Verified") {
                        val slaColor = if (remainingHours < 0) Color(0xFFEF4444) else Color(0xFF10B981)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(slaColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (remainingHours < 0) "SLA Overdue" else "SLA: ${remainingHours}h left",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = slaColor
                            )
                        }
                    }
                }

                if (request.assignedTo.isNotEmpty()) {
                    Text(
                        text = "Assigned to: ${request.assignedTo}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Unassigned",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Expanded Admin/Technician controls and feedback
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Management Actions & History", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Timeline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TimelineStep("File", true)
                        TimelineStep("Assign", request.assignedTo.isNotEmpty())
                        TimelineStep("Work", request.status == "In Progress" || request.status == "Resolved" || request.status == "Verified")
                        TimelineStep("Done", request.status == "Resolved" || request.status == "Verified")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role-based actions
                    when (role) {
                        "Super Admin", "Entity Admin" -> {
                            AdminControls(request, viewModel)
                        }
                        "Technician" -> {
                            TechnicianControls(request, viewModel)
                        }
                        "End User" -> {
                            EndUserFeedbackControls(request, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineStep(label: String, completed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 9.sp, color = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun AdminControls(request: MaintenanceRequest, viewModel: MaintenanceViewModel) {
    var techMenuExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Admin: Assign or update task parameters", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { techMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reassign Tech", fontSize = 11.sp)
                }
                DropdownMenu(
                    expanded = techMenuExpanded,
                    onDismissRequest = { techMenuExpanded = false }
                ) {
                    viewModel.techniciansList.forEach { tech ->
                        DropdownMenuItem(
                            text = { Text(tech) },
                            onClick = {
                                viewModel.assignRequest(request.id, tech)
                                techMenuExpanded = false
                            }
                        )
                    }
                }
            }

            if (request.status != "Verified" && request.status != "Resolved") {
                Button(
                    onClick = { viewModel.updateRequestStatus(request.id, "Resolved") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Mark Resolved", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun TechnicianControls(request: MaintenanceRequest, viewModel: MaintenanceViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Technician Work Dispatch Desk", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (request.status == "Assigned") {
                Button(
                    onClick = { viewModel.updateRequestStatus(request.id, "In Progress") },
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Start Work", fontSize = 11.sp)
                }
            }
            if (request.status == "In Progress") {
                Button(
                    onClick = { viewModel.updateRequestStatus(request.id, "Resolved") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Finish & Resolve", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun EndUserFeedbackControls(request: MaintenanceRequest, viewModel: MaintenanceViewModel) {
    var rating by remember { mutableStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (request.status == "Resolved") {
            Text("Verify resolution & rate service satisfaction", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..5) {
                    IconButton(onClick = { rating = i }) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star $i",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Comment feedback...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Button(
                onClick = { viewModel.submitFeedback(request.id, rating, feedbackText) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Verify & Submit Rating")
            }
        } else if (request.status == "Verified") {
            Text("User Rating Feedback Left", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= request.feedbackRating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (request.feedbackComment.isNotEmpty()) {
                Text(
                    text = "\"${request.feedbackComment}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            Text("Status: Waiting for Technician assignment & resolution progress.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun NotificationsScreen(viewModel: MaintenanceViewModel) {
    val notifications by viewModel.notifications.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "System Notifications Hub",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "In-app automated logs & critical cascading warning status.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(
                    onClick = { viewModel.clearNotifications() },
                    modifier = Modifier.testTag("clear_notifications_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No notifications reported.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            items(notifications) { notification ->
                NotificationItemRow(notification, viewModel)
            }
        }
    }
}

@Composable
fun NotificationItemRow(
    notification: NotificationModel,
    viewModel: MaintenanceViewModel
) {
    val icon = when (notification.type) {
        "CascadingAlert" -> Icons.Default.Warning
        "StatusUpdate" -> Icons.Default.CheckCircle
        "NewRequest" -> Icons.Default.AddAlert
        else -> Icons.Default.Alarm
    }

    val iconColor = when (notification.type) {
        "CascadingAlert" -> Color(0xFFEF4444)
        "StatusUpdate" -> Color(0xFF10B981)
        "NewRequest" -> Color(0xFF3B82F6)
        else -> Color(0xFFF59E0B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.markNotificationAsRead(notification.id) }
            .testTag("notification_item_${notification.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
        ),
        border = BorderStroke(
            1.dp,
            if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(notification.timestamp)),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: MaintenanceViewModel) {
    val activeRequests by viewModel.activeRequests.collectAsState()
    val context = LocalContext.current

    // Calculated Statistics for Reports
    val totalCount = activeRequests.size
    val resolved = activeRequests.filter { it.status == "Resolved" || it.status == "Verified" }.size
    val pending = totalCount - resolved

    // Category distribution counts
    val categoryCounts = activeRequests.groupBy { it.serviceCategory }.mapValues { it.value.size }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Analytics & Reports Center",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Interactive breakdown of cost metrics, resolution times, and maintenance audits.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Stats Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Complaints",
                    value = totalCount.toString(),
                    icon = Icons.Default.Summarize,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Average Resolution",
                    value = "2.4 hrs",
                    icon = Icons.Default.Speed,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Visual Mini Charts
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_charts_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Utility Outage Distribution",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (categoryCounts.isEmpty()) {
                        Text("No active issue logs to chart.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        categoryCounts.forEach { (cat, count) ->
                            val percentage = if (totalCount > 0) count.toFloat() / totalCount else 0f
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("$count cases (${(percentage * 100).toInt()}%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { percentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cost Tracking Report
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Operational Cost Allocation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CostAllocationRow("Spare Parts & Hardware", "₹12,450", "45%")
                    CostAllocationRow("Technician Labor SLAs", "₹10,200", "37%")
                    CostAllocationRow("Preventive Fuel / Gensets", "₹4,800", "18%")
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Current Allocation", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("₹27,450", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Downloadable Report Utility
        item {
            Button(
                onClick = {
                    exportReport(context, activeRequests)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("download_report_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Maintenance CSV Report")
            }
        }
    }
}

@Composable
fun CostAllocationRow(label: String, valStr: String, pctStr: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp)
        Row {
            Text(valStr, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("($pctStr)", color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
        }
    }
}

// Global UI helpers
fun getServiceIcon(category: String): ImageVector {
    return when (category) {
        "Electricity" -> Icons.Default.ElectricalServices
        "Water" -> Icons.Default.WaterDrop
        "Plumbing" -> Icons.Default.Plumbing
        "Gas" -> Icons.Default.LocalGasStation
        "Internet/Network" -> Icons.Default.CellTower
        "Elevator" -> Icons.Default.Elevator
        "Generator" -> Icons.Default.OfflineBolt
        "HVAC" -> Icons.Default.AcUnit
        "Waste" -> Icons.Default.DeleteOutline
        "Security" -> Icons.Default.Security
        "Road/Infra" -> Icons.Default.LocationCity
        else -> Icons.Default.Build
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Healthy" -> Color(0xFF10B981)
        "Maintenance" -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
}

private fun exportReport(context: Context, list: List<MaintenanceRequest>) {
    val csv = StringBuilder("ID,Category,Location,Priority,Status,AssignedTo,CreatedTime,ResolvedTime,Rating,Comment\n")
    list.forEach { req ->
        csv.append("${req.id},\"${req.serviceCategory}\",\"${req.location}\",${req.priority},${req.status},\"${req.assignedTo}\",${req.createdAt},${req.resolvedAt},${req.feedbackRating},\"${req.feedbackComment}\"\n")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "EMMS Maintenance Audit Report")
        putExtra(Intent.EXTRA_TEXT, csv.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Export Audit Log"))
}
