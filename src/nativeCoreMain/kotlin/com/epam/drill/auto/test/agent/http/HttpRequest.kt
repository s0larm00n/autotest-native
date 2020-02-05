package com.epam.drill.auto.test.agent.http

/*
POST /cgi-bin/process.cgi HTTP/1.1
User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)
Host: www.tutorialspoint.com
Content-Type: text/xml; charset=utf-8
Content-Length: 88
Accept-Language: en-us
Accept-Encoding: gzip, deflate
Connection: Keep-Alive
*/

class HttpRequest(
    host: String,
    port: String,
    private val path: String,
    body: String,
    private val method: String
) {
    private val protocol = "HTTP/1.1"
    private val lineEnding = "\r\n"
    private val suffix = if (body.isEmpty()) lineEnding else lineEnding + body + lineEnding
    private val headers = mutableMapOf(
        "Host" to "$host:$port",
        "Accept" to "*/*",
        "User-Agent" to "Mozilla/5.0"
    )

    init {
        if (body.isNotBlank()) {
            headers["Content-Length"] = body.length.toString()
        }
    }

    fun addHeader(key: String, value: String) = apply {
        headers[key] = value
    }

    fun build() = headers.map { (key, value) ->
        "$key: $value$lineEnding"
    }.fold("$method $path $protocol$lineEnding") { result, nextHeader ->
        result + nextHeader
    } + suffix
}
