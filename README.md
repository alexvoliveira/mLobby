# mLobby

Plugin de lobby para servidores Minecraft baseados em Spigot 1.8.8. O mLobby complementa o [mCore](https://github.com/alexvoliveira/mCore): usa os perfis, hotbars, NPCs, hologramas, placeholders e a comunicação com o proxy fornecidos pelo Core, enquanto concentra as funções específicas do lobby.

Na prática, ele organiza a área de entrada da rede: configura o spawn, apresenta um seletor de servidores, mantém NPCs de jogo e de entregas, atualiza scoreboard e hotbar e oferece comandos administrativos para configurar esses elementos.

> **Compatibilidade:** o código foi preparado para a API legada do Spigot 1.8.8 e depende da inicialização correta do mCore. Não é um plugin independente nem uma implementação genérica para versões modernas do Minecraft.

## Funcionalidades

### Seletor de lobbies
O menu é montado a partir de `lobbies.yml`. Cada item representa um servidor e define o slot, o ícone, o limite de jogadores, o endereço usado para consultar o status e o nome registrado no BungeeCord.

O mLobby consulta os servidores configurados em segundo plano, mantém a contagem de jogadores e atualiza o inventário enquanto o jogador está com o menu aberto. O item do servidor atual recebe um destaque visual. A conexão é solicitada pelo mCore, que encaminha o jogador ao fluxo de fila e transferência da rede.

### NPCs de jogos e entregas
O plugin mantém dois tipos de NPC:

- **NPCs de jogo:** associados a uma entrada configurada em `entries.yml`. Exibem hologramas com informações do servidor e sua população, além de skin e itens de mão configuráveis.
- **NPCs de entregas:** apresentam o ponto de acesso à área de entregas do mCore e usam o texto/skin definidos no idioma do plugin.

As localizações e identificadores são persistidos em `npcs.yml`. Os NPCs e hologramas são recriados na inicialização e removidos durante o desligamento do plugin.

### Hotbar e scoreboard
A hotbar é configurada em `hotbar.yml`. Cada botão tem slot, ícone e uma ação. As ações podem chamar menus do mCore, abrir o seletor de lobbies, executar comandos ou enviar mensagens, conforme os tipos registrados pelo Core e pelo mLobby.

O scoreboard usa `scoreboard.yml`: os títulos podem ser animados e as linhas aceitam placeholders do PlaceholderAPI e placeholders registrados pelo mCore. Os intervalos são definidos em ticks.

### Comandos e configuração
O comando administrativo registrado pela aplicação é `/ml` (também aceita o alias `/mlobby`). A permissão geral é `mlobby.cmd.lobby`. Os subcomandos ficam organizados em classes próprias para tarefas como spawn, construção, NPCs, entregas e atualização do plugin.

## Como o mLobby funciona

1. O Spigot carrega o plugin por meio de `plugin.yml`, que declara a dependência obrigatória do `mCore`.
2. No método `enable()`, o mLobby verifica se o mCore existe, está habilitado e concluiu a própria inicialização. Se alguma condição falhar, o mLobby se desativa para evitar chamadas a serviços indisponíveis.
3. O plugin carrega idioma e configurações e inicializa a integração com o mCore.
4. O seletor lê a lista de servidores e agenda consultas periódicas. Os NPCs, hologramas, comandos e listeners são então registrados.
5. Durante a operação, os listeners e ações de inventário encaminham as interações para os módulos apropriados. Para trocar o jogador de servidor, o mLobby chama a API do mCore em vez de implementar outra fila de conexão.
6. Ao desabilitar, os NPCs de jogo são destruídos e os recursos registrados pelo plugin são encerrados.

A explicação detalhada está em [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Dependências e compatibilidade

| Dependência | Uso |
| --- | --- |
| Java 8 | Versão de bytecode alvo |
| Spigot 1.8.8 | API do servidor e compatibilidade com o ambiente legado |
| mCore | Perfis, APIs compartilhadas, menus, NPCs, hologramas e transferência |
| ProtocolLib 4.5.0 | Integração de pacotes usada pelo conjunto |
| PlaceholderAPI 2.10.5 | Placeholders no scoreboard e nas interfaces |
| Gradle Wrapper | Build reproduzível |

O `build.gradle.kts` usa o JAR de mCore localizado em `libs/mCore.jar` como dependência de compilação (`compileOnly`). Esse JAR não é incorporado ao artefato mLobby. Instale mCore separadamente no servidor e mantenha o JAR da pasta `libs` compatível com as APIs utilizadas pelo código.

## Compilar

É necessário ter JDK 8 e o artefato da API Spigot 1.8.8 disponível no ambiente Maven local. O workflow do GitHub Actions prepara essa dependência e executa o Gradle Wrapper.

```bash
./gradlew --no-daemon clean build
```

O arquivo gerado é:

```text
build/libs/mLobby.jar
```

Veja [docs/BUILD.md](docs/BUILD.md) para preparar o mCore local, entender as dependências e diagnosticar falhas de build.

## Configuração

Os arquivos padrão ficam em `src/main/resources` e são copiados para `plugins/mLobby/` na primeira inicialização:

| Arquivo | Responsabilidade |
| --- | --- |
| `config.yml` | Chave de ativação, nome do lobby atual e localização do spawn |
| `lobbies.yml` | Itens do seletor, endereços, nomes BungeeCord e intervalos de atualização |
| `entries.yml` | Entradas de jogos associadas aos NPCs |
| `npcs.yml` | Localizações e identificadores dos NPCs |
| `hotbar.yml` | Itens, slots e ações da hotbar |
| `scoreboard.yml` | Títulos, linhas, placeholders e intervalos |
| `language.yml` | Textos e apresentação dos NPCs de entrega |

Altere as cópias em `plugins/mLobby/` no servidor. Os arquivos em `src/main/resources` são os padrões distribuídos com o plugin, não substituem automaticamente configurações já existentes.

## Estrutura do código

```text
src/main/java/tk/slicecollections/maxteer/lobby/
├── Main.java              # Ciclo de vida e integração com mCore
├── cmd/                   # Comando /ml e subcomandos
├── hook/                  # Integração de hotbar, scoreboard e pacotes
├── listeners/             # Eventos de jogador, entidade e servidor
├── lobby/                 # Servidores, NPCs e tarefas de atualização
│   └── trait/             # Traits visuais dos NPCs
├── menus/                 # Inventário de seleção de lobbies
└── tagger/                # Utilitários de tags/equipes

src/main/resources/        # Configurações padrão do plugin
docs/                      # Documentação técnica
libs/mCore.jar             # API de compilação do mCore
```

## Documentação

- [Arquitetura e fluxo de execução](docs/ARCHITECTURE.md)
- [Build, dependências e CI](docs/BUILD.md)

## Segurança e operação

Use somente artefatos de confiança para mCore, ProtocolLib e PlaceholderAPI. Não distribua JARs de servidor ou credenciais nos arquivos de configuração do plugin. Revise permissões administrativas e endereços de servidores antes de disponibilizar o lobby aos jogadores.

O projeto é direcionado a uma stack antiga. Execute-o em uma instância isolada, mantenha cópias de segurança das configurações e teste atualizações em homologação antes de substituir os JARs de produção.

## Licença

Distribuído sob a licença MIT. Consulte [LICENSE](LICENSE).
