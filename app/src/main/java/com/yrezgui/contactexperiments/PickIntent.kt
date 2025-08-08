package com.yrezgui.contactexperiments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.PickContact
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class PickContactDetails(
    val contactUri: Uri,
    val data: Map<String, String?> = emptyMap()
)

/**
 * Custom ActivityResultContract to handle contact data collection using ACTION_PICK intent
 * as [PickContact] is actually not returning useful data.
 */
class CustomPickContact : ActivityResultContract<Uri, Uri?>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return Intent(Intent.ACTION_PICK)
            .setDataAndType(
                ContactsContract.Contacts.CONTENT_URI,
                ContactsContract.Contacts.CONTENT_TYPE
//                        ContactsContract.Contacts.CONTENT_ITEM_TYPE
//                        ContactsContract.Contacts.CONTENT_VCARD_TYPE
            )
            .setData(input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickIntent() {
    val coroutine = rememberCoroutineScope()
    val contentResolver = LocalContext.current.contentResolver
    var selectedContact by remember { mutableStateOf<PickContactDetails?>(null) }

    val pickContact = rememberLauncherForActivityResult(CustomPickContact()) { contactUri ->
        val contactUri = contactUri ?: return@rememberLauncherForActivityResult

        coroutine.launch(Dispatchers.IO) {
            contentResolver.query(
                contactUri,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.count == 0) return@launch
                cursor.moveToFirst()

                selectedContact = PickContactDetails(contactUri, cursor.getAllData())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ACTION_PICK") },
                actions = {
                    Button(onClick = { pickContact.launch(ContactsContract.CommonDataKinds.Phone.CONTENT_URI) }) {
                        Text("Pick Contact")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        selectedContact?.let { details ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
                    item {
                        Text(buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Contact Uri: ")
                            }
                            append(details.contactUri.toString())
                            appendLine()
                        })
                    }
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(details.data.entries.toList()) {
                        Text(buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(it.key)
                            }
                            append(": ${it.value}")
                        })
                    }
                }
            }
        }
    }
}