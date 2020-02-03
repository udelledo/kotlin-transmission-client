package org.transmission.client

internal class SessionRequest(method: String) : TransmissionRequest(method, null) {
    override fun getArguments() = mutableMapOf<String, Any>()
}

internal class GetSessionInformationRequest(private var fields: List<String>? = null) : TransmissionRequest(Actions.SESSION_GET, null) {
    override fun getArguments() = mutableMapOf<String, Any>()
            .apply {
                fields?.let { put("fields", it) }
            }
}

class SetSessionInformationRequest(private val newSessionValue: SessionInfo) : TransmissionRequest(Actions.SESSION_SET, null) {
    override fun getArguments() = mutableMapOf<String, Any>().apply {
        putAll(mapper.convertValue(newSessionValue, this.javaClass))
    }
}