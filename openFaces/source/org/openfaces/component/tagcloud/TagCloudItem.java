/*
 * OpenFaces - JSF Component Library 2.0
 * Copyright (C) 2007-2010, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */

package org.openfaces.component.tagcloud;

import org.openfaces.component.OUIOutput;
import org.openfaces.util.Rendering;
import org.openfaces.util.Script;
import org.openfaces.util.ScriptBuilder;
import org.openfaces.util.StyleGroup;
import org.openfaces.util.Styles;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * @author : roman.nikolaienko
 */
public class TagCloudItem extends OUIOutput {

    public static final String COMPONENT_TYPE = "org.openfaces.TagCloudItem";
    public static final String COMPONENT_FAMILY = "org.openfaces.TagCloudItem";

    private static final String DEFAULT_TEXT_CONTAINER = "::item_textValue";
    private static final String DEFAULT_WEIGHT = "::item_weight";

    private static final String DEFAULT_CLASS = "o_tagCloud_item";
    private static final String DEFAULT_ROLLOVER_CLASS = "o_tagCloud_item_rollover";

    private String title;
    private String url;

    private double weight;
    private boolean weightVisible;

    private String cloudId;
    private String gradientStyle;
    private static final String DEFAULT_WEIGHT_STYLE = "margin-left:3px;font-size: 0.7em; vertical-align: super;";

    public TagCloudItem() {
        setRendererType("org.openfaces.TagCloudItem");
    }

    private String getDefaultStyleClass() {
        return DEFAULT_CLASS;
    }

    private String getDefaultRolloverClass() {
        return DEFAULT_ROLLOVER_CLASS;
    }

    private String getStyles() {
        return Styles.mergeStyles(getGradientStyle(), getStyle());
    }

    private String getStyleClass(FacesContext context) {
        return Styles.getCSSClass(context, this, getStyles(),
                StyleGroup.regularStyleGroup(),
                this.getStyleClass(),
                getDefaultStyleClass());
    }

    private String getRolloverStyleClass(FacesContext context) {
        return Styles.getCSSClass(context, this, getRolloverStyle(),
                StyleGroup.rolloverStyleGroup(),
                getRolloverClass(),
                getDefaultRolloverClass());
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public Object saveState(FacesContext context) {
        return new Object[]{
                super.saveState(context),
                title,
                url,
                weight,
                weightVisible
        };
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        int i = 0;
        super.restoreState(context, values[i++]);
        title = (String) values[i++];
        url = (String) values[i++];
        weight = (Double) values[i++];
        weightVisible = (Boolean) values[i];
    }

    public String getTitle() {
        return title == null ? "null" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url == null ? "null" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isWeightVisible() {
        return weightVisible;
    }

    public void setWeightVisible(boolean widthVisible) {
        this.weightVisible = widthVisible;
    }

    public String getTextValue() {
        return Rendering.convertToString(getFacesContext(), this, getValue());
    }


    public void encodeBegin(FacesContext context) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String id = getCloudId() + "::" + getClientId(context);
        String styleClass = getStyleClass(context);
        Styles.renderStyleClasses(context, this, true, false);
        TagCloud cloud = (TagCloud) getParent();
        DecimalFormat weightFormat = new DecimalFormat(cloud.getItemWeightFormat());
        String weightStyle = cloud.getItemWeightStyle();

        writer.startElement("a", cloud);

        writer.writeAttribute("id", id, null);
        writer.writeAttribute("title", getTitle(), null);
        writer.writeAttribute("href", getUrl(), null);
        writer.writeAttribute("class", styleClass, null);
        writer.writeAttribute("style", getStyle(), null);

        writer.startElement("span", this);
        writer.writeAttribute("id", id + DEFAULT_TEXT_CONTAINER, null);
        writer.writeText(getTextValue(), null);
        writer.endElement("span");
        StringBuilder styleBuilder = new StringBuilder(getDefaultWeightStyle());
        if (weightStyle != null)
            styleBuilder.append(weightStyle);
        writer.startElement("span", this);
        writer.writeAttribute("id", id + DEFAULT_WEIGHT, null);

        if (!isWeightVisible())
            styleBuilder.append("display:none;");

        writer.writeAttribute("style", styleBuilder.toString(), null);
        writer.writeText(weightFormat.format(getWeight()), null);
        writer.endElement("span");

        Script initScript = new ScriptBuilder().initScript(context, this, "O$.TagCloudItem._init",
                getCloudId(),
                getRolloverStyleClass(context)
        );
        Rendering.renderInitScript(context, initScript);
    }

    private String getDefaultWeightStyle() {
        return DEFAULT_WEIGHT_STYLE;
    }


    public void encodeEnd(FacesContext context) throws IOException {

        ResponseWriter writer = context.getResponseWriter();

        writer.endElement("a");

        writer.flush();
    }

    public void encodeChildren(FacesContext context) throws IOException {
        super.encodeChildren(context);
    }


    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public String getCloudId() {
        return cloudId;
    }

    public String getGradientStyle() {
        return gradientStyle;
    }

    public void setGradientStyle(String gradientStyle) {
        this.gradientStyle = gradientStyle;
    }
}
