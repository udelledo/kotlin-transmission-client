package org.transmission.client

internal class SessionRequest(method: String) : TransmissionRequest(method, null) {
    override fun getArguments() = mutableMapOf<String, Any>()
}

internal class GetSessionInformationRequest : TransmissionRequest(Actions.SESSION_GET, null) {
    override fun getArguments() = mutableMapOf<String, Any>()
}