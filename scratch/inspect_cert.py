from cryptography import x509
from cryptography.hazmat.primitives.asymmetric import ec
import base64

with open("keys/certificate.pem", "rb") as f:
    cert_data = f.read()

cert = x509.load_pem_x509_certificate(cert_data)
pub_key = cert.public_key()

if isinstance(pub_key, ec.EllipticCurvePublicKey):
    numbers = pub_key.public_numbers()
    x = numbers.x
    y = numbers.y
    
    # Convert to base64url
    def to_b64url(val):
        bytes_val = val.to_bytes((val.bit_length() + 7) // 8, byteorder='big')
        return base64.urlsafe_b64encode(bytes_val).decode('utf-8').rstrip('=')
        
    print("Certificate EC Public Key:")
    print("x:", to_b64url(x))
    print("y:", to_b64url(y))
else:
    print("Not an EC Public Key")
