# EconomyShop

Plugin de economia para servidores Paper/Spigot, com saldo persistente em banco de dados e uma loja navegável por GUI.

## Funcionalidades

- **Economia por jogador** com saldo inicial configurável, persistido em **SQLite** (arquivo `economy.db` na pasta do plugin).
- Cache em memória sincronizado de forma assíncrona no join/quit do jogador (evita travar a thread principal do servidor em operações de banco de dados).
- **Loja via GUI** (`/shop`): inventário clicável onde cada item tem preço de compra (clique esquerdo) e venda (clique direito), tudo definido em `config.yml`.
- Transferência de dinheiro entre jogadores (`/pay`).
- Comandos administrativos para dar, tirar ou definir saldo de qualquer jogador.
- **Chat personalizado com tags coloridas**: cada jogador tem uma tag configurável (ex: `[VIP]`, `[DONO]`), cada uma com sua própria cor, exibida antes do nome no chat.
- Passar o mouse no nome de um jogador no chat mostra o saldo dele; clicar no nome já prepara um `/msg` pronto pra digitar.
- **Badges de verdade no chat**: as 5 tags padrão (`membro`, `vip`, `mod`, `admin`, `dono`) são renderizadas como um selinho colorido de imagem (fundo + texto), não só texto colorido — via um **resource pack customizado** que o próprio plugin gera, hospeda e envia automaticamente pro jogador ao entrar.

## Comandos

| Comando | Descrição | Permissão |
|---|---|---|
| `/balance [jogador]` (alias `/bal`, `/saldo`) | Mostra seu saldo, ou o de outro jogador | `economyshop.balance.others` p/ ver de outros |
| `/pay <jogador> <valor>` | Transfere dinheiro para outro jogador online | — |
| `/shop` (alias `/loja`) | Abre a loja em uma GUI | — |
| `/ecoadmin <give\|take\|set> <jogador> <valor>` | Administra o saldo de um jogador | `economyshop.admin` (padrão: OP) |
| `/tag list` | Lista as tags disponíveis | — |
| `/tag set <jogador> <tag>` | Define a tag de chat de um jogador | `economyshop.tag.admin` (padrão: OP) |
| `/tag create <id> <cor> <texto>` | Cria uma nova tag em tempo real (sem precisar editar `config.yml` ou reiniciar) | `economyshop.tag.admin` (padrão: OP) |
| `/tag menu` | Abre uma GUI com a lista de tags, prévia e paleta de cores pra editar cada uma | `economyshop.tag.admin` (padrão: OP) |

## Configuração (`config.yml`)

```yaml
starting-balance: 100.0

default-tag: membro

tags:
  membro:
    display: "[Membro]"
    color: gray
  vip:
    display: "[VIP]"
    color: gold
  dono:
    display: "[DONO]"
    color: light_purple

shop-items:
  pao:
    material: BREAD
    name: "Pão"
    buy-price: 5.0
    sell-price: 2.0
```

Cada entrada em `shop-items` vira um item clicável na loja. `material` é o nome de um `Material` do Bukkit; `buy-price`/`sell-price` podem ser omitidos ou zerados para desabilitar compra ou venda daquele item.

Cada entrada em `tags` define uma tag disponível, com texto de exibição e cor. Além de editar o `config.yml`, também dá pra criar tags direto em jogo com `/tag create <id> <cor> <texto>` — elas ficam salvas em `tags.yml` e persistem entre reinícios.

## Badges de chat (resource pack)

O chat do Minecraft só suporta cor de texto — não dá pra desenhar um retângulo colorido atrás de uma palavra sem imagem. Pra ter um badge "de verdade" como em servidores com identidade visual própria, o plugin:

