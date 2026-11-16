package com.veci.storeappofline.presentation.store

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductCategory
import com.veci.storeappofline.domain.model.ProductEmojis
import com.veci.storeappofline.domain.model.ProductSupplements
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: StoreViewModel,
    onNavigateToCart: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val json = pendingExportJson
        pendingExportJson = null
        if (uri == null || json == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(json.toByteArray(Charsets.UTF_8))
            }
            viewModel.onExportSuccess()
        } catch (e: Exception) {
            viewModel.onExportError(e.message)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            }.orEmpty()
            viewModel.importStockJson(json)
        } catch (e: Exception) {
            viewModel.onImportError(e.message)
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.userMessageShown()
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Restablir productes inicials") },
            text = { Text("Estàs segur que vols restablir els productes de mostra inicials? Això carregarà el menú per defecte.") },
            confirmButton = {
                Button(onClick = {
                    showResetConfirmDialog = false
                    viewModel.resetSampleProducts()
                }) {
                    Text("Sí, restablir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel·lar")
                }
            }
        )
    }

    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Eliminar producte") },
            text = { Text("Estàs segur que vols eliminar '${product.name}' definitivament de la llista de productes?") },
            confirmButton = {
                Button(
                    onClick = {
                        val target = product
                        productToDelete = null
                        viewModel.deleteProduct(target.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel·lar")
                }
            }
        )
    }

    uiState.sandwichPendingSupplements?.let { product ->
        SandwichSupplementDialog(
            product = product,
            cheesePrice = uiState.cheesePrice,
            onionPrice = uiState.onionPrice,
            onDismiss = { viewModel.dismissCheeseDialog() },
            onConfirm = { withCheese, withOnion -> viewModel.confirmSandwichAdd(withCheese, withOnion) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carta del Bar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToCart) {
                        BadgedBox(
                            badge = {
                                if (uiState.totalCartItemsCount > 0) {
                                    Badge {
                                        Text(uiState.totalCartItemsCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cistella de la comanda"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val tabs = listOf(
                StoreTab.BEVERAGES to "Begudes",
                StoreTab.FOOD to "Menjar",
                StoreTab.ALL to "Tots",
                StoreTab.MANAGEMENT to "Gestió"
            )
            val selectedTabIndex = tabs.indexOfFirst { it.first == uiState.selectedTab }.coerceAtLeast(0)

            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, (tab, label) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(label, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            if (uiState.selectedTab == StoreTab.MANAGEMENT) {
                ManagementSection(
                    products = uiState.products,
                    cheesePrice = uiState.cheesePrice,
                    onionPrice = uiState.onionPrice,
                    isLoading = uiState.isLoading,
                    onUpdateCheesePrice = { price -> viewModel.updateCheesePrice(price) },
                    onUpdateOnionPrice = { price -> viewModel.updateOnionPrice(price) },
                    onResetSampleProducts = { showResetConfirmDialog = true },
                    onAddStock = { productId, amount -> viewModel.addStock(productId, amount) },
                    onRemoveStock = { productId, amount -> viewModel.removeStock(productId, amount) },
                    onClearStock = { productId -> viewModel.clearStock(productId) },
                    onDeleteProduct = { product -> productToDelete = product },
                    onAddProduct = { name, price, stock, category, isSandwich, emoji ->
                        viewModel.addNewProduct(name, price, stock, category, isSandwich, emoji)
                    },
                    onExport = {
                        viewModel.prepareExportJson { json ->
                            pendingExportJson = json
                            val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            exportLauncher.launch("estoc_barraques_$stamp.json")
                        }
                    },
                    onImport = {
                        importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (uiState.displayedProducts.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No hi ha productes disponibles en aquesta categoria.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { showResetConfirmDialog = true }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Carregar productes inicials")
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.displayedProducts,
                                key = { it.id }
                            ) { product ->
                                ProductCard(
                                    product = product,
                                    cheesePrice = uiState.cheesePrice,
                                    onionPrice = uiState.onionPrice,
                                    onAddToCart = { viewModel.onAddProductClicked(product) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductEmojiAvatar(
    emoji: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji.ifBlank { Product.DEFAULT_EMOJI },
            fontSize = (size.value * 0.45f).sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmojiPickerRow(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Emoticona", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ProductEmojis.OPTIONS) { emoji ->
                val selected = emoji == selectedEmoji
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
private fun SandwichSupplementDialog(
    product: Product,
    cheesePrice: Double,
    onionPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (withExtraCheese: Boolean, withExtraOnion: Boolean) -> Unit
) {
    var withCheese by remember { mutableStateOf(false) }
    var withOnion by remember { mutableStateOf(false) }

    var totalPrice = product.price
    if (withCheese) totalPrice += cheesePrice
    if (withOnion) totalPrice += onionPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { ProductEmojiAvatar(emoji = product.emoji, size = 56.dp) },
        title = { Text(product.name) },
        text = {
            Column {
                Text(
                    text = "Vols afegir suplements a l'entrepà?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = withCheese,
                            onClick = { withCheese = !withCheese },
                            role = Role.Checkbox
                        )
                ) {
                    Checkbox(
                        checked = withCheese,
                        onCheckedChange = { withCheese = it }
                    )
                    Text(
                        text = "+ ${ProductSupplements.CHEESE_LABEL} (+${String.format(Locale.US, "%.2f", cheesePrice)} €)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = withOnion,
                            onClick = { withOnion = !withOnion },
                            role = Role.Checkbox
                        )
                ) {
                    Checkbox(
                        checked = withOnion,
                        onCheckedChange = { withOnion = it }
                    )
                    Text(
                        text = "+ ${ProductSupplements.ONION_LABEL} (+${String.format(Locale.US, "%.2f", onionPrice)} €)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Total: ${String.format(Locale.US, "%.2f", totalPrice)} €",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(withCheese, withOnion) }) {
                Text("Afegir a la cistella")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·lar")
            }
        }
    )
}

@Composable
fun ManagementSection(
    products: List<Product>,
    cheesePrice: Double,
    onionPrice: Double,
    isLoading: Boolean,
    onUpdateCheesePrice: (Double) -> Unit,
    onUpdateOnionPrice: (Double) -> Unit,
    onResetSampleProducts: () -> Unit,
    onAddStock: (productId: Long, amount: Int) -> Unit,
    onRemoveStock: (productId: Long, amount: Int) -> Unit,
    onClearStock: (productId: Long) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onAddProduct: (
        name: String,
        price: Double,
        stock: Int,
        category: ProductCategory,
        isSandwich: Boolean,
        emoji: String
    ) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("10") }
    var category by remember { mutableStateOf(ProductCategory.FOOD) }
    var isSandwich by remember { mutableStateOf(false) }
    var emoji by remember { mutableStateOf("🥪") }

    var cheesePriceText by remember(cheesePrice) {
        mutableStateOf(String.format(Locale.US, "%.2f", cheesePrice))
    }
    var onionPriceText by remember(onionPrice) {
        mutableStateOf(String.format(Locale.US, "%.2f", onionPrice))
    }

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Preus dels suplements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configura el preu dels suplements d'entrepans",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = cheesePriceText,
                            onValueChange = { cheesePriceText = it },
                            label = { Text("Formatge (€)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val p = cheesePriceText.replace(',', '.').toDoubleOrNull()
                                if (p != null && p >= 0) {
                                    onUpdateCheesePrice(p)
                                }
                            },
                            enabled = cheesePriceText.replace(',', '.').toDoubleOrNull() != null
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guardar")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = onionPriceText,
                            onValueChange = { onionPriceText = it },
                            label = { Text("Ceba (€)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val p = onionPriceText.replace(',', '.').toDoubleOrNull()
                                if (p != null && p >= 0) {
                                    onUpdateOnionPrice(p)
                                }
                            },
                            enabled = onionPriceText.replace(',', '.').toDoubleOrNull() != null
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guardar")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nou producte",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProductEmojiAvatar(emoji = emoji, size = 52.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nom") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    EmojiPickerRow(
                        selectedEmoji = emoji,
                        onEmojiSelected = { emoji = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Preu (€)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text("Estoc") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Categoria", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = category == ProductCategory.BEVERAGE,
                            onClick = {
                                category = ProductCategory.BEVERAGE
                                isSandwich = false
                                if (emoji == "🥪") emoji = "🍺"
                            },
                            label = { Text("Beguda") }
                        )
                        FilterChip(
                            selected = category == ProductCategory.FOOD,
                            onClick = {
                                category = ProductCategory.FOOD
                                if (emoji == "🍺") emoji = "🥪"
                            },
                            label = { Text("Menjar") }
                        )
                    }
                    if (category == ProductCategory.FOOD) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSandwich,
                                    onClick = { isSandwich = !isSandwich },
                                    role = Role.Checkbox
                                )
                        ) {
                            Checkbox(
                                checked = isSandwich,
                                onCheckedChange = { isSandwich = it }
                            )
                            Text("És un entrepà (permet formatge i ceba)")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val price = priceText.replace(',', '.').toDoubleOrNull()
                            val stock = stockText.toIntOrNull()
                            if (name.isBlank() || price == null || stock == null) return@Button
                            onAddProduct(name, price, stock, category, isSandwich, emoji)
                            name = ""
                            priceText = ""
                            stockText = "10"
                            isSandwich = false
                            emoji = if (category == ProductCategory.BEVERAGE) "🍺" else "🥪"
                        },
                        enabled = name.isNotBlank() &&
                            priceText.replace(',', '.').toDoubleOrNull() != null &&
                            stockText.toIntOrNull() != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Afegir producte")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Gestió d'estoc",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(products, key = { it.id }) { product ->
            StockProductCard(
                product = product,
                onAddStock = { amount -> onAddStock(product.id, amount) },
                onRemoveStock = { amount -> onRemoveStock(product.id, amount) },
                onClearStock = { onClearStock(product.id) },
                onDeleteProduct = { onDeleteProduct(product) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Exportar / Importar / Restaurar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onExport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exportar JSON")
                }
                OutlinedButton(
                    onClick = onImport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importar JSON")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onResetSampleProducts,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restablir productes inicials de mostra")
            }
            Text(
                text = "Exporta o importa l'estoc. Si l'estoc s'ha esborrat, utilitza el botó per carregar els productes inicials.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun StockProductCard(
    product: Product,
    onAddStock: (amount: Int) -> Unit,
    onRemoveStock: (amount: Int) -> Unit,
    onClearStock: () -> Unit,
    onDeleteProduct: () -> Unit
) {
    var amountText by remember(product.id) { mutableStateOf("10") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductEmojiAvatar(emoji = product.emoji)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${product.category.displayName} · Estoc actual: ${product.stockQuantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDeleteProduct) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar producte",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Quantitat") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val amount = amountText.toIntOrNull()
                        if (amount != null && amount > 0) onAddStock(amount)
                    },
                    enabled = (amountText.toIntOrNull() ?: 0) > 0
                ) {
                    Text("+ Estoc")
                }
                OutlinedButton(
                    onClick = {
                        val amount = amountText.toIntOrNull()
                        if (amount != null && amount > 0) onRemoveStock(amount)
                    },
                    enabled = (amountText.toIntOrNull() ?: 0) > 0
                ) {
                    Text("- Estoc")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(5, 10, 20).forEach { quick ->
                        OutlinedButton(
                            onClick = { onAddStock(quick) }
                        ) {
                            Text("+$quick")
                        }
                    }
                }
                TextButton(
                    onClick = onClearStock
                ) {
                    Text("Estoc 0", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    cheesePrice: Double,
    onionPrice: Double,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutOfStock = product.stockQuantity <= 0

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductEmojiAvatar(emoji = product.emoji)
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.US, "%.2f €", product.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (product.isSandwich) {
                    Text(
                        text = "+ Suplements: Formatge (${String.format(Locale.US, "%.2f", cheesePrice)} €), Ceba (${String.format(Locale.US, "%.2f", onionPrice)} €)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isOutOfStock) "Sense estoc" else "Estoc: ${product.stockQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOutOfStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }

            Button(
                onClick = onAddToCart,
                enabled = !isOutOfStock
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Afegir")
            }
        }
    }
}
