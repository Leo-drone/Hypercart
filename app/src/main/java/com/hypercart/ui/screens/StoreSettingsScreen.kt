package com.hypercart.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hypercart.DialogAlert
import com.hypercart.GradientScreen
import com.hypercart.SuccessDialog
import com.hypercart.data.CategoryWithOrder
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.darkGray
import com.hypercart.ui.theme.night
import com.hypercart.ui.viewmodel.ItemViewModel
import com.hypercart.ui.viewmodel.StoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSettingsScreen(
    navController: NavController,
    storeId: String,
    itemViewModel: ItemViewModel = viewModel(),
    storeViewModel: StoreViewModel = viewModel()
) {
    var categories by remember { mutableStateOf<List<CategoryWithOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    val selectedStore by storeViewModel.selectedStore.collectAsState()
    
    // États pour la modification du nom du magasin
    var storeName by remember { mutableStateOf("") }
    var isEditingStoreName by remember { mutableStateOf(false) }
    var isSavingStoreName by remember { mutableStateOf(false) }
    
    // États pour les erreurs et messages de succès
    val error by storeViewModel.error.collectAsState()
    val isStoreLoading by storeViewModel.isLoading.collectAsState()
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val listState = rememberLazyListState()
    val dragDropState = rememberDragDropState(listState) { fromIndex, toIndex ->
        categories = categories.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
    }
    
    LaunchedEffect(storeId) {
        val storeIdLong = storeId.toLongOrNull() ?: 0L
        storeViewModel.loadStoreById(storeIdLong)
        itemViewModel.loadCategoriesWithOrder(storeIdLong)
    }
    
    LaunchedEffect(itemViewModel.categoriesWithOrder.collectAsState().value) {
        categories = itemViewModel.categoriesWithOrder.value
        isLoading = false
    }
    
    // Initialiser le nom du magasin quand les données sont chargées
    LaunchedEffect(selectedStore) {
        selectedStore?.let { store ->
            storeName = store.name
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Paramètres - ${selectedStore?.name ?: "Magasin"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkGray
                )
            )
        },
        containerColor = night
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GradientScreen()
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = blueSkye)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Section de modification du nom du magasin
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.1f),
                                            Color.White.copy(alpha = 0.05f)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Nom du magasin",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    if (!isEditingStoreName) {
                                        IconButton(
                                            onClick = { isEditingStoreName = true }
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Modifier",
                                                tint = blueSkye
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (isEditingStoreName) {
                                    OutlinedTextField(
                                        value = storeName,
                                        onValueChange = { storeName = it },
                                        placeholder = { 
                                            Text(
                                                "Nom du magasin",
                                                color = Color.White.copy(alpha = 0.6f)
                                            ) 
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (storeName.isNotBlank() && storeName != selectedStore?.name) {
                                                    isSavingStoreName = true
                                                    storeViewModel.updateStoreName(
                                                        storeId = storeId.toLongOrNull() ?: 0L,
                                                        newName = storeName
                                                    ) {
                                                        isSavingStoreName = false
                                                        isEditingStoreName = false
                                                        successMessage = "Nom du magasin modifié avec succès !"
                                                    }
                                                } else {
                                                    isEditingStoreName = false
                                                }
                                            }
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = blueSkye,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                            cursorColor = blueSkye
                                        ),
                                        enabled = !isSavingStoreName
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                storeName = selectedStore?.name ?: ""
                                                isEditingStoreName = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isSavingStoreName,
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text("Annuler")
                                        }
                                        
                                        Button(
                                            onClick = {
                                                if (storeName.isNotBlank() && storeName != selectedStore?.name) {
                                                    isSavingStoreName = true
                                                    storeViewModel.updateStoreName(
                                                        storeId = storeId.toLongOrNull() ?: 0L,
                                                        newName = storeName
                                                    ) {
                                                        isSavingStoreName = false
                                                        isEditingStoreName = false
                                                        successMessage = "Nom du magasin modifié avec succès !"
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isSavingStoreName && storeName.isNotBlank() && storeName != selectedStore?.name,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = blueSkye,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            if (isSavingStoreName) {
                                                CircularProgressIndicator(
                                                    color = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Text("Sauvegarder")
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = selectedStore?.name ?: "Chargement...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Réorganiser les catégories",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    Text(
                        text = "Maintenez appuyé et glissez pour réorganiser",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .dragContainer(dragDropState),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(categories, key = { _, item -> item.id }) { index, item ->
                            DraggableItem(dragDropState, index) { isDragging ->
                                CategoryOrderCard(
                                    category = item,
                                    index = index,
                                    isDragging = isDragging
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            isSaving = true
                            itemViewModel.saveCategoryOrders(categories, storeId.toLongOrNull() ?: 0L) {
                                isSaving = false
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = blueSkye,
                            contentColor = Color.White
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Sauvegarder les modifications")
                    }
                }
            }
        }
    }
    
    // Gestion des erreurs
    error?.let { errorMessage ->
        DialogAlert(
            message = errorMessage,
            onConfirm = { storeViewModel.clearError() }
        )
    }
    
    // Gestion des messages de succès
    successMessage?.let { message ->
        SuccessDialog(
            message = message,
            onDismiss = { successMessage = null }
        )
    }
}

@Composable
fun CategoryOrderCard(
    category: CategoryWithOrder,
    index: Int,
    isDragging: Boolean
) {
    val elevation by animateDpAsState(if (isDragging) 8.dp else 2.dp, label = "elevation")
    
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDragging) 0.2f else 0.1f),
                            Color.White.copy(alpha = if (isDragging) 0.1f else 0.05f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Position: ${index + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Glisser pour réorganiser",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit
): DragDropState {
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DragDropState(state = lazyListState, onMove = onMove, scope = scope)
    }
    LaunchedEffect(state) {
        while (true) {
            val diff = state.scrollChannel.receive()
            lazyListState.scrollBy(diff)
        }
    }
    return state
}

class DragDropState internal constructor(
    private val state: LazyListState,
    private val scope: CoroutineScope,
    private val onMove: (Int, Int) -> Unit,
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    internal val scrollChannel = Channel<Float>()

    private var draggingItemDraggedDelta by mutableFloatStateOf(0f)
    private var draggingItemInitialOffset by mutableIntStateOf(0)
    
    internal val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggingItemDraggedDelta - item.offset
        } ?: 0f

    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggingItemIndex }

    internal var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set

    internal var previousItemOffset = Animatable(0f)
        private set

    internal fun onDragStart(offset: Offset) {
        state.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> 
                offset.y.toInt() in item.offset..(item.offset + item.size) 
            }
            ?.also {
                draggingItemIndex = it.index
                draggingItemInitialOffset = it.offset
            }
    }

    internal fun onDragInterrupted() {
        if (draggingItemIndex != null) {
            previousIndexOfDraggedItem = draggingItemIndex
            val startOffset = draggingItemOffset
            scope.launch {
                previousItemOffset.snapTo(startOffset)
                previousItemOffset.animateTo(
                    0f,
                    spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 1f),
                )
                previousIndexOfDraggedItem = null
            }
        }
        draggingItemDraggedDelta = 0f
        draggingItemIndex = null
        draggingItemInitialOffset = 0
    }

    internal fun onDrag(offset: Offset) {
        draggingItemDraggedDelta += offset.y

        val draggingItem = draggingItemLayoutInfo ?: return
        val startOffset = draggingItem.offset + draggingItemOffset
        val endOffset = startOffset + draggingItem.size
        val middleOffset = startOffset + (endOffset - startOffset) / 2f

        val targetItem = state.layoutInfo.visibleItemsInfo.find { item ->
            middleOffset.toInt() in item.offset..item.offsetEnd &&
                    draggingItem.index != item.index
        }
        
        if (targetItem != null) {
            if (draggingItem.index == state.firstVisibleItemIndex ||
                targetItem.index == state.firstVisibleItemIndex) {
                scope.launch {
                    state.scrollToItem(
                        state.firstVisibleItemIndex,
                        state.firstVisibleItemScrollOffset,
                    )
                }
            }
            onMove.invoke(draggingItem.index, targetItem.index)
            draggingItemIndex = targetItem.index
        } else {
            val overscroll = when {
                draggingItemDraggedDelta > 0 ->
                    (endOffset - state.layoutInfo.viewportEndOffset).coerceAtLeast(0f)
                draggingItemDraggedDelta < 0 ->
                    (startOffset - state.layoutInfo.viewportStartOffset).coerceAtMost(0f)
                else -> 0f
            }
            if (overscroll != 0f) {
                scrollChannel.trySend(overscroll)
            }
        }
    }

    private val LazyListItemInfo.offsetEnd: Int
        get() = this.offset + this.size
}

@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.dragContainer(dragDropState: DragDropState): Modifier {
    return pointerInput(dragDropState) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset -> dragDropState.onDragStart(offset) },
            onDrag = { change, offset ->
                change.consume()
                dragDropState.onDrag(offset)
            },
            onDragEnd = { dragDropState.onDragInterrupted() },
            onDragCancel = { dragDropState.onDragInterrupted() }
        )
    }
}

@Composable
fun DraggableItem(
    dragDropState: DragDropState,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(isDragging: Boolean) -> Unit,
) {
    val dragging = index == dragDropState.draggingItemIndex
    val draggingModifier = if (dragging) {
        Modifier.graphicsLayer {
            translationY = dragDropState.draggingItemOffset
            shadowElevation = 8.dp.toPx()
        }
    } else if (index == dragDropState.previousIndexOfDraggedItem) {
        Modifier.graphicsLayer {
            translationY = dragDropState.previousItemOffset.value
        }
    } else {
        Modifier
    }
    
    Column(modifier = modifier.then(draggingModifier)) { 
        content(dragging) 
    }
} 