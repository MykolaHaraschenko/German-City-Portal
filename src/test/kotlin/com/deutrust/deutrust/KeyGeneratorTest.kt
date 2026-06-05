package com.deutrust.deutrust

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.jupiter.api.Test
import java.io.File
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.Base64

class KeyGeneratorTest {

//    @Test
//    fun decodeAndPrintJwt() {
//        // Paste your signed JWT (Request Object) here:
//        val jwt =
//            "eyJhbGciOiJFUzI1NiIsInR5cCI6Im9hdXRoLWF1dGh6LXJlcStqd3QiLCJ4NWMiOlsiTUlJQ2dEQ0NBaWFnQXdJQkFnSUlCK3BOS25RdWV6WXdDZ1lJS29aSXpqMEVBd0l3S0RFTE1Ba0dBMVVFQmhNQ1JFVXhHVEFYQmdOVkJBTU1FRWRsY20xaGJpQlNaV2RwYzNSeVlYSXdIaGNOTWpZd05UTXhNRGt5TmpJd1doY05NamN3TlRNeE1Ea3lOakl3V2pCd01Rc3dDUVlEVlFRR0V3SkVSVEVmTUIwR0ExVUVDZ3dXU0dGamEyRjBhRzl1SUMwZ1IyRnlZWE5vWlc1cmJ6RWZNQjBHQTFVRVlRd1dSVlZKUkVVdE5UZENNVE0zT1VaRlFUTTVOVGhDUmpFZk1CMEdBMVVFQXd3V1NHRmphMkYwYUc5dUlDMGdSMkZ5WVhOb1pXNXJiekJaTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEEwSUFCTFRYcU5OZ3Ivd1pTcE9qbU9ERC9JSFd5SUxDaDFQMzJGQ3o5RnM4cTlIR1R3TVRVQnhLMGNxUFBpTTRmRlErNXZpdWovalhMTmkxZEc0WG1nNktWZldqZ2ZFd2dlNHdEQVlEVlIwVEFRSC9CQUl3QURBZEJnTlZIUTRFRmdRVWZ3M2M5cnhGRGUrRDZqdldVVE4yemRqQXJ0a3dId1lEVlIwakJCZ3dGb0FVcWNLajJpOXRyRlR1enJsTzZDekpMQUNEZ0RNd0RnWURWUjBQQVFIL0JBUURBZ2VBTUJJR0ExVWRKUVFMTUFrR0J5aUJqRjBGQVFZd0xnWURWUjBSQkNjd0pZSWpiR0YxYm1SeWVTMXpkR1Z5YVd4bExXTm9kV2N1Ym1keWIyc3RabkpsWlM1a1pYWXdTZ1lEVlIwZkJFTXdRVEEvb0QyZ080WTVhSFIwY0hNNkx5OXpZVzVrWW05NExtVjFaR2t0ZDJGc2JHVjBMbTl5Wnk5aGNHa3ZjM1JoZEhWekxXMWhibUZuWlcxbGJuUXZZM0pzTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFEckRoeU12T3RnU2xvU1p3SVhvL25JS1M3NmJtYnBhTlc5V1M1WGIrb3VKQUlnR3hzTjNqcFdmY3ZPRXY1eTF5SktMRCt6QUhMQms2R0h5bHpGN3dUUjRzRT0iXX0.eyJjbGllbnRfaWQiOiJ4NTA5X3Nhbl9kbnM6bGF1bmRyeS1zdGVyaWxlLWNodWcubmdyb2stZnJlZS5kZXYiLCJpc3MiOiJ4NTA5X3Nhbl9kbnM6bGF1bmRyeS1zdGVyaWxlLWNodWcubmdyb2stZnJlZS5kZXYiLCJyZXNwb25zZV91cmkiOiJodHRwczovL2xhdW5kcnktc3RlcmlsZS1jaHVnLm5ncm9rLWZyZWUuZGV2L2FwaS92MS9vcGVuaWQ0dnAvcmVzcG9uc2UvODQ0YWYyNmItYTIyNC00Yzk0LWE5M2UtODc2MGM1YTFkNjg3IiwicmVzcG9uc2VfdHlwZSI6InZwX3Rva2VuIiwicmVzcG9uc2VfbW9kZSI6ImRpcmVjdF9wb3N0Iiwic2NvcGUiOiJvcGVuaWQiLCJub25jZSI6IjE4Y2U2YWRkLTdhOWEtNGRhNi1iODNiLTVmMGQ1ZjkzMjFlNiIsInN0YXRlIjoiZmE3NTViZmMtYzY0Zi00ZGE1LTgzNWQtZDBjNzdmMzBmMWJkIiwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaWQiOiJwaWQtdmVyaWZpY2F0aW9uLXNpZyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6ImV1LmV1cm9wYS5lYy5ldWRpLnBpZC4xIiwiZm9ybWF0Ijp7InZjK3NkLWp3dCI6eyJhbGciOlsiRVMyNTYiLCJSUzI1NiJdfX0sImNvbnN0cmFpbnRzIjp7ImxpbWl0X2Rpc2Nsb3N1cmUiOiJyZXF1aXJlZCIsImZpZWxkcyI6W3sicGF0aCI6WyIkLnZjdCJdLCJmaWx0ZXIiOnsidHlwZSI6InN0cmluZyIsImNvbnN0IjoiZXUuZXVyb3BhLmVjLmV1ZGkucGlkLjEifX0seyJwYXRoIjpbIiQuZ2l2ZW5fbmFtZSJdLCJpbnRlbnRfdG9fcmV0YWluIjp0cnVlfSx7InBhdGgiOlsiJC5mYW1pbHlfbmFtZSJdLCJpbnRlbnRfdG9fcmV0YWluIjp0cnVlfV19fV19fQ.4s69IG9VGYVQA978DCJLZe-hc8VIBI9WFrwd1UHsW5ULRCiAP4DaeS3apV4qLR9I98f99C6IsfIvF-gt-gu8mA"
//
//        if (jwt.isBlank()) {
//            println("=== JWT IS EMPTY ===")
//            println("Please paste your JWT string inside the 'jwt' variable in decodeAndPrintJwt test.")
//            return
//        }
//
//        try {
//            val parts = jwt.split(".")
//            if (parts.size < 2) {
//                println("=== INVALID JWT FORMAT ===")
//                println("JWT must have at least 2 parts separated by dots.")
//                return
//            }
//
//            val mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
//
//            val headerJson = String(Base64.getUrlDecoder().decode(parts[0]))
//            val headerMap = mapper.readValue(headerJson, Map::class.java)
//
//            val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
//            val payloadMap = mapper.readValue(payloadJson, Map::class.java)
//
//            println("==================================================")
//            println("=== JWT HEADER ===")
//            println(mapper.writeValueAsString(headerMap))
//            println("==================================================")
//            println("=== JWT PAYLOAD ===")
//            println(mapper.writeValueAsString(payloadMap))
//            println("==================================================")
//
//            if (parts.size >= 3) {
//                println("=== SIGNATURE (BASE64URL) ===")
//                println(parts[2])
//                println("==================================================")
//            }
//        } catch (e: Exception) {
//            println("=== ERROR DECODING JWT ===")
//            e.printStackTrace()
//            println("==========================")
//        }
//    }
    @Test
    fun testRealWalletDecryption() {
        val jwe = "eyJlcGsiOnsia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsIngiOiJUam03M01QOVJsTVEtUjA3MjQxZFdmcTd4TldnZHRaZHBtS3o5YVNhREhNIiwieSI6IjQ0Z1VUVTRwZW9BXzdHN094Wng2WGdoTUMyUFc1X3VGcGlhd1N2UUR3M0kifSwiYXB2IjoiWXpNNE56UXpaV1V0WXpFeE1DMDBOV1l6TFRsbU9XRXRNVGt5WWpGbFpXRmtaRGM0Iiwia2lkIjoiNjQxMGYwNDMtNjczMi00YmI1LWIzYTQtYzlmZjIzMzcwMmMyIiwiYXB1IjoiYmpFeFNWbFJlVVZYVGxvdE9VWXpSRmxMYlUxcVFRIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6IkVDREgtRVMifQ..EizjvBt0ouwor-Xu.Of9J1GuMtihlHWf9HtPe0OyxEeV54H8g-ykosLBDAd87zRXQLckaYj8vRxV1XVV207PmFsix7AsoLC7NmMSiUttpVgwOBGU61ncQ7p4fvPaG4fYxPD6IISRtyf5yOBkoLcOTSmuAvXuHlc5dSF44dxNdPZbgWUhiUvAjZs7w2mRNG19URMfwGRJYEhC-q-3XwTT3Qb7zJpcS3d1ad58OmTRJL9bzPdNQZW4Li_GRACAs3E3DLD695klKT0joOxsnbIvK9-c2JtXBKucNArhdYw1BOX2DVQ035GpBpOfqbiVKFpMKfOKprEL5KHPc328n-ghr5jsqUeyHRaR5qxpwlsJQPp3y1c62EE4dqHvTo7cDIvaoVWmOqaISPPnivz647VAzOIGRrU36cP96p85bko1YqUBthisV6X8zD0JVVLp9W3lWx_YHLRHv3DFr0u73zlSF-wQR3uyAnZ12-iuSuISoe9GrTUFYX79AdUiE7W81_9srFdCsaUzA604HlGPnPKU3mmXPlwKFKfJs4-i8NAVi-Nxw6gjihKp9LvV8Dvbm9Yaf80aNWaBpk2zMX4j0mdc9gHesu7q-ivpGsHnIlxpP_ES1sov6ak6_pXgwrvtyri_g-BWc5v52FF19luFQ74a5CCJrmxTB3HJuSnMqIguoBNURvfp-w9gSDSIEbrPkLeX06HJ-ex7rENTKPmlKxFkjNwvTTgKJekQeLuoRLBKHBdE43omIPq-Oo8pBWuzJyMa8Rm_U3L3Oef07N3e7DWffNXy3SZyd1gJUBJPwjrjNvkzFtwuZL_aQS-2fylWi7K_QFQQMcsF7qKJIQLCv2cTMB_wrMrhxKKSBw2oyFgVycVc2tuEfNC60K7fQC1ugLh1Z3KbVg-Pp4qp7pYEXpSgSZGx6xUTuWfAZnOxlVmZFbB6VgP-UT2etaDtt5dkRJDlLAuLRkR8GKon1crs2ffIgaJn2pa0IA9NbG_BFYd-4X1h_C9rkG5Wn194aeiE9Ups69Nk_Dc_yTdvVE5U7vtvXLexuKaW7qWx-TunRh_9xm0uam8azsXOB9K1Xp8zi_yeu9hbDHuA0N83oe587EMeSE10yaMxo-RvounELLnk2URYRT7Lc2cmRiEE5U9NS5ahYwHxp7ycXMpfp1mnw-JO0_Nyl-FMXibjOnRmvpfnSiQKdgOGFTU-uVOSTcnXTxM9W4mtBunzkUf3n33vjTKn-i-ZNDWEo-T5sqHxYGGGB8r_0L-XmBdAVkDUit6CVpOiqD-cMM7JkUXkpTkBwvn8NNBBsC9QzUNAuMKcbhxDgC9wC2mthqsCaqs6jXdeSGZWPpCqesGMyEBS6Z710gKHyoX1kjFdxk3LUnRjE9RKxcSxy4MoW3U0-HfxDlxMrlY7paWBEt7WKdFNGV9iBYVasOX4nZ3yU974CB3vM03Xz4WIq_RjO1ehfEctue3NcQcGT35KDI3SlJyKqz6XQrpSBKLbNPygLH0t2f5zELGy-ynFR0O3G2-0fmfBX6SXi-1k0LmLtBaVqBwaR8D2buXXJOp8vCHbVMTqXMLPJp0grbG4RvsueuvnSZip2DqF3YAW9gbuH5xgXL7mrXlKAy8luluAQSfOVWc2EYFu45b8yTuohtU7evRE20xTJEqDjyLJhozNVAnLbXT2pmMtBD67e2TL84DCwSzfi-IYQNahtGxDqjB0JUSdnka6fXF1GcJt44vTkFkBxG0LsKtXDpZrz19u9BTn2sEeZ4g4CzW21czlYa2q5sTj0rtX9l0l1jZHa_p1_DSL-h0e65lxqf5PphD9eNIjG-CAefkW2mzdOTVfAu6Lk0MZ9QTIwdIMXnKkD7e88ABgcQkq43-9kRdv3sEDRvU2V2BtBW2t2KpudqZBKrWATvANFObdq-JYj8krPYH3m0RdRpg0pRTdPFk53EVE7Lo2rkmzmcLEOB7jPzotkCNis081gBKFzY6ixOWyO5nwrfXxKTq8kUZUtYiNOkugWU310oGIoVEEOlihCfBnbnzK6svoOUES4cB_y6v8yRIa-iD_8jmJZU5-kNEDJ5S3aRcRQ-Y9TAlUteNRIW6N6e5OCafzXwQ6-4n7bU2K474OS95qbZnsqa_StOdrQ8JMa0VzhEsIuD7TeZlPA9NAX51xm9eqnQWkI5jNEhP9Gosd46k1PcuW06bkCXvCGROrjA9Z7_NEUjWjjgX1MeOB5_kAdOXXycHFlOOUtArHz9gMmpR_7ClqPyuJpPMUWFRShf23ZFPRcww6Zmvs90Bc34BR-2hFXKntYCje-5cSm0IubQNsskmubRJVPIiusFsVfvMtlYUX67H_3MjPwknnIrgJM4czsF1XImycb6-VIJ63PQaWlC0R1GAG1MoOvmdVJsPrTvPuSKlZHUG4rZKinxFJ_Q9vUrSRlWMWHhY-jhDoZaRRoNTXxKyfOVaiOL3ybkz_yb0mPLfdEVJ6E2um_G1hsaW69NgCRVu5BEB3GIoFYDQwt1hqSzh4UZh7dXWmyKNxmC5Hv72xcBvrKaNaw9aVIySmQnptJM5RO8sRdWwplu_YA3Io-BhmOPMSiPoo9MX2b4DsWONDJYYGCAatkzUZgknKNNp-TntE9y7gR203dFwRxULbZq8PFcGJgY14TLEw1Kr9qs7wivlF_H3KNcc2AxNQHIDGRGqfu05CO6qUe9MZgTPhdnffAqgqFKwOwPF-bm6sbchP28M47njoMuTSLSUpYYZbKIr826VsYc52GytWe3zxowgTnhb0u02gz_S7PzNo-VDlDnhdzpPyiKP6MJ5fSpeNJ7NLf3T1D-1WKaWJd-x2r5-a-IMeXuZ0iMrpBF5yYH8JcL_IsYRYC9OvJXLgKiF_NbSyxWmbxm7geDT8CLmGjUPWvGkXSExgMXKrlb948tJUqI2G2ySV5iQkD1XUZbOoXkfm_MIk4uW1DGwxyjq4qn15Sr5qGbWKITn7r9i9ngrJzZZCRMd59EAtnvUyLkebOJJpX8XrU7-x8DuBi3a75Yqgl_9R95jsN0GDybtg2US4G6TSdehEaDLmuWxsptTu0gYHlxIqUEEirb6Kpf5df669FNG8QCLLGiKhagQHOQem-LOHBWncAcl2D8XMKbbT_M0IGbmxraRVbITe-QHdVFWGLLS0KVeSXFRzGlOyJkXYCt3Mkllbd12WMNnoNekIIM2EeYaBWuGXjH3Ot3aPHpYJE-zhAAQaa0DLCYLVghI6QV4yHIqmJ8JlYfp6OsB_pnAqCUcEqsrIwJbpugRz7V5wixS_B4cdoHPLisKU9B0TXJQTagg_Z_0n1qNx-HPFe5g_OpjOGzP2FYsHjFPWWRUCga7uzvnKtWfckNbB3GHuHa9Ii-Il8n6ZMU-9dtqmsMdfH-W0Ju__qB6gXcwp31NEcia070fImIzJ5s6iAfccf6mE1h7UIDSJcNSHY6KbcDNXnsMm9dG-IsQ01hZFG1iOLWSJq8OAauruuv_YZTP9t8e5ETacdxqXxCXdRdGHy0rNPGWLBjJr8KbbQXTwHsmqT1qWJuRKk7QcEcE9oB0GNqRN8Yv2TGIolKiELVrfam5hX8mSS39kVRChOCZdwMJbHEoJ6DUq0x5niDEA6EUSu_4zL0wh_nZ5K1AE51hMntHhAWcMboB8G2rC0RA4Ikf5xID_D3DuWJ75SpFqeHTAujXePybTaPivU4A2YtsjWYaohTXytioGgD0smd3wcUrWcy0Xl_428Yr98uPAbs6Q_UG-d7lk0sZ8SB_8gIL8pQhMwVLewomWAB_SWK8oq-qKV_KjofIlGpYUci2TYBvxDlt1qcwx8KEPtiHI70nQC9Mo-jAoNUuyvte9HFq2irW7uLY9IZFeHhS1DPQFQ0wxpOga_9ADrQMrVrT1nJT8Fj5M4F6kZzcEPCdXXU78VzOleMQqVgx9oziU8UQLjFCV-_Tf_Z3FSXsUvR5pxuIzmujzQ22epP9aKiLeyQDkMDviOlEWk_ZPDIhsxFICGHwHbWw2fRm0UPNJnR23_E7aoxcLD8CCgG2djtkk6dYmHsTurBU5-oA1f6T7MWKjWgibY4gNNxXKEYpC2TLUxAckWp70yVFPTveMDLOxuv6qseGm_gYrDx2ehEBqGsQoQloNxkwawZmbvuGigPlBTT3WDWZGXjfV_uXWRMKqWWMACsCJbKyswoebEL_m747fsicogsGt05rGbJWoAYT3EPO0K4uJWf1jPjwJM-EKMnFOZI2A2brznDRH6hIE2jG7m8V7JWxnESibanKFJTwvZdFHzVB65Q1mDrpNUuJLPTpV8-OzdpteGrbHCo7Dpemtotm9ReV4dZ0U6uR2B7pE_OKu-bkkn2PGyxK1RQhh_Lh4OCCx48ugBjqsCe996TF-zBSQGfKbTlHdr-ri-QujcNgCR3Km6RPRmwUeFRlUDEhtBX4DMtWTr-mdY6qNnl8PrqspIUEzQ92UvRdzoSdxT_ZLqYAmZoC75wtQKNnmOKLd9kDGydw7Cl0s0IgjINTYJRnC63EZhzyhacy4Ggdk6RG7sdKPkZMc4kgv_B-oQKI8QowjWzGEVfJRjwE6Kchzx2KhSAZwmLydcuEyoj64ZeNbKlvzL1Y6OtmMkJ327tyi-DlDW5OBW78_AwaMxrgZRcjCJBOTtiNWy9Efvq89befPiqg8nqaIrJYq5XrTJXNCTJxTLrkgnQHc0lud4CGcM0_RNtuSa1-F8gUWd6cVKcvmJywSJsSfRvz4YhnZJ0ROfUcQmA7PEBLXTR6luY2dIgMR0geyXte9e1U_k6b1BM38hl5ws2iY1oBhl2CUGLdQxtDSvvmO1rl9XU6ESbqCEAN56h3T5pKNki2q38KZ1ocA7prvyVyyMuWT6FURslU1PWa5YbLR0RSJPo6_O41_BZN5R9wx9D5SyXnVX1mmjPAasTzj3NlQf4e9-amvPcpmhnLpipwzHJmOoNV39bAGEdmPlcP8o2VP_ULd2RVCg1MyxclRWR1xvn1933cLg6QmlpTZ5uYqFjJEcaWw2Avq6lKPOtEj4sHeMJm0yjxvYt6Hip-4rTEhNmQ1KRkrfMotVN6lP5NIisB3mYFQ1UG2G2NuSZzSIkFw8vQP2QOxrwgOLmU1gZ4-591VclFUjhoSOWLZIvsta7-OR7OBm3vluhPY9dM9KVR6WSo9_fsMS2ycbuQur8n5UCKGku6vasagGIPU0sHXw0e7yPhoDGYhyUnMZ_LAwGODE06Iy_oX9JCilhorKqbvOpdhV7WIwtuEgeUZ66k8HOnGoHaqC3mm5I4TRkWvaKDlASAQcE6CsuH9b3hhCEQRxT6-DpzDmtC7nxZRsmvlxSD3SF9xsKUtZ6VyUaQG7fCwV_JNlX9wO9BH9u29_zLsP1x9UOlCKLoausAg5_uvxIzEik3dKki6MdY1enqndIYlxcab1znmWAZ3rwjVu8TNdpAorzApNlmchoOPo0GtM.CYMgGIQUc3enFZo2ynSNhA"
        val client = com.deutrust.deutrust.infrastructure.eudi.EudiClient(
            com.fasterxml.jackson.databind.ObjectMapper(),
            ""
        )
        val decrypted = client.decryptJwe(jwe)
        println("=== REAL DECRYPTED JWS ===")
        println(decrypted)
        println("==========================")
    }

