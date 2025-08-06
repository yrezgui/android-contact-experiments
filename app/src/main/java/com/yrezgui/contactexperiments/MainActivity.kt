package com.yrezgui.contactexperiments

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yrezgui.contactexperiments.ui.theme.ContactExperimentsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactExperimentsTheme {
                val backStack = remember { mutableStateListOf<Route>(PickIntent) }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<Home> { Home(onApiNavigate = { backStack.add(it) }) }
                        entry<PickIntent> { PickIntent() }
                    }
                )
            }
        }
    }
}

sealed interface Route
data object Home : Route
data object PickIntent : Route

val apiList = listOf(PickIntent)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(onApiNavigate: (Route) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Contact API Experiments") })
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(apiList) {
                ListItem(
                    headlineContent = { Text(text = it.javaClass.simpleName) },
                    modifier = Modifier.clickable {
                        onApiNavigate(it)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}