# Matrix MQL data model notes

These notes capture the distinctions used by this MCP server, based on the supplied **ENOVIA Collaboration Platform Modeling Studio 10.8 MQL Guide**. That guide is dated 2007 and is a conceptual reference, not a substitute for checking the target R2023x/R2024x command help.

## 1. Three concepts that must not be conflated

| Concept | Meaning | MQL surface |
|---|---|---|
| Business object (`bus`) | A managed object instance with type, name, revision, policy/state, owner, vault and attributes | `print bus`, `temp query bus`, `expand bus` |
| Relationship definition (`relationship`) | Schema/admin definition describing a relationship type and its rules | `list relationship`, `print relationship` |
| Connection instance (`connection`) | One actual link between a From business object and a To business object; carries relationship type, ID and relationship attributes | `print connection`, `query connection`, `expand bus` |

In particular, a runtime relationship instance is commonly surfaced as a **connection**. The relationship name identifies the schema definition used by that connection. Therefore the MCP API names the instance tools `connection_*` and schema-definition tools `schema_list_relationships`.

## 2. MQL command families used in v0.1.0

### Business objects

- `print bus TYPE NAME REVISION select ...;`
- `temp query bus TYPE NAME_PATTERN REVISION_PATTERN limit N;`
- `expand bus TYPE NAME REVISION relationship RELATIONSHIP recurse to N;`

### Connections

- `print connection CONNECTION_ID select ...;`
- `query connection relationship PATTERN limit N select ...;`

The guide describes `print connection` by connection ID and `query connection` with clauses including `to`, `from`, `relationship`, `type`, `vault`, `where`, `limit` and `orderby`.

### Schema definitions

- `list type [PATTERN];`
- `list relationship [PATTERN];`
- `list attribute [PATTERN];`

The guide also documents corresponding `print` commands for administrative definitions. Detailed schema printing is intentionally not part of this first release.

## 3. Query/expand performance guardrails

The supplied guide warns against unbounded queries such as querying all types/names/revisions, and against unbounded recursive expansion. It recommends narrowing queries and using a finite recursion depth. Accordingly:

- `bus_find` defaults to 50 and caps the limit at 500.
- `connection_find` defaults to 50 and caps the limit at 500.
- `expand_bus` defaults to one level and caps recursion at eight.
- The server does not expose `recurse all`.

These caps are application guardrails, not a guarantee that every query is inexpensive. Production deployments should tune limits and query patterns to the actual schema and data volume.

## 4. Reference sections in the supplied guide

- MQL CLI invocation and command-line switches: Chapter 1, approximately pp. 46–51.
- Explicit transactions: Chapter 2, approximately pp. 75–80.
- Relationship definitions and connection instances: Chapter 17, approximately pp. 447–459.
- Business-object expansion and recursion: Chapter 42, approximately pp. 810–812.
- Query and expand performance: Chapter 45, approximately pp. 909–911.

## 5. Version caveat

The guide is MQL 10.8, not a current 3DEXPERIENCE R2023x/R2024x API contract. Validate command syntax, selectables, permissions and output behavior against the installed MQL client and the target database before relying on these tools.