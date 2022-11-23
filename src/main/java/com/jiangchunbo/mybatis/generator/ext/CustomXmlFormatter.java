package com.jiangchunbo.mybatis.generator.ext;

import org.mybatis.generator.api.dom.DefaultXmlFormatter;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.api.dom.xml.render.AttributeRenderer;
import org.mybatis.generator.api.dom.xml.render.DocTypeRenderer;
import org.mybatis.generator.internal.util.CustomCollectors;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomXmlFormatter extends DefaultXmlFormatter {

    /**
     * XML 缩进多少个空格
     */
    public static final int INTENT_SPACE = 4;

    @Override
    public String getFormattedContent(Document document) {
        return Stream.of(renderXmlHeader(),
                        renderDocType(document),
                        renderRootElement(document))
                .flatMap(Function.identity())
                .collect(Collectors.joining(System.getProperty("line.separator")));
    }

    private Stream<String> renderXmlHeader() {
        return Stream.of("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
    }

    private Stream<String> renderDocType(Document document) {
        return Stream.of("<!DOCTYPE " //$NON-NLS-1$
                + document.getRootElement().getName()
                + document.getDocType().map(this::renderDocType).orElse("") //$NON-NLS-1$
                + ">"); //$NON-NLS-1$
    }

    private String renderDocType(DocType docType) {
        return " " + docType.accept(new DocTypeRenderer()); //$NON-NLS-1$
    }

    private Stream<String> renderRootElement(Document document) {
        return document.getRootElement().accept(new ElementVisitor<Stream<String>>() {
            private final AttributeRenderer attributeRenderer = new AttributeRenderer();

            @Override
            public Stream<String> visit(TextElement element) {
                return Stream.of(element.getContent());
            }

            @Override
            public Stream<String> visit(XmlElement element) {
                if (element.hasChildren()) {
                    return renderWithChildren(element);
                } else {
                    return renderWithoutChildren(element);
                }
            }

            private Stream<String> renderWithoutChildren(XmlElement element) {
                return Stream.of("<" //$NON-NLS-1$
                        + element.getName()
                        + renderAttributes(element)
                        + " />"); //$NON-NLS-1$
            }

            public Stream<String> renderWithChildren(XmlElement element) {
                return Stream.of(renderOpen(element), renderChildren(element), renderClose(element))
                        .flatMap(s -> s);
            }

            private String renderAttributes(XmlElement element) {
                return element.getAttributes().stream()
                        .sorted(Comparator.comparing(Attribute::getName))
                        .map(attributeRenderer::render)
                        .collect(CustomCollectors.joining(" ", " ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            private Stream<String> renderOpen(XmlElement element) {
                return Stream.of("<" //$NON-NLS-1$
                        + element.getName()
                        + renderAttributes(element)
                        + ">"); //$NON-NLS-1$
            }

            private Stream<String> renderChildren(XmlElement element) {
                return element.getElements().stream()
                        .flatMap(this::renderChild)
                        .map(this::indent);
            }

            private Stream<String> renderChild(VisitableElement child) {
                return child.accept(this);
            }

            private String indent(String s) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < INTENT_SPACE; i++) {
                    stringBuilder.append(" ");
                }
                return stringBuilder + s; //$NON-NLS-1$
            }

            private Stream<String> renderClose(XmlElement element) {
                return Stream.of("</" //$NON-NLS-1$
                        + element.getName()
                        + ">"); //$NON-NLS-1$
            }
        });
    }
}
