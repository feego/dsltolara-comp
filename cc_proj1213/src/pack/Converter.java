package pack;

import java.util.ArrayList;
import java.util.HashMap;

public class Converter {
	private boolean fixedDeclared = false;
	private String fixedDeclaration = new String();
	private HashMap<String, ArrayList<String>> tempVars = new HashMap<String, ArrayList<String>>();

	public String doConversion(SimpleNode node) {
		return doConversion(node, new ArrayList<String>());
	}

	public String doConversion(SimpleNode node, ArrayList<String> selectors) {
		String code = "";
		boolean ruleDeclaration = false;

		switch (node.toString()) {
		case "ForEachStatement":
			return getForEachInfo(node, selectors);
		case "Rule":
			fixedDeclared = false;
			ruleDeclaration = true;
			code += "aspectdef " + node.val + "\n";
			break;
		case "Use":
			if (node.val != null)
				return "call " + node.val + ";\n";
			break;
		case "SetFixed":
			ArrayList<String> lhs = nodeChildrenToStringArray((SimpleNode) node
					.jjtGetChild(0));
			ArrayList<String> rhs = nodeChildrenToStringArray((SimpleNode) node
					.jjtGetChild(1));

			fixedDeclared = true;
			fixedDeclaration = "{" + lhs.get(0).toLowerCase() + "="
					+ lhs.get(1).toLowerCase() + ", "
					+ rhs.get(0).toLowerCase() + "=" + rhs.get(1).toLowerCase()
					+ "}";
			return "var fixed = \"" + fixedDeclaration + "\";\n";
		case "TypeDef":
			SimpleNode child = (SimpleNode) node.jjtGetChild(0);
			ArrayList<String> params = nodeChildrenToStringArray(child);
			return "var "
					+ node.val
					+ " = \""
					+ ((child.jjtGetNumChildren() > 0) ? ((params.get(0)
							.equals("1")) ? "fixed" : "ufixed")
							+ "("
							+ params.get(1) + ", " + params.get(2) + ")"
							: (child.toString().toLowerCase())) + "\""
					+ ((fixedDeclared) ? " + fixed" : "") + ";\n";
		case "Set":
			code += "def ";

			params = nodeChildrenToStringArray(node);
			child = (SimpleNode) node.jjtGetChild(0);
			if (child.val != null) {
				if (!params.get(0).equals("type")) {
					tempVars.put(params.get(0),
							nodeChildrenToStringArray((SimpleNode) node
									.jjtGetChild(1)));
				}
			}
			return "";
		case "WithStatement":
			String newSelector = parseWithSelector(node);
			if (newSelector != null)
				selectors.add(newSelector);

			code = doConversion((SimpleNode) node.jjtGetChild(1), selectors);
			if (selectors.size() > 0)
				selectors.remove(selectors.size() - 1);

			return code;
		case "Do":
			String body = processBody(node, "");

			if (!body.equals("")) {
				code += "select ";
				code += concatSelectors(selectors);
				code += " end\napply\n";
				code += body;
				code += "end\n";
			}
			break;
		}

		for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
			SimpleNode n = (SimpleNode) node.jjtGetChild(i);
			if (n != null) {
				code += doConversion(n, selectors);
			}
		}

