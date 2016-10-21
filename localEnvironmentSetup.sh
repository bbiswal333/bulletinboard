# Used for backing services like the PostgreSQL database
export VCAP_APPLICATION={}
export VCAP_SERVICES='{"postgresql-9.3":[{"name":"postgresql-lite","label":"postgresql-9.3","credentials":{"dbname":"test","hostname":"127.0.0.1","password":"test123!","port":"5432","uri":"postgres://testuser:test123!@localhost:5432/test","username":"testuser"},"tags":["relational","postgresql"],"plan":"free"}],"rabbitmq-lite":[{"credentials":{"hostname":"127.0.0.1","password":"guest","uri":"amqp://guest:guest@127.0.0.1:5672","username":"guest"},"label":"rabbitmq-lite","tags":["rabbitmq33","rabbitmq","amqp"]}],"xsuaa":[{"credentials":{"clientid":"testClient!27","clientsecret":"dummy-clientsecret","identityzone":"d012345trial","identityzoneid":"d012345trial","tenantid":"d012345trial","tenantmode":"shared","url":"dummy-url","verificationkey":"-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn5dYHyD/nn/Pl+/W8jNGWHDaNItXqPuEk/hiozcPF+9l3qEgpRZrMx5ya7UjGdvihidGFQ9+efgaaqCLbk+bBsbU5L4WoJK+/t1mgWCiKI0koaAGDsztZsd3Anz4LEi2+NVNdupRq0ScHzweEKzqaa/LgtBi5WwyA5DaD33gbytG9hdFJvggzIN9+DSverHSAtqGUHhwHSU4/mL36xSReyqiKDiVyhf/y6V6eiE0USubTEGaWVUANIteiC+8Ags5UF22QoqMo3ttKnEyFTHpGCXSn+AEO0WMLK1pPavAjPaOyf4cVX8b/PzHsfBPDMK/kNKNEaU5lAXo8dLUbRYquQIDAQAB-----END PUBLIC KEY-----","xsappname":"bulletinboard-d012345"},"label":"xsuaa","name":"uaa-bulletinboard","plan":"application","tags":["xsuaa"]}]}'

# Used for dependent service call
export USER_ROUTE=https://bulletinboard-users-course.cfapps.sap.hana.ondemand.com

# Overwrite logging library defaults
export APPENDER=STDOUT
export LOG_APP_LEVEL=ALL
