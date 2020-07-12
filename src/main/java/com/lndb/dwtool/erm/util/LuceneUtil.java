package com.lndb.dwtool.erm.util;

import org.apache.lucene.analysis.StopAnalyzer;

public class LuceneUtil {
	public static final String prepareSearchTerm(String searchTerm) {
		if (searchTerm == null) {
			return "";
		}
		String cleanedTerm = handleSpecialChars(searchTerm);
		String[] terms = cleanedTerm.split(" ");
		String formtted = "";
		for (String term : terms) {
			if (term == null || term.trim().length() == 0 || StopAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term.toLowerCase())) {
				continue;
			}
			if (formtted.equals("")) {
				formtted = "+" + term + "* ";
			} else {
				formtted = formtted + " +" + term + "*";
			}
		}
		return formtted.trim();
	}

	/**
	 * @param searchTerm
	 * @return
	 */
	public static final String handleSpecialChars(String searchTerm) {
		int pos = 0;
		char[] tmp = new char[searchTerm.length() * 2];
		char[] chars = searchTerm.toCharArray();
		for (char c : chars) {
			if (c == ',' || c == '/' || c == '`' || c == '@' || c == '#' || c == '$' || c == '%' || c == '_' || c == '=' || c == ';' || c == '<' || c == '>') {
				// ignore
				continue;
			}
			if (c == '+' || c == '-' || c == '&' || c == '|' || c == '!' || c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == '^' || c == '"' || c == '~' || c == '*'
					|| c == '?' || c == ':' || c == '\\' || c == '.') {
				tmp[pos++] = '\\';
			}
			tmp[pos++] = c;
		}
		String cleanedTerm = new String(tmp, 0, pos);
		return cleanedTerm;
	}

	public static final String stripSpecialChars(String searchTerm) {
		int pos = 0;
		char[] tmp = new char[searchTerm.length() * 2];
		char[] chars = searchTerm.toCharArray();
		for (char c : chars) {
			if (c == ',' || c == '/' || c == '`' || c == '@' || c == '#' || c == '$' || c == '%' || c == '_' || c == '=' || c == ';' || c == '<' || c == '>' || c == '+' || c == '-' || c == '&'
					|| c == '|' || c == '!' || c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == '^' || c == '"' || c == '~' || c == '*' || c == '?' || c == ':'
					|| c == '\\' || c == '.') {
				// ignore
				continue;
			}
			tmp[pos++] = c;
		}
		String cleanedTerm = new String(tmp, 0, pos);
		return cleanedTerm;
	}
}
