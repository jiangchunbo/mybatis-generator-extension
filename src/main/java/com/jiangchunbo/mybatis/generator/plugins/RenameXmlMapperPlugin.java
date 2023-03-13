package com.jiangchunbo.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class RenameXmlMapperPlugin extends PluginAdapter {

	private final String searchString = "[A-Z][a-z]*";
	private Pattern pattern;

	public RenameXmlMapperPlugin() {
		super();
	}

	@Override
	public boolean validate(List<String> warnings) {
		pattern = Pattern.compile(searchString);
		return true;
	}

	@Override
	public void initialized(IntrospectedTable introspectedTable) {
		String oldType = introspectedTable.getMyBatis3XmlMapperFileName();
		if (oldType.endsWith("Mapper.xml")) {
			oldType = oldType.substring(0, oldType.lastIndexOf("Mapper.xml"));
		}
		if (oldType.endsWith("DO")) {
			oldType = oldType.substring(0, oldType.lastIndexOf("DO"));
		}
		Matcher matcher = pattern.matcher(oldType);
		StringBuffer stringBuffer = new StringBuffer();
		while (matcher.find()) {
			if (matcher.start() == 0) {
				matcher.appendReplacement(stringBuffer, matcher.group(0).toLowerCase());
			} else {
				matcher.appendReplacement(stringBuffer, "_" + matcher.group(0).toLowerCase());
			}
		}

		introspectedTable.setMyBatis3JavaMapperType(introspectedTable.getMyBatis3JavaMapperType()
				.substring(0, introspectedTable.getMyBatis3JavaMapperType().lastIndexOf(".") + 1).toString() + oldType
				+ "Mapper");
		stringBuffer.append("_mapping.xml");
		introspectedTable.setMyBatis3XmlMapperFileName(stringBuffer.toString());
	}
}
