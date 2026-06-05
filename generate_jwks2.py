import json
from cryptography.hazmat.primitives import serialization

with open("keys/public_key.pem", "rb") as f:
    key = serialization.load_pem_public_key(f.read())
    
pn = key.public_numbers()
import base64
def b64(i):
    return base64.urlsafe_b64encode(i.to_bytes(32, 'big')).decode('utf-8').rstrip('=')

jwk = {
  "kty": "EC",
  "crv": "P-256",
  "x": b64(pn.x),
  "y": b64(pn.y),
  "use": "sig",
  "alg": "ES256",
  "kid": "test-key-1"
}

print(json.dumps({"keys": [jwk]}, indent=2))
