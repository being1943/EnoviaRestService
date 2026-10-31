package com.being1943.enoviamcp;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
/** MCP JSON-RPC stdio server. stdout is reserved for protocol messages only. */
public final class Main {
 private static final ObjectMapper J=new ObjectMapper(); private final MqlCliClient mql=new MqlCliClient();
 public static void main(String[]args)throws Exception{new Main().loop();}
 private void loop()throws Exception{
  try(BufferedReader r=new BufferedReader(new InputStreamReader(System.in,StandardCharsets.UTF_8))){
   String line;while((line=r.readLine())!=null){if(line.isBlank())continue;try{JsonNode req=J.readTree(line),res=handle(req);if(res!=null){System.out.println(J.writeValueAsString(res));System.out.flush();}}catch(Exception e){System.err.println("MCP request error: "+e.getMessage());}}
  }
 }
 private JsonNode handle(JsonNode q){
  JsonNode id=q.get("id"),p=q.path("params");String method=q.path("method").asText("");
  if(method.startsWith("notifications/"))return null;
  return switch(method){
   case "initialize"->ok(id,obj("protocolVersion","2025-06-18","capabilities",obj("tools",obj("listChanged",false)),"serverInfo",obj("name","enovia-mql-mcp","version","0.1.0")));
   case "ping"->ok(id,obj());
   case "tools/list"->ok(id,toolList());
   case "tools/call"->call(id,p);
   default->error(id,-32601,"Method not found: "+method);
  };
 }
 private JsonNode call(JsonNode id,JsonNode p){
  String n=p.path("name").asText("");JsonNode a=p.path("arguments");
  try{
   String result=switch(n){
    case "enovia_connection_test"->mql.execute("list type");
    case "mql_execute"->mql.execute(required(a,"command"));
    case "bus_get"->mql.execute("print bus "+MqlSafety.token(required(a,"type"),"type")+" "+MqlSafety.token(required(a,"name"),"name")+" "+MqlSafety.token(required(a,"revision"),"revision")+" select id type name revision current state owner vault policy");
    case "bus_find"->mql.execute("temp query bus "+MqlSafety.token(required(a,"type"),"type")+" "+MqlSafety.token(required(a,"namePattern"),"namePattern")+" "+MqlSafety.token(required(a,"revisionPattern"),"revisionPattern")+" limit "+Math.max(1,Math.min(500,a.path("limit").asInt(50))));
    case "expand_bus"->mql.execute("expand bus "+MqlSafety.token(required(a,"type"),"type")+" "+MqlSafety.token(required(a,"name"),"name")+" "+MqlSafety.token(required(a,"revision"),"revision")+" relationship "+MqlSafety.token(required(a,"relationship"),"relationship")+" recurse to "+Math.max(1,Math.min(8,a.path("levels").asInt(1)))+" terse");
    default->throw new IllegalArgumentException("Unknown tool: "+n);
   };
   return ok(id,obj("content",J.createArrayNode().add(obj("type","text","text",result)),"isError",false));
  }catch(Exception e){return ok(id,obj("content",J.createArrayNode().add(obj("type","text","text",String.valueOf(e.getMessage()))),"isError",true));}
 }
 private ObjectNode toolList(){
  ArrayNode t=J.createArrayNode();
  t.add(tool("enovia_connection_test","Check MQL startup and ENOVIA connectivity.",schema(obj(),new String[]{})));
  t.add(tool("mql_execute","Execute one allowlisted read-only MQL statement.",schema(obj("command",prop("string")),new String[]{"command"})));
  t.add(tool("bus_get","Read a business object by type, name and revision.",schema(obj("type",prop("string"),"name",prop("string"),"revision",prop("string")),new String[]{"type","name","revision"})));
  t.add(tool("bus_find","Search business objects by type/name/revision patterns.",schema(obj("type",prop("string"),"namePattern",prop("string"),"revisionPattern",prop("string"),"limit",prop("integer")),new String[]{"type","namePattern","revisionPattern"})));
  t.add(tool("expand_bus","Expand a business object through one relationship type in the to direction.",schema(obj("type",prop("string"),"name",prop("string"),"revision",prop("string"),"relationship",prop("string"),"levels",prop("integer")),new String[]{"type","name","revision","relationship"})));
  return obj("tools",t);
 }
 private ObjectNode schema(ObjectNode props,String[]required){ArrayNode r=J.createArrayNode();for(String s:required)r.add(s);return obj("type","object","properties",props,"required",r,"additionalProperties",false);}
 private ObjectNode prop(String type){return obj("type",type);}
 private ObjectNode tool(String n,String d,ObjectNode s){return obj("name",n,"description",d,"inputSchema",s);}
 private String required(JsonNode a,String k){String v=a.path(k).asText("").trim();if(v.isEmpty())throw new IllegalArgumentException("Missing argument: "+k);return v;}
 private ObjectNode ok(JsonNode id,JsonNode result){ObjectNode n=obj("jsonrpc","2.0","result",result);if(id!=null)n.set("id",id);return n;}
 private ObjectNode error(JsonNode id,int code,String msg){ObjectNode n=obj("jsonrpc","2.0","error",obj("code",code,"message",msg));if(id!=null)n.set("id",id);return n;}
 private ObjectNode obj(Object...v){ObjectNode n=J.createObjectNode();for(int i=0;i<v.length;i+=2){Object x=v[i+1];if(x instanceof JsonNode j)n.set((String)v[i],j);else n.putPOJO((String)v[i],x);}return n;}
}