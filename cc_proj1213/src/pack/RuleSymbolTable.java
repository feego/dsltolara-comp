package pack;

import java.util.ArrayList;

public class RuleSymbolTable {
	private ArrayList<String> typeDefs = new ArrayList<String>();
	private ArrayList<String> setTypes = new ArrayList<String>();
	private ArrayList<String> sets = new ArrayList<String>();
	private ArrayList<String> usedSets = new ArrayList<String>();
	
	private ArrayList< ArrayList < ArrayList <String>>> forEachsDeclared = new ArrayList< ArrayList < ArrayList <String>>>();
	private ArrayList< ArrayList < ArrayList <String>>> forEachsUsed = new ArrayList< ArrayList < ArrayList <String>>>();
	
	public ArrayList<String> getUsedSets() {
		return usedSets;
	}
	
	public ArrayList<String> getSetTypes() {
		return setTypes;
	}
	
	public ArrayList< ArrayList < ArrayList <String>>> getForEachsUsedVars() {
		return forEachsUsed;
	}
	
	public void addTypeDef(String typedef) {
		typeDefs.add(typedef);
	}
	
	public boolean containsTypeDef(String typedef) {
		return typeDefs.contains(typedef);
	}
	
	public void addSetType(String settype) {
		setTypes.add(settype);
	}
	
	public boolean containsSetType(String settype) {
		return setTypes.contains(settype);
	}

	public boolean containsSet(String set) {
		return sets.contains(set);
	}

	public void addSet(String set) {
		sets.add(set);		
	}
	
	public void addForEachDeclared(ArrayList < ArrayList <String>> forInfo) {
		forEachsDeclared.add(forInfo);
	}
	
	public void addForEachUsed(ArrayList < ArrayList <String>> forInfo) {
		forEachsUsed.add(forInfo);
	}
	
	public boolean containsForDeclared(int forIndex, String var) {
		for (ArrayList<String> s : forEachsDeclared.get(forIndex)) {
			if (s.contains(var)) return true;
		} return false;
	}

	public void addUsedSet(String set) {
		usedSets.add(set);		
	}
}
