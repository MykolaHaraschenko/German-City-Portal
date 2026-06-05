import eu.europa.ec.eudi.sdjwt.NimbusSdJwtOps
import java.lang.reflect.Modifier

fun main() {
    val methods = NimbusSdJwtOps::class.java.methods
    for (m in methods) {
        if (m.name == "issuer") {
            println(m)
        }
    }
}
