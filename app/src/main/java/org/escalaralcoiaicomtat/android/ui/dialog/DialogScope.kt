package org.escalaralcoiaicomtat.android.ui.dialog

interface DialogScope {
    companion object {
        fun create(onDismiss: () -> Unit) = object : DialogScope {
            override fun dismiss() = onDismiss()
        }
    }

    fun dismiss()
}
