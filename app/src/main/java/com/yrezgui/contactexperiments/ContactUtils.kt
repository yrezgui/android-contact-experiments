package com.yrezgui.contactexperiments

import android.net.Uri
import android.provider.ContactsContract

enum class ContactDataType {
    Name {
        override val uri: Uri = ContactsContract.Contacts.CONTENT_URI
    }, Phone {
        override val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    },
    Address {
        override val uri: Uri = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI
    },
    Email {
        override val uri: Uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
    };


    abstract val uri: Uri
}