1. Já vem com um **resource pack pronto** embutido no jar (`resourcepack/pack.zip`), com uma imagem por tag padrão (fundo colorido + texto da tag desenhado nela) mapeada como um caractere customizado via `assets/minecraft/font/badges.json`.
2. No `onEnable`, extrai esse pack pra pasta de dados do plugin, calcula o hash SHA-1 dele, e sobe um **servidor HTTP embutido** (via `com.sun.net.httpserver.HttpServer`, sem dependências externas) pra servir o arquivo.
3. Quando um jogador entra, o plugin manda um `ResourcePackRequest` (API da Adventure) oferecendo o download — **não é obrigatório**, quem recusar simplesmente vê a tag em texto colorido normal em vez do badge de imagem.
4. No chat, cada tag com badge usa um `Component` com `.font(Key.key("minecraft:badges"))` isolado (só o glyph do badge usa essa fonte customizada; o resto da mensagem continua na fonte padrão do jogo).

Tags criadas por `/tag create` não têm imagem pronta — elas caem automaticamente no visual em texto colorido, que funciona pra qualquer jogador sem precisar baixar nada.

Configurável em `config.yml`:

```yaml
resource-pack:
  enabled: true
  port: 34567
  public-url: ""   # em branco = http://127.0.0.1:<port>/..., só funciona localmente.
                    # Em produção, aponte pro IP/domínio público do servidor.
```

## Editor de tags via GUI (`/tag menu`)

Pra quem é OP, `/tag menu` abre uma interface de duas telas:

1. **Lista de tags**: um item por tag (cor do item = cor da tag, nome = prévia da tag na cor real, lore avisa se ela tem badge de imagem ou só texto). Clicar em uma abre a tela de edição.
2. **Editor da tag**: um item de prévia no topo + uma **paleta de 16 cores** (uma lã de cada `NamedTextColor` do Minecraft) — o mais próximo de uma "roda de cores" que dá pra fazer dentro de um inventário do jogo, já que não existe um seletor de cor livre nativo no Minecraft.

Ao clicar numa cor:
- A cor da tag é salva na hora.
- Se a tag tiver badge de imagem (as 5 padrão), o plugin **regenera a textura do badge com o novo fundo** (usando `Graphics2D`, o mesmo motor que gerou as imagens originais), reempacota o resource pack, recalcula o hash e **reenvia automaticamente pra todo mundo online** — ninguém precisa relogar pra ver a mudança.
- A tela se atualiza na hora com a nova prévia.

Como não existe entrada de texto livre dentro de um inventário do Minecraft (sem usar uma bigorna), criar uma tag nova (`id`, cor inicial, texto) continua sendo por `/tag create` — o menu foca em editar/visualizar as que já existem.

## Arquitetura

```
EconomyShopPlugin        → classe principal (onEnable/onDisable)
economy/EconomyManager   → acesso ao SQLite, cache thread-safe (ConcurrentHashMap)
shop/ShopManager         → carrega itens do config.yml, monta o Inventory da loja
shop/ShopHolder          → InventoryHolder para identificar a GUI da loja nos eventos
tag/TagManager           → tags configuráveis por config.yml + atribuição por jogador persistida em playertags.yml
chatpack/ResourcePackServer → extrai/reempacota o resource pack, calcula o hash SHA-1, serve via HTTP embutido
chatpack/BadgeRenderer   → desenha a imagem de um badge (fundo + texto) via Graphics2D
gui/TagGuiManager        → monta os inventários de lista e edição de tags
gui/ColorPalette         → paleta de 16 NamedTextColor mapeados pra lãs coloridas
gui/TagListHolder, TagEditHolder → InventoryHolder pra identificar cada GUI nos eventos
commands/                → BalanceCommand, PayCommand, ShopCommand, EcoAdminCommand, TagCommand
listeners/                → PlayerDataListener (carrega/salva saldo), ShopListener (compra/venda),
                            ChatFormatListener (badge/tag + hover com saldo + clique pra /msg),
                            ResourcePackJoinListener (oferece o resource pack ao entrar),
                            TagGuiListener (cliques no editor de tags)
```

## Stack técnica

- **Paper API 26.1.2** (Minecraft 26.1.2)
- **SQLite JDBC** (`org.xerial:sqlite-jdbc`) — resolvido em runtime pelo próprio Paper via a seção `libraries:` do `plugin.yml`, sem necessidade de shade/relocate do driver dentro do jar.
- Java 21, Maven

## Build

```
mvn clean package
```

O jar final fica em `target/EconomyShop.jar`. Basta colocar na pasta `plugins/` do servidor.
