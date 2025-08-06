package com.yrezgui.contactexperiments

import android.net.Uri
import android.provider.ContactsContract.Contacts
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.PickContact
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * long	_ID	read-only	Row ID. Consider using LOOKUP_KEY instead.
 * String	LOOKUP_KEY	read-only	An opaque value that contains hints on how to find the contact if its row id changed as a result of a sync or aggregation.
 * long	NAME_RAW_CONTACT_ID	read-only	The ID of the raw contact that contributes the display name to the aggregate contact. During aggregation one of the constituent raw contacts is chosen using a heuristic: a longer name or a name with more diacritic marks or more upper case characters is chosen.
 * String	DISPLAY_NAME_PRIMARY	read-only	The display name for the contact. It is the display name contributed by the raw contact referred to by the NAME_RAW_CONTACT_ID column.
 * long	PHOTO_ID	read-only	Reference to the row in the ContactsContract.Data table holding the photo. That row has the mime type CommonDataKinds.Photo#CONTENT_ITEM_TYPE. The value of this field is computed automatically based on the CommonDataKinds.Photo#IS_SUPER_PRIMARY field of the data rows of that mime type.
 * long	PHOTO_URI	read-only	A URI that can be used to retrieve the contact's full-size photo. This column is the preferred method of retrieving the contact photo.
 * long	PHOTO_THUMBNAIL_URI	read-only	A URI that can be used to retrieve the thumbnail of contact's photo. This column is the preferred method of retrieving the contact photo.
 * int	IN_VISIBLE_GROUP	read-only	An indicator of whether this contact is supposed to be visible in the UI. "1" if the contact has at least one raw contact that belongs to a visible group; "0" otherwise.
 * int	HAS_PHONE_NUMBER	read-only	An indicator of whether this contact has at least one phone number. "1" if there is at least one phone number, "0" otherwise.
 * int	STARRED	read/write	An indicator for favorite contacts: '1' if favorite, '0' otherwise. When raw contacts are aggregated, this field is automatically computed: if any constituent raw contacts are starred, then this field is set to '1'. Setting this field automatically changes the corresponding field on all constituent raw contacts.
 * String	CUSTOM_RINGTONE	read/write	A custom ringtone associated with a contact. Typically this is the URI returned by an activity launched with the android.media.RingtoneManager#ACTION_RINGTONE_PICKER intent.
 * int	SEND_TO_VOICEMAIL	read/write	An indicator of whether calls from this contact should be forwarded directly to voice mail ('1') or not ('0'). When raw contacts are aggregated, this field is automatically computed: if all constituent raw contacts have SEND_TO_VOICEMAIL=1, then this field is set to '1'. Setting this field automatically changes the corresponding field on all constituent raw contacts.
 * int	CONTACT_PRESENCE	read-only	Contact IM presence status. See StatusUpdates for individual status definitions. Automatically computed as the highest presence of all constituent raw contacts. The provider may choose not to store this value in persistent storage. The expectation is that presence status will be updated on a regular basis.
 * String	CONTACT_STATUS	read-only	Contact's latest status update. Automatically computed as the latest of all constituent raw contacts' status updates.
 * long	CONTACT_STATUS_TIMESTAMP	read-only	The absolute time in milliseconds when the latest status was inserted/updated.
 * String	CONTACT_STATUS_RES_PACKAGE	read-only	The package containing resources for this status: label and icon.
 * long	CONTACT_STATUS_LABEL	read-only	The resource ID of the label describing the source of contact status, e.g. "Google Talk". This resource is scoped by the CONTACT_STATUS_RES_PACKAGE.
 * long	CONTACT_STATUS_ICON	read-only	The resource ID of the icon for the source of contact status. This resource is scoped by the CONTACT_STATUS_RES_PACKAGE.
 */
data class PickContactDetails(
    val contactUri: Uri,
    val _id: Long,
    val lookupKey: String,
    val nameRawContactId: Long,
    val displayNamePrimary: String,
    val photoId: Long,
    val photoUri: Uri,
    val photoThumbnailUri: Uri,
    val inVisibleGroup: Int,
    val hasPhoneNumber: Int,
    val starred: Int,
    val customRingtone: String,
    val sendToVoicemail: Int,
    val contactPresence: Int,
    val contactStatus: String,
    val contactStatusTimestamp: Long,
    val contactStatusResPackage: String,
    val contactStatusLabel: Long,
    val contactStatusIcon: Long
)

