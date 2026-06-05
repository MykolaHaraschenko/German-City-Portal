import json

jwk = {
  "kty": "EC",
  "crv": "P-256",
  "x": "NtAy7fUBZzKL9VKA2f9FnOsVYGMhzE1TYzgZ2J5Fwh4",
  "y": "hjLA1-4Z2PsGK4FPVP5-0ahVbB8w-CjTo9jzrsTMja8",
  "use": "sig",
  "alg": "ES256",
  "kid": "test-key-1"
}

print(json.dumps({"keys": [jwk]}, indent=2))