		if (code != "")
			if (ruleDeclaration) {
				tempVars.clear();
				return code + "end\n\n";
			} else
				return code;
		else
			return "";
	}

	private String concatSelectors(ArrayList<String> selectors) {
		String code = "";
		if (selectors.size() > 0)
			code += selectors.get(0);
		for (int i = 1; i < selectors.size(); i++)
			code += "." + selectors.get(i);
		return code;
	}

	private String parseWithSelector(SimpleNode node) {
		SimpleNode firstChild = (SimpleNode) node.jjtGetChild(0);

		switch (firstChild.toString()) {
		case "Statement":
			return "statement{'" + firstChild.val + "'}";
		case "Var":
			String selector = "var{";
			ArrayList<String> vararray = nodeChildrenToStringArray(firstChild);

			if (vararray.size() > 0)
				selector += "'" + vararray.get(0) + "'";
			for (int i = 0; i < vararray.size() - 1; i++) {
				selector += ",'" + vararray.get(i) + "'";
			}
			selector += "}";
			return selector;
		case "Function":
			return "function{name==\"" + firstChild.val + "\"}";
		}
		return null;
	}

	public ArrayList<String> nodeChildrenToStringArray(SimpleNode node) {
		ArrayList<String> array = new ArrayList<String>();

		for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
			SimpleNode n = (SimpleNode) node.jjtGetChild(i);
			if (n != null) {
				if (n.val != null)
					array.add(n.val);
				else
					array.add(n.toString());
			}
		}
		return array;
	}

	public String getForEachInfo(SimpleNode node, ArrayList<String> selectors) {
		ArrayList<String> iter = nodeChildrenToStringArray((SimpleNode) node
				.jjtGetChild(0));
		SimpleNode iterListNode = (SimpleNode) node.jjtGetChild(1);

		if (iterListNode.val != null) {
			ArrayList<String> list = tempVars.get(iterListNode.val);

			if (list != null) {
				return processForEachList(iter, list,
						(SimpleNode) node.jjtGetChild(2), selectors);
			}
		} else {
			SimpleNode iterId = (SimpleNode) iterListNode.jjtGetChild(0);

			if (iterId.toString() == "Program"
					|| iterId.toString() == "Function") {
				ArrayList<String> equalsList = new ArrayList<String>();
				ArrayList<String> colonsList = new ArrayList<String>();
				ArrayList<String> list = new ArrayList<String>();
				for (int i = 1; i < iterListNode.jjtGetNumChildren(); i++) {
					SimpleNode param = (SimpleNode) iterListNode.jjtGetChild(i);
					switch (param.toString()) {
					case "Equals":
						equalsList = nodeChildrenToStringArray(param);
						break;
					case "Colons":
						colonsList = nodeChildrenToStringArray(param);
						break;
					case "List":
						list = nodeChildrenToStringArray(param);
						break;
					}
				}

				equalsList.add(0, "");
				colonsList.add(0, "");
				list.add(0, "");
				return processForEachProgFunc(iterId.toString(), iter,
						equalsList, colonsList, list,
						(SimpleNode) node.jjtGetChild(2), selectors);
			} else if (iterId.toString() == "List") {
				return processForEachList(iter,
						nodeChildrenToStringArray(iterId),
						(SimpleNode) node.jjtGetChild(2), selectors);
			}
		}
		return "";
	}

	private String processForEachProgFunc(String selector,
			ArrayList<String> iter, ArrayList<String> equalsList,
			ArrayList<String> colonsList, ArrayList<String> list,
			SimpleNode body, ArrayList<String> selectors) {
		String code = "";
		colonsList.addAll(list);

		for (String colons : colonsList) {
			for (String equals : equalsList) {
				if ((colons.equals("") && colonsList.size() > 1)
						|| equals.equals("") && equalsList.size() > 1)
					continue;
				
				code += "select ";
				String selectorsStr = concatSelectors(selectors);
				if (selectorsStr.length() > 0)
					code += selectorsStr + ".";

				switch (iter.get(0)) {
				case "Key":
					code += ((selector.equals("Function")) ? (selector.toLowerCase() + processElem(equals)
							+ ".") : ("")) + processKey(colons) + " end\napply\n"
							+ processBody(body, colons) + "end\n\n";
					break;
				case "Tag":					
					code += ((selector.equals("Function")) ? (selector.toLowerCase() + processElem(equals)
							+ ".") : ("")) + "section" + processElem(colons) + " end\napply\n"
							+ processBody(body, colons) + "end\n\n";
					break;
				case "Var":
					code += ((selector.equals("Function")) ? (selector.toLowerCase() + processElem(equals)
							+ ".") : ("")) + "var" + processElem(colons) + " end\napply\n"
							+ processBody(body, colons) + "end\n\n";
					break;
				}
			}
		}
		return code;
	}

	private String processElem(String elem) {
		return (elem == "") ? "" : "{'" + elem + "'}";
	}

	private String processForEachList(ArrayList<String> iter,
			ArrayList<String> iterlist, SimpleNode body,
			ArrayList<String> selectors) {
		String code = "";
		String selectorsStr = concatSelectors(selectors);

		for (String elem : iterlist) {
			code += "select ";
			if (selectorsStr.length() > 0)
				code += selectorsStr + ".";
			
			switch (iter.get(0)) {
			case "Key":
				code += processKey(elem) + " end\napply\n"
						+ processBody(body, processInnerKey(elem)) + "end\n\n";
				break;
			case "Tag":
				
				code += "section{'" + elem + "'} end\napply\n"
						+ processBody(body, processInnerKey(elem)) + "end\n\n";
				break;
			case "Var":
				code += "var{'" + elem + "'} end\napply\n"
						+ processBody(body, "") + "end\n\n";
				break;
			}
		}
		return code;
	}

	private String processKey(String elem) {
		switch (elem) {
		case "for":
			return "loop{'for'}";
		case "while":
			return "loop{'while'}";
		case "elsif":
			return "elseif";
		default:
			return elem.toLowerCase();
		}
	}

	private String processInnerKey(String elem) {
		if (elem.equals("for") || elem.equals("while"))
			return "loop";
		else if (elem.equals("elsif"))
			return "elseif";
		else
			return elem.toLowerCase();
	}

	private String processBody(SimpleNode body, String elem) {
		String code = "";

		for (int i = 0; i < body.jjtGetNumChildren(); i++) {
			SimpleNode n = (SimpleNode) body.jjtGetChild(i);

			switch (n.toString()) {
			case "Insert":
				ArrayList<String> args = nodeChildrenToStringArray((SimpleNode) n
						.jjtGetChild(0));
				code += "insert "
						+ args.get(0).toLowerCase()
						+ " %{"
						+ processInsertBody((SimpleNode) n.jjtGetChild(1), elem)
						+ "}%;\n";
				break;
			case "Set":
				code += "def ";

				ArrayList<String> params = nodeChildrenToStringArray(n);
				if (params.get(1).equals("Rhs"))
					params.set(
							1,
							"\""
									+ ((SimpleNode) ((SimpleNode) n
											.jjtGetChild(1)).jjtGetChild(0))
											.toString().toLowerCase() + "\"");
				System.out.println(params);
				SimpleNode child = (SimpleNode) n.jjtGetChild(0);

				if (child.val != null)
					if (params.get(0).equals("type")) {
						return code + "type = " + params.get(1) + ";\n";
					}
				break;
			case "Decompose":
				child = (SimpleNode) n.jjtGetChild(0);
				code += "decompose(" + child.val + ");\n";
				break;
			}
		}
		return code;
	}

	private String processInsertBody(SimpleNode body, String iter) {
		String code = "";
		SimpleNode n = (SimpleNode) body.jjtGetChild(0);

		switch (n.toString()) {
		case "Assignment":
			code = processParams((SimpleNode) n.jjtGetChild(0), iter) + " = "
					+ processParams((SimpleNode) n.jjtGetChild(1), iter) + ";";
			break;
		default:
			code = processInsertingLine(n, iter);
			break;
		}
		return code;
	}

	private String processInsertingLine(SimpleNode node, String iter) {
		String code;
		if (node.val != null)
			code = node.val.toLowerCase() + "(";
		else
			code = node.toString().toLowerCase() + "(";

		if (node.jjtGetNumChildren() > 0) {
			for (int i = 0; i < node.jjtGetNumChildren() - 1; i++) {
				SimpleNode n = (SimpleNode) node.jjtGetChild(i);

				code += processCallArgs(n, iter) + ", ";
			}
			SimpleNode n = (SimpleNode) node.jjtGetChild(node
					.jjtGetNumChildren() - 1);
			code += processCallArgs(n, iter);
		}
		return code + ");";
	}

	private String processCallArgs(SimpleNode n, String iter) {
		if (n.jjtGetNumChildren() > 0 && n.toString() != "Arg") {
			if (n.toString().equals("File")) {
				return "file: "
						+ parseQuotedArg(((SimpleNode) n.jjtGetChild(0)).val,
								iter);
			}
			return "";
		} else {
			switch (n.toString()) {
			case "Arg":
				if (n.jjtGetNumChildren() == 0)
					return parseQuotedArg(n.val, iter);
				else {
					SimpleNode child = (SimpleNode) n.jjtGetChild(0);
					String code = processParams(child, iter);

					if (n.jjtGetNumChildren() > 1) {
						child = (SimpleNode) n.jjtGetChild(1);
						code += ": " + parseQuotedArg(child.val, iter);
					}
					return code;
				}
			default:
				return n.toString();
			}
		}
	}

	private String parseQuotedArg(String val, String iter) {
		if (iter.equals("")) {
			iter = "var.name";
		}
		return val.replace("<var>", "[[$" + iter + "]]").replace("<function>",
				"[[$function.name]]");
	}

	private String processParams(SimpleNode n, String elem) {
		String code = "";
		switch (n.toString()) {
		case "Param":
			if (n.val != null)
				code = n.val;
			ArrayList<String> params = nodeChildrenToStringArray(n);
			switch (params.get(0)) {
			case "Identifier":
				code += params.get(0);
				break;
			case "Key":
				String iter = elem;
				if (iter.equals(""))
					iter = "var.name";

				if (params.size() > 1)
					code += "[[$" + iter + "." + params.get(1) + "]]";
				else
					code += "[[$" + iter + "]]";
				break;
			case "Var":
				iter = elem;
				if (iter.equals(""))
					iter = "var.name";

				if (params.size() > 1)
					code += "[[$" + iter + "." + params.get(1) + "]]";
				else
					code += "[[$" + iter + "]]";
				break;
			default:
				code += elem;
				break;
			}
			break;
		case "Number":
			code = n.val;
			break;
		case "Op":
			code = processParams((SimpleNode) n.jjtGetChild(0), elem) + n.val
					+ processParams((SimpleNode) n.jjtGetChild(1), elem);
			break;
		}
		return code;
	}
}
