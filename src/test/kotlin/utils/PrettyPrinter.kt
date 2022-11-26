package utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter

class PrettyPrinter : DefaultPrettyPrinter() {
    init {
        this._objectIndenter = DefaultIndenter("  ", "\n")
        this._arrayIndenter = _objectIndenter
    }

    override fun createInstance(): DefaultPrettyPrinter {
        return PrettyPrinter()
    }

    override fun writeObjectFieldValueSeparator(jg: JsonGenerator) {
        jg.writeRaw(": ")
    }
}