    @Test
    fun verifyKeyMatch() {
        try {
            val client = com.deutrust.deutrust.infrastructure.eudi.EudiClient(
                com.fasterxml.jackson.databind.ObjectMapper(),
                ""
            )
            val privateKey = client.decryptJwe("dummy") // triggers loadPrivateKey
        } catch (e: Exception) {
            // we just need loadPrivateKey and cert matching
        }
        try {
            val certFile = File("keys/certificate.pem")
            if (!certFile.exists()) {
                println("certificate.pem does not exist")
                return
            }
            val privateKeyFile = File("keys/private_key.pem")
            if (!privateKeyFile.exists()) {
                println("private_key.pem does not exist")
                return
            }
            val cf = java.security.cert.CertificateFactory.getInstance("X.509")
            val cert = cf.generateCertificate(certFile.inputStream())
            val publicKey = cert.publicKey as java.security.interfaces.ECPublicKey

            val pem = privateKeyFile.readText()
            val cleanPem = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")
            val decoded = Base64.getDecoder().decode(cleanPem)
            val spec = java.security.spec.PKCS8EncodedKeySpec(decoded)
            val kf = java.security.KeyFactory.getInstance("EC")
            val privateKey = kf.generatePrivate(spec) as java.security.interfaces.ECPrivateKey

            // Verify if public key matches private key by signing and verifying
            val challenge = "challenge-string-123".toByteArray()
            val sig = java.security.Signature.getInstance("SHA256withECDSA")
            sig.initSign(privateKey)
            sig.update(challenge)
            val signature = sig.sign()

            val verifier = java.security.Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(challenge)
            val matches = verifier.verify(signature)

            println("==================================================")
            println("=== KEY PAIR MATCH VERIFICATION ===")
            println("Does certificate.pem match private_key.pem? $matches")
            println("==================================================")
            org.junit.jupiter.api.Assertions.assertTrue(matches, "Verifier Key Pair Mismatch: private_key.pem does not match public key in certificate.pem")
        } catch (e: Exception) {
            e.printStackTrace()
            org.junit.jupiter.api.Assertions.fail("Exception during key match verification: ${e.message}", e)
        }
    }
}
