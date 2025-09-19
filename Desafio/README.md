Sistema de Gerenciamento de Acesso
Funcionalidades
Gerenciamento de Usuários

Cadastro de usuários com nome, senha e tipo (administrador/comum)
Listagem de usuários cadastrados
Validação de credenciais para acesso

Controle de Acesso

Liberação de porta 1 mediante autenticação
Liberação de porta 2 mediante autenticação
Controle de GPIO via arquivos (/tmp/gpio1, /tmp/gpio2)

Sistema de Eventos

Registro de todos os eventos do sistema
Acesso aos logs restrito a administradores
Armazenamento em arquivo (/tmp/events.txt)

Comunicação

Protocolo Modbus RTU implementado
Address: 0x01, Porta 1: 0x34, Porta 2: 0x35
Transmissão TCP/IP para servidor (127.0.0.1:502)
Envio automático de dados de cadastro e eventos

Interface

Menu interativo via console
Opções numeradas para navegação
Validação de entrada e tratamento de erros

Armazenamento

Dados de usuários em /tmp/users.txt
Log de eventos em /tmp/events.txt
Persistência de dados entre execuções

O sistema implementa todos os requisitos especificados para controle de acesso corporativo com comunicação Modbus RTU.
