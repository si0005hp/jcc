package jcc;

import java.util.List;
import java.util.stream.Collectors;

import jcc.ast.ExprNode;
import jcc.ast.IntLiteralNode;
import jcc.type.IntegerType;

public final class ParseUtils {

	public static List<ExprNode> strToIntLiteralNodes(String s) {
		List<ExprNode> elems = s.chars()
				.mapToObj(c -> new IntLiteralNode(IntegerType.of(CType.CHAR), c))
				.collect(Collectors.toList());
		elems.add(new IntLiteralNode(IntegerType.of(CType.CHAR), 0));
		return elems;
	}

}
