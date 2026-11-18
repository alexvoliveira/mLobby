# Build e dependências do mLobby

Este guia corresponde ao `build.gradle.kts` e ao workflow `.github/workflows/build.yml` da branch de refatoração.

## 1. Requisitos

- JDK 8 (o artefato é compilado para Java 8).
- Git.
- Gradle Wrapper versionado no repositório.
- API/NMS do Spigot 1.8.8 gerada e instalada em `mavenLocal()` para builds locais.
- Um JAR compatível do mCore em `libs/mCore.jar`.

O mLobby é um plugin legado. A compatibilidade real depende das APIs específicas do Spigot 1.8.8 e do mCore utilizado no servidor.

## 2. Relação com mCore

O build declara `libs/mCore.jar` como dependência `compileOnly`. O arquivo é necessário para compilar as referências a classes de perfil, hotbar, menus, NPCs, hologramas e conexão. Ele não é incluído no JAR final do mLobby.

Antes de atualizar o JAR local, compile/obtenha a versão de mCore que será usada no servidor e confirme que ela contém as classes e assinaturas esperadas. A pasta `libs/` atualmente contém esse artefato para permitir o build no workflow.

O mLobby também usa APIs compile-only do Spigot 1.8.8, ProtocolLib 4.5.0 e PlaceholderAPI 2.10.5. Em execução, essas dependências precisam estar presentes no servidor; o mCore declara ProtocolLib e PlaceholderAPI como dependências.

## 3. Build local

Na raiz do projeto:

```bash
./gradlew --no-daemon clean build
```

O Gradle verifica a presença de `libs/mCore.jar`. Se não encontrar o arquivo, o build falha antes de compilar.

O artefato esperado é `build/libs/mLobby.jar`.

No Windows, use `gradlew.bat --no-daemon clean build`.

## 4. Como o workflow valida o projeto

O workflow atual executa checkout do mLobby, configuração do Temurin JDK 8, validação de que `libs/mCore.jar` existe e não está vazio, geração das APIs Spigot 1.8.8 pelo BuildTools action, `./gradlew --no-daemon clean build), verificação do JAR gerado e upload do artefato de CI por 14 dias.

O workflow não clona o repositório mCore durante a execução nem usa `MCORE_REPO_TOKEN`. O contrato de compilação vem do JAR já disponibilizado em `libs/mCore.jar`.

## 5. Instalação no servidor

Instale separadamente mCore, mLobby, ProtocolLib 4.5.0 e PlaceholderAPI 2.10.5. Use a versão de mCore compatível com o JAR utilizado para compilar o mLobby. Reinicie o servidor após substituir os plugins. O mLobby declara `depend: [mCore]` no `plugin.yml`, portanto o Bukkit deve carregar mCore antes dele.

## 6. Diagnóstico de build

| Erro | Causa provável | Ação |
| --- | --- | --- |
| `Required mCore library was not found` | `libs/mCore.jar` ausente | Adicione o artefato compatível nesse caminho |
| Spigot 1.8.8 não resolvido | API/NMS não instalada | Execute BuildTools para 1.8.8 e confira `mavenLocal()` |
| Classe do mCore não encontrada | JAR incompatível ou antigo | Compare a API usada pelo código com o JAR |
| Falha de ProtocolLib/PlaceholderAPI | Dependência não resolvida no build ou ausente no servidor | Confirme versões 4.5.0 e 2.10.5 |
| Erro em NMS ou pacote | Compilação com API de versão diferente | Gere/instale artefatos Spigot 1.8.8 compatíveis |

