package com.being1943.enoviamcp;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
/** Conservative command allowlist. ENOVIA ACLs remain the actual security boundary. */
public final class MqlSafety {
 private static final Set<String> PREFIXES=Set.of("print bus","temp query bus","expand bus","print connection","query connection","list type","print type","list relationship","print relationship","list attribute","print attribute","list policy","print policy","list vault","print vault","help");
 private static final Pattern CONTROL=Pattern.compile("[\\r\\n\\u0000]");
 private MqlSafety(){}
 public static String validate(String input){
  if(input==null||input.isBlank())throw new IllegalArgumentException("MQL command is empty.");
  String c=input.trim();if(c.length()>4000)throw new IllegalArgumentException("MQL command exceeds 4000 characters.");
  if(CONTROL.matcher(c).find())throw new IllegalArgumentException("Multi-line MQL is blocked.");
  if(c.indexOf(';')>=0&&!c.endsWith(";"))throw new IllegalArgumentException("Only one MQL statement is allowed.");
  if(c.endsWith(";"))c=c.substring(0,c.length()-1).trim();String l=c.toLowerCase(Locale.ROOT);
  if(l.contains("--")||l.contains("/*")||l.contains("*/")||l.contains("//"))throw new IllegalArgumentException("MQL comments are blocked.");
  if(l.matches("(?s).*\\b(add|modify|delete|remove|connect|disconnect|promote|demote|revise|clone|copy|checkout|checkin|lock|unlock|execute|trigger|set|unset|transaction|commit|abort|rollback|import|export|purge|kill|start|resume|freeze|thaw)\\b.*"))throw new IllegalArgumentException("Mutating or administrative MQL is blocked in read-only mode.");
  if(PREFIXES.stream().noneMatch(p->l.equals(p)||l.startsWith(p+" ")))throw new IllegalArgumentException("MQL command is not in the read-only allowlist.");
  return c+";";
 }
 public static String token(String v,String label){if(v==null||!v.matches("[A-Za-z0-9_.*?@ -]{1,128}"))throw new IllegalArgumentException("Invalid "+label+"; allowed: letters, digits, spaces, _, -, ., *, ?, @.");return v;}
 public static String quoted(String v,String label){String x=token(v,label);return x.contains(" ")?"\\""+x+"\\"":x;}
}