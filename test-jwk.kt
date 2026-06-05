import com.nimbusds.jose.jwk.JWK
import java.io.File

fun main() {
    val pem = File("keys/private_key.pem").readText()
    try {
        val jwk = JWK.parseFromPEMEncodedObjects(pem)
        println("JWK: " + jwk.toPublicJWK().toJSONString())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
