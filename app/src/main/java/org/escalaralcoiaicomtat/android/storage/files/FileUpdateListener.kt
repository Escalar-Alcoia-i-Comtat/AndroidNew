package org.escalaralcoiaicomtat.android.storage.files

open class FileUpdateListener(val path: LocalFile) {
    /** Any event was called */
    open fun onAny(file: LocalFile) { }

    /** Data was read from a file */
    open fun onAccess(file: LocalFile) { }

    /** Metadata (permissions, owner, timestamp) was changed explicitly */
    open fun onAttrib(file: LocalFile) { }

    /** Someone had a file or directory open read-only, and closed it */
    open fun onCloseNoWrite(file: LocalFile) { }

    /** Someone had a file or directory open for writing, and closed it */
    open fun onCloseWrite(file: LocalFile) { }

    /** A new file or subdirectory was created under the monitored directory */
    open fun onCreate(file: LocalFile) { }

    /** A file was deleted from the monitored directory */
    open fun onDelete(file: LocalFile) { }

    /** The monitored file or directory was deleted; monitoring effectively stops */
    open fun onDeleteSelf(file: LocalFile) { }

    /** Data was written to a file */
    open fun onModify(file: LocalFile) { }

    /** A file or subdirectory was moved from the monitored directory */
    open fun onMovedFrom(file: LocalFile) { }

    /** A file or subdirectory was moved to the monitored directory */
    open fun onMovedTo(file: LocalFile) { }

    /** A file or directory was opened */
    open fun onOpen(file: LocalFile) { }
}
