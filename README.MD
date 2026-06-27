## Como roda essa joça

pra rodar esse treco, clona os dois repos (esse (backend) e o frontend pra mesma pasta)
aí move o docker-compose.yml pra essa pasta root

roda `docker compose up --build`

e tá pronto o sorvetinho (obviamente tem que ter o docker ou o podman com compatibilidade docker instalado e aberto)

o frontend por padrão acessa por http://localhost:8081/

### banco de dados

pra conectar no banco de dados por padrao, acessa o DB (MySQL) com um DBeaver da vida com essas credenciais:

- Host: localhost
- Port: 3306
- User: root
- Password: password
- Database: desweb

### usuários 

os usuários padrão ADMIN e USER são seedados assim:

- admin@mail.com / desweb → ADMIN
- user@mail.com / desweb → USER