enum class ContactDataTable {
    Name, Email, PhoneNumber
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickIntent() {
    val coroutine = rememberCoroutineScope()
    val contentResolver = LocalContext.current.contentResolver
    var selectedContact by remember { mutableStateOf<PickContactDetails?>(null) }

    val pickContact = rememberLauncherForActivityResult(PickContact()) { contactUri ->
        val contactUri = contactUri ?: return@rememberLauncherForActivityResult
        val queryFields = arrayOf(
            Contacts._ID,
            Contacts.LOOKUP_KEY,
            Contacts.NAME_RAW_CONTACT_ID,
            Contacts.DISPLAY_NAME_PRIMARY,
            Contacts.PHOTO_ID,
            Contacts.PHOTO_URI,
            Contacts.PHOTO_THUMBNAIL_URI,
            Contacts.IN_VISIBLE_GROUP,
            Contacts.HAS_PHONE_NUMBER,
            Contacts.STARRED,
            Contacts.CUSTOM_RINGTONE,
            Contacts.SEND_TO_VOICEMAIL,
            Contacts.CONTACT_PRESENCE,
            Contacts.CONTACT_STATUS,
            Contacts.CONTACT_STATUS_TIMESTAMP,
            Contacts.CONTACT_STATUS_RES_PACKAGE,
            Contacts.CONTACT_STATUS_LABEL,
            Contacts.CONTACT_STATUS_ICON
        )

        coroutine.launch(Dispatchers.IO) {
            contentResolver.query(
                contactUri,
                queryFields,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.count == 0) return@launch
                cursor.moveToFirst()

                val idColumn = cursor.getColumnIndexOrThrow(Contacts._ID)
                val lookupKeyColumn = cursor.getColumnIndexOrThrow(Contacts.LOOKUP_KEY)
                val nameRawContactIdColumn =
                    cursor.getColumnIndexOrThrow(Contacts.NAME_RAW_CONTACT_ID)
                val displayNamePrimaryColumn =
                    cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME_PRIMARY)
                val photoIdColumn = cursor.getColumnIndexOrThrow(Contacts.PHOTO_ID)
                val photoUriColumn = cursor.getColumnIndexOrThrow(Contacts.PHOTO_URI)
                val photoThumbnailUriColumn =
                    cursor.getColumnIndexOrThrow(Contacts.PHOTO_THUMBNAIL_URI)
                val inVisibleGroupColumn = cursor.getColumnIndexOrThrow(Contacts.IN_VISIBLE_GROUP)
                val hasPhoneNumberColumn = cursor.getColumnIndexOrThrow(Contacts.HAS_PHONE_NUMBER)
                val starredColumn = cursor.getColumnIndexOrThrow(Contacts.STARRED)
                val customRingtoneColumn = cursor.getColumnIndexOrThrow(Contacts.CUSTOM_RINGTONE)
                val sendToVoicemailColumn = cursor.getColumnIndexOrThrow(Contacts.SEND_TO_VOICEMAIL)
                val contactPresenceColumn = cursor.getColumnIndexOrThrow(Contacts.CONTACT_PRESENCE)
                val contactStatusColumn = cursor.getColumnIndexOrThrow(Contacts.CONTACT_STATUS)
                val contactStatusTimestampColumn =
                    cursor.getColumnIndexOrThrow(Contacts.CONTACT_STATUS_TIMESTAMP)
                val contactStatusResPackageColumn =
                    cursor.getColumnIndexOrThrow(Contacts.CONTACT_STATUS_RES_PACKAGE)
                val contactStatusLabelColumn =
                    cursor.getColumnIndexOrThrow(Contacts.CONTACT_STATUS_LABEL)
                val contactStatusIconColumn =
                    cursor.getColumnIndexOrThrow(Contacts.CONTACT_STATUS_ICON)

                selectedContact = PickContactDetails(
                    contactUri = contactUri,
                    _id = cursor.getLong(idColumn),
                    lookupKey = cursor.getStringOrNull(lookupKeyColumn) ?: "No lookup key",
                    nameRawContactId = cursor.getLong(nameRawContactIdColumn),
                    displayNamePrimary = cursor.getStringOrNull(displayNamePrimaryColumn)
                        ?: "Unknown Name",
                    photoId = cursor.getLong(photoIdColumn),
                    photoUri = cursor.getStringOrNull(photoUriColumn)?.toUri() ?: Uri.EMPTY,
                    photoThumbnailUri = cursor.getStringOrNull(photoThumbnailUriColumn)?.toUri()
                        ?: Uri.EMPTY,
                    inVisibleGroup = cursor.getInt(inVisibleGroupColumn),
                    hasPhoneNumber = cursor.getInt(hasPhoneNumberColumn),
                    starred = cursor.getInt(starredColumn),
                    customRingtone = cursor.getStringOrNull(customRingtoneColumn)
                        ?: "No custom ringtone",
                    sendToVoicemail = cursor.getInt(sendToVoicemailColumn),
                    contactPresence = cursor.getInt(contactPresenceColumn),
                    contactStatus = cursor.getStringOrNull(contactStatusColumn) ?: "No status",
                    contactStatusTimestamp = cursor.getLong(contactStatusTimestampColumn),
                    contactStatusResPackage = cursor.getStringOrNull(contactStatusResPackageColumn)
                        ?: "No status package",
                    contactStatusLabel = cursor.getLong(contactStatusLabelColumn),
                    contactStatusIcon = cursor.getLong(contactStatusIconColumn)
                )

                Log.d("PickIntent", "selectedContact: $selectedContact")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ACTION_PICK") },
                actions = {
                    Button(onClick = { pickContact.launch(null) }) {
                        Text("Pick Contact")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val pagerState = rememberPagerState(pageCount = { 3 })

        selectedContact?.let { details ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                    ContactDataTable.entries.forEach { item ->
                        Tab(
                            selected = item.ordinal == pagerState.currentPage,
                            onClick = {
                                coroutine.launch {
                                    pagerState.animateScrollToPage(item.ordinal)
                                }
                            },
                            text = {
                                Text(
                                    text = item.name,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
                HorizontalPager(modifier = Modifier.weight(1f), state = pagerState) { page ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Text(buildAnnotatedString {
                                // Contact URI
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Contact URI")
                                }
                                append(": ${details.contactUri}")

                                // _ID
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("_ID")
                                }
                                append(": ${details._id}")

                                // Lookup Key
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Lookup Key")
                                }
                                append(": ${details.lookupKey}")

                                // Name Raw Contact ID
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Name Raw Contact ID")
                                }
                                append(": ${details.nameRawContactId}")

                                // Display Name Primary
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Display Name Primary")
                                }
                                append(": ${details.displayNamePrimary}")

                                // Photo ID
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Photo ID")
                                }
                                append(": ${details.photoId}")

                                // Photo URI
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Photo URI")
                                }
                                append(": ${details.photoUri}")

                                // Photo Thumbnail URI
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Photo Thumbnail URI")
                                }
                                append(": ${details.photoThumbnailUri}")

                                // In Visible Group
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("In Visible Group")
                                }
                                append(": ${details.inVisibleGroup}")

                                // Has Phone Number
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Has Phone Number")
                                }
                                append(": ${details.hasPhoneNumber}")

                                // Starred
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Starred")
                                }
                                append(": ${details.starred}")

                                // Custom Ringtone
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Custom Ringtone")
                                }
                                append(": ${details.customRingtone}")

                                // Send To Voicemail
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Send To Voicemail")
                                }
                                append(": ${details.sendToVoicemail}")

                                // Contact Presence
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Contact Presence")
                                }
                                append(": ${details.contactPresence}")

                                // Contact Status
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Contact Status")
                                }
                                append(": ${details.contactStatus}")

                                // Contact Status Timestamp
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Contact Status Timestamp")
                                }
                                append(": ${details.contactStatusTimestamp}")

                                // Contact Status Res Package
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Contact Status Res Package")
                                }
                                append(": ${details.contactStatusResPackage}")

                                // Contact Status Label
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Contact Status Label")
                                }
                                append(": ${details.contactStatusLabel}")

                                // Contact Status Icon
                                appendLine()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Contact Status Icon")
                                }
                                append(": ${details.contactStatusIcon}")
                            })
                        }
                    }
                }
            }
        }
    }
}
