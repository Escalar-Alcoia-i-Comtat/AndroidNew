package org.escalaralcoiaicomtat.android.exception.remote

import java.util.UUID

class RemoteFileNotFound(uuid: UUID) : IllegalStateException(
    "The requested file was not found on the remote server. UUID=$uuid"
) {
    companion object {
        const val ERROR_CODE = 3
    }
}
