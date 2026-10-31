package com.being1943.enoviamcp;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Calls the locally installed MQL CLI; vendor binaries/JARs are never copied into this repository. */
public final class MqlCliClient {
 public String execute(String raw) throws IOException,InterruptedException {
  String exe=System.getenv("MQL_EXECUTABLE");
  if(exe==null||exe.isBlank())throw new IllegalStateException("Set MQL_EXECUTABLE to the full path of mql.exe.");
  String command=MqlSafety.validate(raw);
  int timeout=30; try{timeout=Integer.parseInt(System.getenv().getOrDefault("MQL_TIMEOUT_SECONDS","30"));}catch(Exception ignored){}
  timeout=Math.max(3,Math.min(300,timeout));
  Path dir=Files.createTempDirectory("enovia-mql-mcp-"),out=dir.resolve("out.txt"),err=dir.resolve("err.txt");
  try{
   List<String>a=new ArrayList<>();a.add(exe);
   String boot=System.getenv("MQL_BOOTFILE");if(boot!=null&&!boot.isBlank()){a.add("-b");a.add(boot);}
   a.add("-k");a.add("-t");a.add("-stdout:"+out);a.add("-stderr:"+err);a.add("-c");a.add(command);
   Process p=new ProcessBuilder(a).start();
   if(!p.waitFor(timeout,TimeUnit.SECONDS)){p.destroyForcibly();throw new IOException("MQL timed out after "+timeout+" seconds.");}
   String stdout=Files.exists(out)?Files.readString(out,StandardCharsets.UTF_8):"";
   String stderr=Files.exists(err)?Files.readString(err,StandardCharsets.UTF_8):"";
   if(p.exitValue()!=0||!stderr.isBlank())throw new IOException("MQL failed (exit "+p.exitValue()+"): "+stderr+(stdout.isBlank()?"":"\n"+stdout));
   return stdout.isBlank()?"(MQL completed; no output)":stdout;
  }finally{Files.deleteIfExists(out);Files.deleteIfExists(err);Files.deleteIfExists(dir);}
 }
}