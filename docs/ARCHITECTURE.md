# Arquitetura do mLobby

Este documento descreve o fluxo implementado no código da branch de refatoração do mLobby. Ele complementa o README e concentra a responsabilidade de cada módulo, a integração com mCore e o ciclo de vida dos recursos.

## 1. Posição do mLobby na rede

O mLobby é um plugin Bukkit/Spigot instalado nos servidores que atuam como lobby. Ele não substitui o proxy BungeeCord e não mantém seu próprio sistema de perfis, fila, persistência de jogadores ou API de menus global.

O mCore é a camada compartilhada da rede. O mLobby consome serviços que já existem nele, como perfil e hotbar do jogador, API de NPC e hologramas, classes de menu e utilitários Bukkit, placeholders e scoreboard, contagem/balanceamento de servidores e transferência do jogador para outro servidor.

Essa divisão reduz duplicação: recursos transversais ficam no mCore e as regras específicas de lobby permanecem neste repositório.

## 2. Inicialização e encerramento

A classe `lobby.Main` estende `MPlugin` e concentra o ciclo de vida do plugin.

### Inicialização

No `start()`, a instância estática é registrada. No `enable()`, a aplicação:

1. Obtém `Core.getInstance()` e confere se o objeto existe, se o plugin está habilitado e se `Core.validInit` está verdadeiro.
2. Se o mCore não estiver pronto, registra uma mensagem de erro e desabilita o mLobby. Isso evita inicializar menus e listeners que dependem de serviços não disponíveis.
3. Carrega `config.yml`, define o nome do lobby atual e aplica o spawn configurado, quando informado.
4. Carrega idioma, hotbars e scoreboard por meio de `LCoreHook`.
5. Inicializa os servidores do seletor e suas tarefas de consulta.
6. Carrega NPCs de jogo e de entrega.
7. Registra comandos e listeners.

A ordem é importante: os componentes do mLobby acessam configurações e APIs disponibilizadas pelo mCore. Por isso, não devem ser iniciados antes da validação da dependência.

### Encerramento

No `disable()`, os NPCs de jogo são destruídos e o estado visual de tags é limpo quando a inicialização foi concluída. O método também aplica o fluxo legado de atualização por arquivo caso exista `plugins/mLobby/update/mLobby.jar`. Esse mecanismo não valida assinatura criptográfica do JAR; restrinja a escrita nessa pasta e só coloque nela artefatos produzidos e verificados pelo processo de release da equipe.

## 3. Seleção de servidores

Os servidores são definidos em `lobbies.yml`. Cada item de `items` contém:

| Campo | Significado |
| --- | --- |
| `slot` | Posição do item no inventário |
| `icon` | Item serializado e texto com placeholders locais |
| `max-players` | Limite usado para impedir seleção de servidor cheio |
| `servername` | Endereço `host:porta`, separador ` ; ` e nome do servidor BungeeCord |

A classe `Lobby.setupLobbies()` valida o formato do endereço e a porta (1–65535). Registros incompletos ou inválidos são ignorados e adicionados à lista de avisos. O nome BungeeCord não é derivado do endereço: é uma configuração separada e precisa coincidir com a configuração do proxy.

Para cada lobby, `LobbyEntryTask` executa consultas periódicas em uma tarefa assíncrona. `Lobby.fetch()` atualiza o objeto `ServerPing` e guarda a contagem em `ServerItem.SERVER_COUNT`. O intervalo vem de `refresh-interval-ticks` e possui um mínimo de 20 ticks.

O menu é implementado por `MenuLobbies`, que usa a API de inventário empacotada no mCore. Ele preenche os slots, substitui `{players}`, `{maxplayers}` e `{description}` no ícone e destaca o servidor atual.

Ao clicar, o slot é associado à instância de `Lobby`; o próprio servidor e os servidores no limite de jogadores não são selecionáveis; o perfil do jogador é recuperado; e a transferência é solicitada por `Core.sendServer(profile, serverName)`.

O mLobby não envia diretamente a mensagem de conexão ao BungeeCord. O mCore controla a fila, salva o perfil e realiza a transferência.

