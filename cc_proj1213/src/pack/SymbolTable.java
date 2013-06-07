package pack;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
	private ArrayList<String> applys = new ArrayList<String>();
	private ArrayList<String> uses = new ArrayList<String>();
	private HashMap<String, RuleSymbolTable> rules = new HashMap<String, RuleSymbolTable>();
	
	public ArrayList<String> getApplyList() {
		return applys;
	}
	
	public ArrayList<String> getUseList() {
		return uses;
	}
	
	public HashMap<String, RuleSymbolTable> getRules() {
		return rules;
	}
	
	public void addApplyRule(String rule) {
		applys.add(rule);
	}
	
	public void addRule(String rule) {
		rules.put(rule, new RuleSymbolTable());
	}
	
	public void addUse(String use) {
		uses.add(use);
	}
	
	public boolean containsUse(String use) {
		return uses.contains(use);
	}
	
	public void addSet(String rule, String set) {
		rules.get(rule).addSet(set);
	}
	
	public void addUsedSet(String rule, String set) {
		rules.get(rule).addUsedSet(set);
	}
	
	public boolean containsSet(String rule, String set) {
		return rules.get(rule).containsSet(set);
	}
	
	public boolean containsRule(String rule) {
		return rules.containsKey(rule);
	}
	
	public void addTypeDef(String rule, String typedef) {
		rules.get(rule).addTypeDef(typedef);
	}
	
	public boolean containsTypeDef(String rule, String typedef) {
		return rules.get(rule).containsTypeDef(typedef);
	}
	
	public void addSetType(String rule, String settype) {
		rules.get(rule).addSetType(settype);
	}
	
	public boolean containsSetType(String rule, String settype) {
		return rules.get(rule).containsSetType(settype);
	}
	
	public void addForEachsDeclared(String rule, ArrayList<ArrayList<String>> forInfo) {
		rules.get(rule).addForEachDeclared(forInfo);
	}
	
	public void addForEachsUsed(String rule, ArrayList<ArrayList<String>> forInfo) {
		rules.get(rule).addForEachUsed(forInfo);
	}
	
	public boolean containsForDeclared(String rule, int forIndex, String var) {
		return rules.get(rule).containsForDeclared(forIndex, var);
	}
}
