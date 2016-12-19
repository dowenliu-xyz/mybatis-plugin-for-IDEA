/*
* Copyright (c) 2016 Qunar.com. All Rights Reserved.
*/
package org.qunar.plugin.mybatis.linemarker;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.qunar.plugin.mybatis.bean.mapper.Mapper;
import org.qunar.plugin.mybatis.bean.mapper.Statement;
import org.qunar.plugin.mybatis.util.DomElements;
import org.qunar.plugin.util.Icons;

import java.util.Collection;

/**
 * navigate from java interface 2 mapper xml
 *
 * Author: jianyu.lin
 * Date: 2016/11/25 Time: 下午7:08
 */
public class Java2XmlLineMarkerProvider extends AbstractMapperMakerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            Collection<? super RelatedItemLineMarkerInfo> result) {
        if (!(element instanceof PsiClass)) {
            return;
        }
        PsiClass mapperClass = (PsiClass) element;
        Collection<Mapper> mapperDomElements = DomElements.getMapperDomElements(mapperClass);

        for (Mapper mapperDom : mapperDomElements) {
            //  add interface 2 mapper
            if (mapperDom.getNamespace().getXmlAttribute() != null) {
                result.add(buildInterfaceLineMarker(mapperClass, mapperDom));
            }
            //  add method 2 statement
            result.addAll(buildMethodLineMarkers(mapperDom));
        }
    }

    /**
     * build interface line marker
     * @param mapperClass mapper class
     * @param mapperDom mapper dom element
     * @return line marker
     */
    @SuppressWarnings("ConstantConditions")
    private RelatedItemLineMarkerInfo buildInterfaceLineMarker(@NotNull PsiClass mapperClass,
                                                               @NotNull Mapper mapperDom) {
        //  marker element
        PsiElement markElement = mapperClass.getNameIdentifier() == null ? mapperClass : mapperClass.getNameIdentifier();
        //  target element
        XmlAttribute attribute = mapperDom.getNamespace().getXmlAttribute();
        PsiElement targetElement = attribute.getValueElement() == null ? attribute : attribute.getValueElement();
        //  tooltip text
        PsiFile mapperFile = mapperDom.getXmlElement().getContainingFile();
        String tooltipText = "Navigate to mapper xml" + (mapperFile == null ? "" : ": " + mapperFile.getName());
        //  build line marker
        return NavigationGutterIconBuilder.create(Icons.MYBATIS_2_XML_ICON)
                .setTarget(targetElement)
                .setTooltipText(tooltipText)
                .createLineMarkerInfo(markElement);
    }

    /**
     * {@inheritDoc}
     * @param statement sql statement
     * @return marker
     */
    @Override
    @Nullable
    protected RelatedItemLineMarkerInfo buildMethodLineMarker(@NotNull Statement statement,
                                                              @NotNull PsiMethod method) {
        @SuppressWarnings("ConstantConditions")
        XmlAttributeValue idAttrValue = statement.getXmlTag().getAttribute("id").getValueElement();
        String statementDesc = String.format("&lt;%s id=&quot;%s&quot; .../&gt;", statement.getXmlTag().getName(), method.getName());
        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(Icons.MYBATIS_2_XML_ICON).setTarget(idAttrValue)
                        .setAlignment(GutterIconRenderer.Alignment.CENTER)
                        .setTooltipText("Navigate to mapper xml statement: " + statementDesc);
        PsiElement markElement = method.getNameIdentifier() == null ? method : method.getNameIdentifier();
        return builder.createLineMarkerInfo(markElement);
    }
}