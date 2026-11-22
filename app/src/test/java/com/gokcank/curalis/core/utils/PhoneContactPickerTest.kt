package com.gokcank.curalis.core.utils

import android.app.Activity
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PhoneContactPickerTest {

    /** Stands in for the contacts app's single picked phone row. */
    class PickedRowProvider : ContentProvider() {
        override fun onCreate() = true
        override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor =
            MatrixCursor(projection).apply {
                when (uri.lastPathSegment) {
                    "1" -> addRow(arrayOf(" Ayşe Yılmaz ", "+90 555 000 00 00"))
                    "2" -> addRow(arrayOf<Any?>(null, "0555 111 22 33"))
                }
            }
        override fun getType(uri: Uri): String? = null
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
    }

    private val context get() = RuntimeEnvironment.getApplication()

    @Test
    fun `the picker asks for a phone number, which needs no contacts permission`() {
        val intent = PickPhoneContact().createIntent(context, Unit)
        assertEquals(Intent.ACTION_PICK, intent.action)
        assertEquals(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, intent.data)
    }

    @Test
    fun `a cancelled pick gives no contact`() {
        val uri = Uri.parse("content://picked/1")
        assertNull(PickPhoneContact().parseResult(Activity.RESULT_CANCELED, Intent().setData(uri)))
        assertEquals(uri, PickPhoneContact().parseResult(Activity.RESULT_OK, Intent().setData(uri)))
    }

    @Test
    fun `name and number are read from the picked row`() {
        Robolectric.setupContentProvider(PickedRowProvider::class.java, "picked")

        assertEquals(PickedContact("Ayşe Yılmaz", "+90 555 000 00 00"), readPickedContact(context, Uri.parse("content://picked/1")))
        assertEquals(PickedContact(null, "0555 111 22 33"), readPickedContact(context, Uri.parse("content://picked/2")))
        assertNull(readPickedContact(context, Uri.parse("content://picked/3")))
    }
}
