# ENOVIA MQL MCP Server

**Version:** 0.1.0 · **Transport:** MCP stdio · **Access:** read-only

This standalone project adds a local MCP server to the existing `being1943/EnoviaRestService` repository without changing that repository's existing Spring Boot application. It invokes the locally installed ENOVIA MQL command-line client; Dassault Systèmes binaries and JARs are not redistributed.

## Included tools

| MCP tool | Behavior |
|---|---|
| `enovia_connection_test` | Runs `list type;` to verify MQL startup/connectivity |
| `mql_execute` | Executes one statement from a conservative read-only allowlist |
| `bus_get` | `print bus ... select id type name revision current state owner vault policy;` |
| `bus_find` | `temp query bus ... limit N;` |
| `expand_bus` | Expands a bus object through one relationship type |

The tool taxonomy starts from Matrix primitives (MQL adapter, `bus`, and relationship/connection expansion), not from business modules such as EBOM or MBOM.

## Requirements

- Windows with an installed ENOVIA MQL client and a working local login/bootstrap configuration.
- Java 17 or later.
- Maven 3.9 or later.

## Build

From this directory:

```powershell
mvn clean package
```

Executable JAR:

```
target/enovia-mql-mcp-0.1.0.jar
```

## Configure

```powershell
$env:MQL_EXECUTABLE = "C:\DassaultSystemes\R2023x\...\mql.exe"
# Optional: alternate Matrix bootstrap file, resolved by the MQL client
$env:MQL_BOOTFILE = "matrix-r"
$env:MQL_TIMEOUT_SECONDS = "30"
```

The MQL Guide 10.8 provided with this task documents the command-line `-c`, `-stdout:`, `-stderr:` and `-b` options. The server uses these options to execute one command and capture output without shell interpolation. That PDF is from 2007; confirm CLI compatibility with the exact installed R2023x/R2024x client before using it outside a development environment.

Authentication is intentionally delegated to the locally configured MQL client/bootstrap. Do not commit credentials, bootstrap files, or vendor libraries.

## Codex configuration

```toml
[mcp_servers.enovia]
command = "java"
args = ["-jar", "C:\\tools\\enovia-mql-mcp-0.1.0.jar"]
startup_timeout_sec = 30

[mcp_servers.enovia.env]
MQL_EXECUTABLE = "C:\\DassaultSystemes\\R2023x\\...\\mql.exe"
MQL_BOOTFILE = "matrix-r"
MQL_TIMEOUT_SECONDS = "30"
```

For a local build, point `args` to `target\\enovia-mql-mcp-0.1.0.jar`.

## Security boundaries

- The server only exposes read-oriented tools in this version.
- `mql_execute` rejects multiline input, statement chaining, comments, and commands outside its allowlist.
- Common write/admin verbs (add, modify, delete, promote, demote, execute, trigger, transaction, import/export, etc.) are blocked.
- The ENOVIA account's own ACLs and licenses remain authoritative.
- This filter is defense-in-depth, not a complete MQL parser. Use a least-privilege account and a non-production database first.
- stdio is intended for a local MCP client; no network listener is opened.

## Known limitations

- Real ENOVIA integration has not been exercised from this build environment because it requires the user's installed MQL client, bootstrap and database.
- The exact command syntax and selectables should be verified on the target release.
- Relationship-instance read/write, reverse expansion, schema introspection, attribute APIs, lifecycle, vault, JPO and business-specific BOM tools are not yet implemented.
- MQL output is returned as text; structured parsing is a later increment.

## Next version

1. Validate the five tools against the user's R2023x/R2024x system.
2. Add `connection_get`, `connection_find`, and bidirectional expand.
3. Add read-only schema introspection: type, relationship, attribute, policy.
4. Add structured output and pagination.
5. Consider a Matrix Java API adapter after confirming the supported JAR set and authentication model.

## License

MIT. Independent integration project; not affiliated with Dassault Systèmes.