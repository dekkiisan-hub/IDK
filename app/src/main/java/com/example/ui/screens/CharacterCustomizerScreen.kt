package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.api.GeminiApiClient
import com.example.api.Content as ApiContent
import com.example.api.Part as ApiPart
import com.example.ui.WaifuViewModel
import com.example.ui.theme.TsunderePink
import com.example.ui.theme.KuudereCyan
import com.example.ui.theme.CheerfulWarm
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCustomizerScreen(
    viewModel: WaifuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val name by viewModel.customName.collectAsState()
    val personality by viewModel.customPersonality.collectAsState()
    val relation by viewModel.customRelationType.collectAsState()
    val backstory by viewModel.customBackstory.collectAsState()
    val avatarChoice by viewModel.customAvatarChoice.collectAsState()
    val themeChoice by viewModel.customThemeColor.collectAsState()

    var isGeneratingPrompt by remember { mutableStateOf(false) }

    // Personality presets matching generated drawings
    val personalitiesList = listOf("Tsundere", "Kuudere", "Cheerful Friend")
    val relationshipsList = listOf("Companion", "Girlfriend", "Wife", "Mentor", "Rival", "Bodyguard")
    val themeColors = listOf("Purple", "Pink", "Cyan")

    // Retrieve corresponding preview images
    val avatarDrawableId = remember(avatarChoice) {
        val resourceId = context.resources.getIdentifier(
            avatarChoice,
            "drawable",
            context.packageName
        )
        if (resourceId != 0) resourceId else R.drawable.img_anime_kaguya
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("ENGRAVE NEW MATRIX SOUL", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.backToGallery() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Cancel", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "ENGRAVING TERMINAL LOG",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Dynamic Live Avatar Choice Grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CHOOSE CORE AVATAR PHENOTYPE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = avatarDrawableId),
                            contentDescription = "Avatar Review",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row of choice presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                viewModel.customAvatarChoice.value = "img_anime_kaguya"
                                viewModel.customPersonality.value = "Tsundere"
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .border(
                                        if (avatarChoice == "img_anime_kaguya") 2.5.dp else 1.dp,
                                        if (avatarChoice == "img_anime_kaguya") TsunderePink else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_anime_kaguya),
                                    contentDescription = "Kaguya Phenotype",
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tsundere", fontSize = 9.sp, color = if (avatarChoice == "img_anime_kaguya") TsunderePink else MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                viewModel.customAvatarChoice.value = "img_anime_yuki"
                                viewModel.customPersonality.value = "Kuudere"
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .border(
                                        if (avatarChoice == "img_anime_yuki") 2.5.dp else 1.dp,
                                        if (avatarChoice == "img_anime_yuki") KuudereCyan else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_anime_yuki),
                                    contentDescription = "Yuki Phenotype",
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Kuudere", fontSize = 9.sp, color = if (avatarChoice == "img_anime_yuki") KuudereCyan else MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                viewModel.customAvatarChoice.value = "img_anime_sakura"
                                viewModel.customPersonality.value = "Cheerful Friend"
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .border(
                                        if (avatarChoice == "img_anime_sakura") 2.5.dp else 1.dp,
                                        if (avatarChoice == "img_anime_sakura") CheerfulWarm else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_anime_sakura),
                                    contentDescription = "Sakura Phenotype",
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Bubbly", fontSize = 9.sp, color = if (avatarChoice == "img_anime_sakura") CheerfulWarm else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Input Form
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Companion Moniker / Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                TextField(
                    value = name,
                    onValueChange = { viewModel.customName.value = it },
                    modifier = Modifier.fillMaxWidth().testTag("custom_name_input"),
                    placeholder = { Text("E.g., Rin Tohsaka", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 13.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Presets row: Personality type selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Core Emotional Persona", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    personalitiesList.forEach { p ->
                        val isSelected = personality == p
                        val accent = when(p) {
                            "Tsundere" -> TsunderePink
                            "Kuudere" -> KuudereCyan
                            else -> CheerfulWarm
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) accent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .border(1.dp, if (isSelected) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .clickable { viewModel.customPersonality.value = p }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Presets Row for Relationship type choice
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Vow Link Relationship Style", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    relationshipsList.forEach { r ->
                        val isSelected = relation == r
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.customRelationType.value = r },
                            label = { Text(r, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Custom description of backstory
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Character Backstory & Trait Directives", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                    // Innovative helper: Generate a masterpiece of backstory prompt using Gemini Model itself!
                    TextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                isGeneratingPrompt = true
                                coroutineScope.launch {
                                    val aiPrompt = "Write a beautiful, rich 2-sentence background story for an anime girl character named $name who is a $personality acting as a $relation to the user. Describe her traits, her background, and notes about her unique quirks."
                                    val generatedStory = GeminiApiClient.chatWithWaifu(
                                        history = listOf(ApiContent(parts = listOf(ApiPart(text = aiPrompt)), role = "user")),
                                        systemPrompt = "You are a creative character creator writing high quality visual novel trait summaries."
                                    )
                                    viewModel.customBackstory.value = generatedStory.trim()
                                    isGeneratingPrompt = false
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                        enabled = name.isNotBlank() && !isGeneratingPrompt
                    ) {
                        Icon(Icons.Default.Star, "Gemini Helper", modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGeneratingPrompt) "Infusing Soul..." else "Matrix AI Writer",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TextField(
                    value = backstory,
                    onValueChange = { viewModel.customBackstory.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("custom_backstory_input"),
                    placeholder = { Text("E.g., Rin is a powerful mage and heiress to her family. She is highly intelligent, competitive, but secretly extremely warm and caring...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 12.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 8,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
            }

            // Cyber Glow colors
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Vapor Cybernetic Accent Theme Color", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    themeColors.forEach { c ->
                        val isSelected = themeChoice == c
                        val actualColor = when(c) {
                            "Purple" -> MaterialTheme.colorScheme.primary
                            "Pink" -> TsunderePink
                            else -> KuudereCyan
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(actualColor.copy(alpha = if (isSelected) 0.25f else 0.05f), RoundedCornerShape(10.dp))
                                .border(if (isSelected) 2.dp else 1.dp, if (isSelected) actualColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .clickable { viewModel.customThemeColor.value = c },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(actualColor, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(c, fontSize = 11.sp, color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Complete Matrix Button
            Button(
                onClick = { viewModel.createCharacter() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_custom_waifu_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp),
                enabled = name.isNotBlank() && backcountryNotBlank(backstory)
            ) {
                Icon(Icons.Default.Check, "Engrave matrix")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Engrave Matrix Complete", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun backcountryNotBlank(backstory: String): Boolean {
    return backstory.isNotBlank()
}
