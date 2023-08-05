package org.escalaralcoiaicomtat.android.exception.remote

class ServerException(
    message: String,
    cause: Throwable,
    stackTrace: Array<String>
) : RuntimeException(message, cause, true, true) {
    init {
        val stackTraceElements = stackTrace.map { trace ->
            // Example trace: org.json.JSONTokener.syntaxError(JSONTokener.java:497)

            // classPath=org.json.JSONTokener.syntaxError
            val classPath = trace.substring(0, trace.indexOf('('))
            val className = classPath.substringBeforeLast('.')
            val methodName = classPath.substringAfterLast('.')

            // fileInfo=JSONTokener.java:497
            val fileInfo = trace.substringAfter('(')
                .let { it.substring(0, it.length - 1) }
            val fileName = fileInfo.substringBefore(':')
            val lineNumber = fileInfo.substringAfter(':')

            StackTraceElement(className, methodName, fileName, lineNumber.toInt())
        }
        setStackTrace(stackTraceElements.toTypedArray())
    }
}
