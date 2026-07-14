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

## Comandos

| Comando | Descrição | Permissão |
|---|---|---|
| `/balance [jogador]` (alias `/bal`, `/saldo`) | Mostra seu saldo, ou o de outro jogador | `economyshop.balance.others` p/ ver de outros |
| `/pay <jogador> <valor>` | Transfere dinheiro para outro jogador online | — |
| `/shop` (alias `/loja`) | Abre a loja em uma GUI | — |
| `/ecoadmin <give\|take\|set> <jogador> <valor>` | Administra o saldo de um jogador | `economyshop.admin` (padrão: OP) |
| `/tag list` | Lista as tags disponíveis | — |
| `/tag set <jogador> <tag>` | Define a tag de chat de um jogador | `economyshop.tag.admin` (padrão: OP) |

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

Cada entrada em `tags` define uma tag disponível, com texto de exibição e cor. Adicionar uma nova tag é só adicionar uma nova entrada — sem precisar recompilar nada.

## Arquitetura

```
EconomyShopPlugin        → classe principal (onEnable/onDisable)
economy/EconomyManager   → acesso ao SQLite, cache thread-safe (ConcurrentHashMap)
shop/ShopManager         → carrega itens do config.yml, monta o Inventory da loja
shop/ShopHolder          → InventoryHolder para identificar a GUI da loja nos eventos
tag/TagManager           → tags configuráveis por config.yml + atribuição por jogador persistida em playertags.yml
commands/                → BalanceCommand, PayCommand, ShopCommand, EcoAdminCommand, TagCommand
listeners/                → PlayerDataListener (carrega/salva saldo), ShopListener (compra/venda),
                            ChatFormatListener (tag colorida + hover com saldo + clique pra /msg)
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