## 4. NPCs

O sistema possui dois fluxos separados.

### NPC de jogo

`PlayNPC` usa `ServerEntry` para associar localização, skin, item de mão e linhas do holograma a uma entrada de servidor/jogo. A biblioteca de NPC cria uma entidade de jogador falsa; a biblioteca de hologramas exibe textos próximos ao NPC. A população apresentada no holograma vem do balanceador associado à entrada.

As localizações são serializadas e mantidas em `npcs.yml`. Na inicialização, o plugin carrega as entradas, reconstrói os NPCs e agenda a atualização das linhas.

### NPC de entregas

`DeliveryNPC` cria o NPC e o holograma para a área de entregas. As linhas e a skin vêm do idioma carregado pelo mLobby. Sua localização e identificador também são salvos em `npcs.yml`.

Ambos os fluxos carregam o chunk da localização antes de gerar a entidade. No desligamento, os NPCs de jogo são destruídos pelo ciclo de vida principal; os serviços de NPC/holograma pertencem ao mCore.

## 5. Hotbar e scoreboard

`LCoreHook.setupHook()` faz a integração entre os módulos: registra o tipo de ação `lobby` na hotbar do mCore, lê `hotbar.yml` e cria os botões definidos para o lobby, carrega `scoreboard.yml`, agenda a rolagem dos títulos e a atualização dos scoreboards dos jogadores que não estão em uma partida e registra o adaptador de pacotes de hologramas.

As linhas de scoreboard passam pelo PlaceholderAPI antes de serem entregues ao scoreboard do jogador. Portanto, placeholders externos dependem de expansões instaladas e funcionais no servidor.

Os intervalos são ticks do Bukkit: 20 ticks correspondem nominalmente a 1 segundo. Use valores adequados à quantidade de jogadores e ao custo dos placeholders; atualizar em todo tick aumenta trabalho no thread principal.

## 6. Comandos e eventos

`LobbyCommand` registra o comando `ml` e o alias `mlobby`. A permissão geral é `mlobby.cmd.lobby`. Os subcomandos são classes individuais, incluindo operações de spawn, construção, criação/remoção de NPCs, entregas e atualização.

Os listeners ficam organizados por domínio: jogador (entrada, saída, interação, morte, inventário e chat), entidade (eventos relacionados às entidades do lobby) e servidor (eventos de ciclo de vida e estado do servidor).

## 7. Build e dependência binária

O Gradle compila contra `libs/mCore.jar` usando `compileOnly`. O JAR do mCore não é empacotado dentro de `mLobby.jar`. Em produção, ambos devem ser instalados separadamente, e o mCore do servidor deve fornecer as mesmas assinaturas usadas durante a compilação.

O workflow atual faz checkout do mLobby, prepara Java 8, verifica o JAR de mCore em `libs/`, gera a API/NMS do Spigot 1.8.8, executa `./gradlew clean build`, verifica o artefato e o publica como artefato do workflow.

Uma mudança na API do mCore exige atualizar deliberadamente o JAR de compilação do mLobby e validar os dois projetos juntos.

## 8. Configurações e diagnóstico

As configurações em `src/main/resources` são valores padrão copiados para `plugins/mLobby/` em uma instalação nova. Alterações nos recursos versionados não sobrescrevem configurações existentes.

| Sintoma | Verificação |
| --- | --- |
| mLobby desativa durante o startup | Confirme que mCore iniciou e que `Core.validInit` está verdadeiro |
| Seletor vazio | Verifique `lobbies.yml`, formato dos endereços e entradas válidas |
| Contagem desatualizada | Confira conectividade, porta de status e `refresh-interval-ticks` |
| Clique não transfere | Verifique o nome BungeeCord em `servername`, o perfil e a fila do mCore |
| Placeholder literal | Confirme a expansão correspondente no PlaceholderAPI |
| Scoreboard não atualiza | Confira `scoreboard.yml`, ticks e erros dos placeholders |
| NPC não aparece | Verifique mundo/chunk/localização, `npcs.yml` e inicialização das bibliotecas do mCore |

